package ks.client2client.protocol;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import ks.client2client.socket.SocketClientMain;
import ks.relay.common.protocol.AbstractManagementProtocol;
import ks.relay.common.protocol.dto.request.NewClientConnectRequest;
import ks.relay.common.protocol.dto.response.NewClientConnectResponse;
import ks.relay.common.protocol.enums.FunctionCodes;
import ks.relay.common.protocol.types.UnsignedInt;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagementProtocol extends AbstractManagementProtocol {

  public static void runReceivedMsg(ManagementClient managementClient, List<ByteBuf> data) {
    byte[] dataBuffer = assembleMsg(data);

    int index = getIdxOfFuncCode(dataBuffer); // Check Start Communication code

    if (index < 0 || (index + 12 > dataBuffer.length)) {
      return;
    }

    FunctionCodes functionCode = FunctionCodes.fromCode((int) UnsignedInt.parse(dataBuffer, index));
    index += 4;
    int channelIdLength = (int) UnsignedInt.parse(dataBuffer, index);
    index += 4;
    int bodyByteLength = (int) UnsignedInt.parse(dataBuffer, index);
    index += 4;

    if ((index + channelIdLength + bodyByteLength + 8) > dataBuffer.length) {
      return;
    }

    byte[] tempChannelId = new byte[channelIdLength];
    System.arraycopy(dataBuffer, index, tempChannelId, 0, channelIdLength);
    index += channelIdLength;

    String bodyStr = null;

    if (bodyByteLength > 0) {
      byte[] body = new byte[bodyByteLength];
      System.arraycopy(dataBuffer, index, body, 0, bodyByteLength);
      index += bodyByteLength;
      bodyStr = new String(body, StandardCharsets.UTF_8);
    }

    if (!checkEndOfData(index, dataBuffer)) {
      return;
    }

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
