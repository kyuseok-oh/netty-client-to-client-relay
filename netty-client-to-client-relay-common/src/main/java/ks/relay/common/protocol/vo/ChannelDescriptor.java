package ks.relay.common.protocol.vo;

import java.util.ArrayList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

public class ChannelDescriptor {
  @Getter @Setter private Channel opposite;
  @Getter @Setter private ArrayList<ByteBuf> byteBufList;
  
  public ChannelDescriptor() {
    this.opposite = null;
    this.byteBufList = null;
  }
}
