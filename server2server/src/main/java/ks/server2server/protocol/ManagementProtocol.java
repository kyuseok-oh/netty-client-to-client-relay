package ks.server2server.protocol;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import ks.relay.common.protocol.AbstractManagementProtocol;
import ks.relay.common.protocol.dto.request.NewClientConnectRequest;
import ks.relay.common.protocol.enums.FunctionCodes;
import ks.relay.common.protocol.exception.EndOfDataException;
import ks.relay.common.protocol.exception.NoMoreDataException;
import ks.relay.common.protocol.vo.ParsedProtocolMsg;
import ks.server2server.socket.ServerManagerSocketHandler;
import ks.server2server.socket.SocketServerMain;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j @NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagementProtocol extends AbstractManagementProtocol {

  public static void runReceivedMsg(Channel channel, List<ByteBuf> data) {
    ParsedProtocolMsg msg;
    try {
      msg = readProtocolMsg(data);
    } catch (NoMoreDataException e) {
      System.out.println("WARN: ManagementProtocol : No more data to read..");
      return;
    } catch (EndOfDataException e) {
      System.out.println("WARN: ManagementProtocol : End-Of-Data Error");
      return;
    }
    
    FunctionCodes functionCode = msg.getFunctionCode();
    String bodyStr = msg.getBody();

    if ((SocketServerMain.getInstance().getServerManagerChannel() == null || !SocketServerMain.getInstance().getServerManagerChannel().isActive())
        && (!FunctionCodes.managementSocketAccessRequest.equals(functionCode))) {
      return;
    }
    switch (functionCode) {
      case managementSocketAccessRequest:
        System.out.println("Received : ManagementSocketAccessRequest - from : " + channel.id().asShortText());
        log.info("Received : ManagementSocketAccessRequest - from : {}", channel.id().asShortText());
        handleManagementSocketAccessRequest(channel);
        break;
      case newClientConnectResponse:
        System.out.println("Received : NewClientConnectResponse - from : " + channel.id().asShortText());
        log.info("Received : NewClientConnectResponse - from : {}", channel.id().asShortText());
        break;
      case healthCheckRequest:
        System.out.println("Received : HealthCheckRequest - from : " + channel.id().asShortText());
        sendHealthCheckResponse(channel);
        break;
      default:
        break;
      }
  }
  
  private static void sendHealthCheckResponse(Channel channel) {
    System.out.println("Send : HealthCheckResponse - to : " + channel.id().asShortText());
    sendProtocolMsg(channel, FunctionCodes.healthCheckResponse);
  }
  
  private static void handleManagementSocketAccessRequest(Channel channel) {
    if((SocketServerMain.getInstance().getServerManagerChannel() != null) && 
        (SocketServerMain.getInstance().getServerManagerChannel().isActive() || SocketServerMain.getInstance().getServerManagerChannel().isOpen())) {
      SocketServerMain.getInstance().getServerManagerChannel().close();
    }
    SocketServerMain.getInstance().setServerManagerChannel(channel);
    
    channel.pipeline().replace(
        SocketServerMain.SOCKET_HANDLER,
        SocketServerMain.SERVER_MANAGER_SOCKET_HANDLER,
        new ServerManagerSocketHandler());
    
    List<Channel> listForClose = new ArrayList<>();
    
    SocketServerMain.getChannelDescriptorMap().forEach((c, cd) ->{
      if(!c.equals(channel) && (c.isActive() || c.isOpen())) {
        listForClose.add(c);
      }
    });
    
    listForClose.forEach(Channel::close);
    
    SocketServerMain.getInstance().clearNotMappedCtxList();
    
    System.out.println("Send : ManagementSocketAccessResponse - to : " + channel.id().asShortText());
    log.info("Send : ManagementSocketAccessResponse - to : {}", channel.id().asShortText());
    
    sendProtocolMsg(channel, FunctionCodes.managementSocketAccessResponse);
  }

  public static void sendNewClientConnectRequest(Channel channel) throws JsonProcessingException, InterruptedException {
    String origChannelId = channel.id().asLongText();
    Channel svrManagerChannel = SocketServerMain.getInstance().getServerManagerChannel();
    
    System.out.println("Send : NewClientConnectRequest - to : " + svrManagerChannel.id().asShortText());
    log.info("Send : NewClientConnectRequest - to : {}", svrManagerChannel.id().asShortText());
    
    NewClientConnectRequest request = NewClientConnectRequest.builder()
        .origClientChannelId(origChannelId)
        .build(); 
    
    String retBody = SocketServerMain.getObjectMapper().writeValueAsString(request);
    sendProtocolMsg(svrManagerChannel, FunctionCodes.newClientConnectRequest, retBody);
  }

}
