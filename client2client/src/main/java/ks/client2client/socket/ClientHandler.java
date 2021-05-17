package ks.client2client.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {
  Channel opposite;
  
  ClientHandler(Channel opposite){
    super();
    this.opposite = opposite;
  }
  
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("Client Channel Active - channel : " + ctx.channel().id().asShortText());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    opposite.writeAndFlush(msg);
  }
  
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("Client Channel Close - channel : " + ctx.channel().id().asShortText());
    SocketClientMain.getInstance().closeLocalAndServerCtx(ctx, opposite);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    System.out.println("Client Channel Close - channel : " + ctx.channel().id().asShortText());
    SocketClientMain.getInstance().closeLocalAndServerCtx(ctx, opposite);
  }
}
