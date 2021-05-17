package ks.server2server.socket;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ks.relay.common.protocol.vo.ChannelDescriptor;
import ks.server2server.protocol.Server2ServerManagementProtocol;

// 실제 각 G/W별 서버 역할을 수행하는 클래스
public class SocketHandler extends ChannelInboundHandlerAdapter {
  Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("Channel Active - channel : " + ctx.channel().id().asShortText());
    ChannelDescriptor channelDesc = new ChannelDescriptor();
    SocketServerMain.getChannelDescriptorMap().put(ctx.channel(), channelDesc);
    Channel svrManagerChannel = SocketServerMain.getInstance().getServerManagerChannel();
    if ((svrManagerChannel != null) && svrManagerChannel.isActive()) {
      String svrManagerAddr = svrManagerChannel.remoteAddress().toString().split(":")[0];
      String clientAddr = ctx.channel().remoteAddress().toString().split(":")[0];
      if (svrManagerAddr.equals(clientAddr)) {
        SocketServerMain.getInstance().mapServerCtx(ctx);
      } else {
        SocketServerMain.getInstance().mapClientCtx(ctx);
      }
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ChannelDescriptor channelDesc = SocketServerMain.getChannelDescriptorMap().get(ctx.channel());
    if (SocketServerMain.getInstance().getServerManagerChannel() == null) {
      if (channelDesc.getByteBufList() == null) {
        channelDesc.setByteBufList(new ArrayList<>());
      }
      channelDesc.getByteBufList().add(((ByteBuf) msg).copy());
      ((ByteBuf) msg).release();
    } else {
      while (channelDesc.getOpposite() == null
          && SocketServerMain.getInstance().getServerManagerChannel() != null
          && SocketServerMain.getInstance().getServerManagerChannel().isActive()) {
        // Wait for opposite channel mapping before reading.
        Thread.sleep(1);
      }
      System.out.println("DEBUG : SocketHandler CHANNEL READING | Channel = " + ctx.channel().id().asShortText());
      channelDesc.getOpposite().writeAndFlush(msg);
      ctx.pipeline().replace(SocketServerMain.SOCKET_HANDLER,
          SocketServerMain.CLIENT_SOCKET_HANDLER,
          new ClientSocketHandler(channelDesc.getOpposite()));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    if (SocketServerMain.getInstance().getServerManagerChannel() == null) {
      ChannelDescriptor channelDesc = SocketServerMain.getChannelDescriptorMap().get(ctx.channel());
      if (channelDesc.getByteBufList() != null) {
        Server2ServerManagementProtocol.runReceivedMsg(ctx.channel(),
            (ArrayList<ByteBuf>) channelDesc.getByteBufList().clone());
      }
      channelDesc.setByteBufList(null);
    }
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
    System.out.println("Channel Close - channel : " + ctx.channel().id().asShortText());

    if (SocketServerMain.getChannelDescriptorMap().containsKey(ctx.channel())) {
      Channel opposite =
          SocketServerMain.getChannelDescriptorMap().get(ctx.channel()).getOpposite();
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
