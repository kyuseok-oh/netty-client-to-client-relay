package ks.client2client.protocol;

import java.util.ArrayList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ManagementClient {
  private ChannelHandlerContext ctx = null;
  private ArrayList<ByteBuf> dataBuffer;
  private boolean isAvailable = false;
}
