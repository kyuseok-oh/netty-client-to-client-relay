package ks.server2server.socket;

import java.util.ArrayList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ks.relay.common.protocol.vo.ChannelDescriptor;
import ks.server2server.protocol.Server2ServerManagementProtocol;

public class ServerManagerSocketHandler extends ChannelInboundHandlerAdapter {
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ChannelDescriptor channelDesc = SocketServerMain.getChannelDescriptorMap().get(ctx.channel());
    if (channelDesc.getByteBufList() == null) {
      channelDesc.setByteBufList(new ArrayList<>());
    }
    channelDesc.getByteBufList().add(((ByteBuf) msg).copy());
    ((ByteBuf) msg).release();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ChannelDescriptor channelDesc = SocketServerMain.getChannelDescriptorMap().get(ctx.channel());
    if (channelDesc.getByteBufList() != null) {
      Server2ServerManagementProtocol.runReceivedMsg(ctx.channel(),
          (ArrayList<ByteBuf>) channelDesc.getByteBufList().clone());
    }
    channelDesc.setByteBufList(null);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    channelClose(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    channelClose(ctx);
  }

  private void channelClose(ChannelHandlerContext ctx) {
    System.out.println("Server Manager Channel Close - channel : " + ctx.channel().id().asShortText());
    
    if (SocketServerMain.getChannelDescriptorMap().containsKey(ctx.channel())) {
      SocketServerMain.getChannelDescriptorMap().remove(ctx.channel());
    }
    
    SocketServerMain.getInstance().setServerManagerChannel(null);

    if (ctx.channel().isActive() || ctx.channel().isOpen()) {
      ctx.close();
    }
  }
}
