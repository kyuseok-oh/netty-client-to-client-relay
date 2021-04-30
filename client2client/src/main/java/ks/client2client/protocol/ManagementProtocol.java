package ks.client2client.protocol;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import ks.client2client.socket.SocketClientMain;
import ks.relay.common.protocol.AbstractManagementProtocol;
import ks.relay.common.protocol.dto.request.NewClientConnectRequest;
import ks.relay.common.protocol.dto.response.NewClientConnectResponse;
import ks.relay.common.protocol.enums.FunctionCodes;
import ks.relay.common.protocol.exception.EndOfDataException;
import ks.relay.common.protocol.exception.NoMoreDataException;
import ks.relay.common.protocol.vo.ParsedProtocolMsg;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagementProtocol extends AbstractManagementProtocol {

  public static void runReceivedMsg(ManagementClient managementClient, List<ByteBuf> data) {
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

    if (!managementClient.isAvailable()
        && (FunctionCodes.managementSocketAccessResponse.equals(functionCode))) {
      return;
    }
    try {
      switch (functionCode) {
        case managementSocketAccessResponse:
          System.out.println("Received : ManagementSocketAccessResponse - from : " + managementClient.getCtx().channel().id().asShortText());
          managementClient.setAvailable(true);
          break;
        case newClientConnectRequest:
          System.out.println("Received : NewClientConnectRequest - from : " + managementClient.getCtx().channel().id().asShortText());
          NewClientConnectRequest request = SocketClientMain.getObjectMapper().readValue(bodyStr, NewClientConnectRequest.class);
          newClientConnect(request, managementClient);
          break;
        case healthCheckResponse:
          System.out.println("Received : HealthCheckResponse - from : " + managementClient.getCtx().channel().id().asShortText());
          managementClient.setAvailable(true);
          break;
        default:
          break;
      }
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
  
  public static void sendManagementSocketAccess(ManagementClient managementClient) {
    System.out.println("Send : ManagementSocketAccessRequest - to : " +  managementClient.getCtx().channel().id().asShortText());
    sendProtocolMsg(managementClient.getCtx().channel(), FunctionCodes.managementSocketAccessRequest);
  }
  
  public static void sendHealthCheckRequest(ManagementClient managementClient) {
    System.out.println("Send : HealthCheckRequest - to : " +  managementClient.getCtx().channel().id().asShortText());
    managementClient.setAvailable(false);
    sendProtocolMsg(managementClient.getCtx().channel(), FunctionCodes.healthCheckRequest);
  }

  private static void newClientConnect(NewClientConnectRequest request, ManagementClient managementClient) throws JsonProcessingException {
    System.out.println("Send : NewClientConnectResponse - to : " +  managementClient.getCtx().channel().id().asShortText());
    String origChannelId = request.getOrigClientChannelId();
    
    if(StringUtils.isBlank(origChannelId)) {
      return;
    }
    
    String generatedClientChannelId = SocketClientMain.getInstance().connectNewClient().id().asLongText();
    
    NewClientConnectResponse response = NewClientConnectResponse.builder()
        .origClientChannelId(origChannelId)
        .generatedClientChannelId(generatedClientChannelId)
        .build(); 
    
    String retBody = SocketClientMain.getObjectMapper().writeValueAsString(response);
    
    sendProtocolMsg(managementClient.getCtx().channel(), FunctionCodes.newClientConnectResponse, retBody);
  }

}
