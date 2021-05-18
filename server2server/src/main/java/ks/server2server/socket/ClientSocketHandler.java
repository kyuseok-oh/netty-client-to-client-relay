package ks.server2server.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientSocketHandler extends ChannelInboundHandlerAdapter {
  Channel opposite;
  
  ClientSocketHandler(Channel opposite){
    super();
    this.opposite = opposite;
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    opposite.writeAndFlush(msg);
  }
  
  @Override
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    opposite.config().setAutoRead(ctx.channel().isWritable());
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
    System.out.println("Client Channel Close - channel : " + ctx.channel().id().asShortText());

    if (SocketServerMain.getChannelDescriptorMap().containsKey(ctx.channel())) {
      if ((opposite != null) && (opposite.isActive() || opposite.isOpen())) {
        opposite.close();
      }
      SocketServerMain.getInstance().deleteCtxFromNotMappedCtxList(ctx);
      SocketServerMain.getChannelDescriptorMap().remove(ctx.channel());
    }

    if (ctx.channel().isActive() || ctx.channel().isOpen()) {
      ctx.close();
    }
  }
}
