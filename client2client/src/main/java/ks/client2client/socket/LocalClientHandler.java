package ks.client2client.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LocalClientHandler extends ChannelInboundHandlerAdapter {
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("Local Channel Active - channel : " + ctx.channel().id().asShortText());
    SocketClientMain.getInstance().mapLocalCtx(ctx);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ChannelHandlerContext oppositeCtx = SocketClientMain.getInstance().getOppositeCtx(ctx);
    oppositeCtx.writeAndFlush(msg);
  }
  
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("Local Channel Close - channel : " + ctx.channel().id().asShortText());
    SocketClientMain.getInstance().closeLocalAndServerCtx(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    System.out.println("Local Channel Close - channel : " + ctx.channel().id().asShortText());
    SocketClientMain.getInstance().closeLocalAndServerCtx(ctx);
  }
}
