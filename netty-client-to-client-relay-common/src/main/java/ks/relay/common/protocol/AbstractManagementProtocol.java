package ks.relay.common.protocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import ks.relay.common.protocol.enums.FunctionCodes;
import ks.relay.common.protocol.types.UnsignedInt;

public abstract class AbstractManagementProtocol {
  // HEADER :
  // Start Communication code : 00 FF FF 00 FF 00 00 FF (8byte)
  // Function Code : 00 00 00 01 ~ FF FF FF FF (4byte)
  // Channel ID Length : Channel ID Byte Length (int : 32bit - 4byte)
  // Body Length : Body Byte Length (int : 32bit - 4byte)
  // Channel ID : From
  // Body :
  // End-of-Data : 00 00 FF FF FF FF 00 00 (8byte)

  // Start and End of Protocol.
  protected static final byte[] startCode = {(byte) (0x00), (byte) (0xff), (byte) (0xff),
      (byte) (0x00), (byte) (0xff), (byte) (0x00), (byte) (0x00), (byte) (0xff)};
  protected static final byte[] endCode = {(byte) (0x00), (byte) (0x00), (byte) (0xff), (byte) (0xff),
      (byte) (0xff), (byte) (0xff), (byte) (0x00), (byte) (0x00)};

  protected static void sendProtocolMsg(Channel channel, FunctionCodes function) {
    sendProtocolMsg(channel, function, null);
  }
  
  protected static void sendProtocolMsg(Channel channel, FunctionCodes function, String body) {
    ByteBuf sendBuf = ByteBufAllocator.DEFAULT.heapBuffer();

    sendBuf.writeBytes(startCode); // Start Code
    sendBuf.writeBytes(UnsignedInt.toByteArray(((long) function.getCode()))); // Function Code

    byte[] channelId = channel.id().asLongText().getBytes(StandardCharsets.UTF_8);
    byte[] bodyBytes = StringUtils.isEmpty(body) ? ArrayUtils.EMPTY_BYTE_ARRAY : body.getBytes();
    
    sendBuf.writeInt(channelId.length); // Channel ID Length
    sendBuf.writeInt(bodyBytes.length); // Body Length
    sendBuf.writeBytes(channelId); // Channel ID
    sendBuf.writeBytes(bodyBytes); // Body
    sendBuf.writeBytes(endCode); // End-of-Data

    channel.writeAndFlush(sendBuf);
  }
  
  protected static boolean checkEndOfData(int index, byte[] dataBuffer) {
    if ((index + 8) > dataBuffer.length) {
      return false;
    }
    return (dataBuffer[index] == endCode[0]) && (dataBuffer[index + 1] == endCode[1])
        && (dataBuffer[index + 2] == endCode[2]) && (dataBuffer[index + 3] == endCode[3])
        && (dataBuffer[index + 4] == endCode[4]) && (dataBuffer[index + 5] == endCode[5])
        && (dataBuffer[index + 6] == endCode[6]) && (dataBuffer[index + 7] == endCode[7]);
  }
  
  protected static int getIdxOfFuncCode(byte[] dataBuffer) {
    int index = -1;

    int bufLengthWithoutHeader = dataBuffer.length - 7;

    for (int i = 0; i < bufLengthWithoutHeader; i++) {
      boolean checker = (dataBuffer[i] == startCode[0]) && (dataBuffer[i + 1] == startCode[1])
          && (dataBuffer[i + 2] == startCode[2]) && (dataBuffer[i + 3] == startCode[3])
          && (dataBuffer[i + 4] == startCode[4]) && (dataBuffer[i + 5] == startCode[5])
          && (dataBuffer[i + 6] == startCode[6]) && (dataBuffer[i + 7] == startCode[7]);

      if (checker) {
        index = i + 8;
        break;
      }
    }

    if ((index != -1) && (index < dataBuffer.length)) {
      return index;
    } else {
      System.out.println("WARN: ManagementProtocol : No more data to read..");
      return -1;
    }
  }
  
  protected static byte[] assembleMsg(List<ByteBuf> list) {
    List<Byte> buf = new ArrayList<>();

    for (ByteBuf msg : list) {
      byte[] tmp = new byte[msg.slice().readableBytes()];
      msg.slice().readBytes(tmp);
      for (byte b : tmp) {
        buf.add(b);
      }
      msg.release();
    }

    byte[] receivedMsg = new byte[buf.size()];

    for (int i = 0; i < buf.size(); i++) {
      receivedMsg[i] = buf.get(i);
    }
    return receivedMsg;
  }
}
