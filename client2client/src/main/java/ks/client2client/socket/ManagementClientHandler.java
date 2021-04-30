package ks.client2client.socket;

import java.util.ArrayList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ks.client2client.protocol.ManagementClient;
import ks.client2client.protocol.ManagementProtocol;

public class ManagementClientHandler extends ChannelInboundHandlerAdapter {
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("Management Channel Active - channel : " + ctx.channel().id().asShortText());
    SocketClientMain.getInstance().getManagementClient().setCtx(ctx);
    ManagementProtocol.sendManagementSocketAccess(SocketClientMain.getInstance().getManagementClient());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if(SocketClientMain.getInstance().getManagementClient().getDataBuffer() == null) {
      SocketClientMain.getInstance().getManagementClient().setDataBuffer(new ArrayList<>());
    }
    SocketClientMain.getInstance().getManagementClient().getDataBuffer().add(((ByteBuf) msg).copy());
    ((ByteBuf) msg).release();
  }
  
  @Override @SuppressWarnings("unchecked")
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ManagementClient client = SocketClientMain.getInstance().getManagementClient();
    if (client.getDataBuffer() != null) {
      ManagementProtocol.runReceivedMsg(client, (ArrayList<ByteBuf>)client.getDataBuffer().clone());
    }
    client.setDataBuffer(null);
  }
  
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("Management Channel Close - channel : " + ctx.channel().id().asShortText());
    if(ctx.channel().isActive() || ctx.channel().isOpen()) {
      ctx.close();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    System.out.println("Management Channel Close - channel : " + ctx.channel().id().asShortText());
    if(ctx.channel().isActive() || ctx.channel().isOpen()) {
      ctx.close();
    }
  }
}
