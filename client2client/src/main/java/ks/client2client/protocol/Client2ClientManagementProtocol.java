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
import ks.relay.common.utils.SingletonObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Client2ClientManagementProtocol extends AbstractManagementProtocol {

  public static void runReceivedMsg(ManagementClient managementClient, List<ByteBuf> data) {
    ParsedProtocolMsg msg;
    try {
      msg = readProtocolMsg(data);
    } catch (NoMoreDataException e) {
      System.out.println("WARN: Client2ClientManagementProtocol : No more data to read..");
      return;
    } catch (EndOfDataException e) {
      System.out.println("WARN: Client2ClientManagementProtocol : End-Of-Data Error");
      return;
    }
    
    FunctionCodes functionCode = msg.getFunctionCode();
    String bodyStr = msg.getBody();

    try {
      switch (functionCode) {
        case managementSocketAccessResponse:
          System.out.println("Received : ManagementSocketAccessResponse - from : " + managementClient.getOpposite().id().asShortText());
          managementClient.setAvailable(true);
          break;
        case newClientConnectRequest:
          System.out.println("Received : NewClientConnectRequest - from : " + managementClient.getOpposite().id().asShortText());
          NewClientConnectRequest request = SingletonObjectMapper.getObjectMapper().readValue(bodyStr, NewClientConnectRequest.class);
          newClientConnect(request, managementClient);
          break;
        case healthCheckResponse:
          System.out.println("Received : HealthCheckResponse - from : " + managementClient.getOpposite().id().asShortText());
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
    System.out.println("Send : ManagementSocketAccessRequest - to : " +  managementClient.getOpposite().id().asShortText());
    sendProtocolMsg(managementClient.getOpposite(), FunctionCodes.managementSocketAccessRequest);
  }
  
  public static void sendHealthCheckRequest(ManagementClient managementClient) {
    System.out.println("Send : HealthCheckRequest - to : " +  managementClient.getOpposite().id().asShortText());
    managementClient.setAvailable(false);
    sendProtocolMsg(managementClient.getOpposite(), FunctionCodes.healthCheckRequest);
  }

  private static void newClientConnect(NewClientConnectRequest request, ManagementClient managementClient) throws JsonProcessingException {
    System.out.println("Send : NewClientConnectResponse - to : " +  managementClient.getOpposite().id().asShortText());
    String origChannelId = request.getOrigClientChannelId();
    
    if(StringUtils.isBlank(origChannelId)) {
      return;
    }
    
    String generatedClientChannelId = SocketClientMain.getInstance().connectNewClient().id().asLongText();
    
    NewClientConnectResponse response = NewClientConnectResponse.builder()
        .origClientChannelId(origChannelId)
        .generatedClientChannelId(generatedClientChannelId)
        .build(); 
    
    String retBody = SingletonObjectMapper.getObjectMapper().writeValueAsString(response);
    
    sendProtocolMsg(managementClient.getOpposite(), FunctionCodes.newClientConnectResponse, retBody);
  }

}
