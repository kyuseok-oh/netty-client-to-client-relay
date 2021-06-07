package ks.server2server.protocol;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import ks.relay.common.protocol.AbstractManagementProtocol;
import ks.relay.common.protocol.dto.request.ManagementSocketAccessRequest;
import ks.relay.common.protocol.dto.request.NewClientConnectRequest;
import ks.relay.common.protocol.dto.response.ManagementSocketAccessResponse;
import ks.relay.common.protocol.enums.FunctionCodes;
import ks.relay.common.protocol.exception.EndOfDataException;
import ks.relay.common.protocol.exception.NoMoreDataException;
import ks.relay.common.protocol.vo.ParsedProtocolMsg;
import ks.relay.common.utils.SingletonObjectMapper;
import ks.server2server.socket.ServerManagerSocketHandler;
import ks.server2server.socket.SocketServerMain;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j @NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Server2ServerManagementProtocol extends AbstractManagementProtocol {

  public static void runReceivedMsg(Channel channel, List<ByteBuf> data) {
    ParsedProtocolMsg msg;
    try {
      msg = readProtocolMsg(data);
    } catch (NoMoreDataException e) {
      System.out.println("WARN: Server2ServerManagementProtocol : No more data to read..");
      return;
    } catch (EndOfDataException e) {
      System.out.println("WARN: Server2ServerManagementProtocol : End-Of-Data Error");
      return;
    }
    
    FunctionCodes functionCode = msg.getFunctionCode();
    String bodyStr = msg.getBody();

    if ((SocketServerMain.getInstance().getServerManagerChannel() == null || !SocketServerMain.getInstance().getServerManagerChannel().isActive())
        && (!FunctionCodes.managementSocketAccessRequest.equals(functionCode))) {
      return;
    }
    
    try {
      switch (functionCode) {
        case managementSocketAccessRequest:
          System.out.println("Received : ManagementSocketAccessRequest - from : " + channel.id().asShortText());
          log.info("Received : ManagementSocketAccessRequest - from : {}", channel.id().asShortText());
          bodyStr = SocketServerMain.getInstance().getEncryptUtil().aesDecrypt(bodyStr);
          ManagementSocketAccessRequest request = SingletonObjectMapper.getObjectMapper().readValue(bodyStr, ManagementSocketAccessRequest.class);
          handleManagementSocketAccessRequest(request, channel);
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
    } catch(JsonProcessingException | GeneralSecurityException e) {
      if(functionCode.equals(FunctionCodes.managementSocketAccessRequest)) {
        try {
          sendManagementSocketAccessResponse(false, channel);
        } catch (JsonProcessingException e1) {
          e1.printStackTrace();
        }
      } else {
        e.printStackTrace();
      }
    }
  }
  
  private static void sendHealthCheckResponse(Channel channel) {
    System.out.println("Send : HealthCheckResponse - to : " + channel.id().asShortText());
    sendProtocolMsg(channel, FunctionCodes.healthCheckResponse);
  }
  
  private static void handleManagementSocketAccessRequest(ManagementSocketAccessRequest request, Channel channel) throws JsonProcessingException {
    if(!request.getApiKey().equals(SocketServerMain.getInstance().getApiKey())) {
      sendManagementSocketAccessResponse(false, channel);
      return;
    }
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
    
    sendManagementSocketAccessResponse(true, channel);
  }
  
  private static void sendManagementSocketAccessResponse(boolean isAllowed, Channel channel) throws JsonProcessingException {
    ManagementSocketAccessResponse response = ManagementSocketAccessResponse.builder()
        .isAllowed(isAllowed)
        .build();
    System.out.println("Send : ManagementSocketAccessResponse - to : " + channel.id().asShortText() + ", Allow : " + isAllowed);
    log.info("Send : ManagementSocketAccessResponse - to : {}, Allow : {}", channel.id().asShortText(), isAllowed);
    String retBody = SingletonObjectMapper.getObjectMapper().writeValueAsString(response);
    sendProtocolMsg(channel, FunctionCodes.managementSocketAccessResponse, retBody);
  }

  public static void sendNewClientConnectRequest(Channel channel) throws JsonProcessingException, InterruptedException, GeneralSecurityException {
    String origChannelId = channel.id().asLongText();
    Channel svrManagerChannel = SocketServerMain.getInstance().getServerManagerChannel();
    
    System.out.println("Send : NewClientConnectRequest - to : " + svrManagerChannel.id().asShortText());
    log.info("Send : NewClientConnectRequest - to : {}", svrManagerChannel.id().asShortText());
    
    NewClientConnectRequest request = NewClientConnectRequest.builder()
        .origClientChannelId(origChannelId)
        .build(); 
    
    String retBody = SingletonObjectMapper.getObjectMapper().writeValueAsString(request);
    retBody = SocketServerMain.getInstance().getEncryptUtil().aesEncrypt(retBody);
    
    sendProtocolMsg(svrManagerChannel, FunctionCodes.newClientConnectRequest, retBody);
  }

}
