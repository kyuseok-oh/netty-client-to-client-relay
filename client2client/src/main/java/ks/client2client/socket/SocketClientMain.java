package ks.client2client.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import ks.client2client.protocol.ManagementClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SocketClientMain {
  @Getter @Setter private int inPort;
  @Getter @Setter private String addr;
  @Getter @Setter private int port;
  
  @Getter BidiMap<ChannelHandlerContext, ChannelHandlerContext> ctxMap = new DualHashBidiMap<>();
  List<ChannelHandlerContext> notMappedLocalCtxList = new ArrayList<>();
  List<ChannelHandlerContext> notMappedServerCtxList = new ArrayList<>();
  
  @Getter private ManagementClient managementClient = new ManagementClient();
  
  EventLoopGroup group;
  Class<? extends SocketChannel> channelClass;
  Bootstrap bootstrap = new Bootstrap();
  
  private static String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
  
  public ChannelHandlerContext getOppositeCtx(ChannelHandlerContext ctx) {
    if(ctxMap.containsKey(ctx)) {
      return ctxMap.get(ctx);
    } else if(ctxMap.containsValue(ctx)) {
      return ctxMap.getKey(ctx);
    } else {
      return null;
    }
  }
  
  public synchronized void closeLocalAndServerCtx(ChannelHandlerContext ctx) {
    if(ctx.channel().isActive() || ctx.channel().isOpen()) {
      ctx.close();
    }
    ChannelHandlerContext oppositeCtx = SocketClientMain.getInstance().getOppositeCtx(ctx);
    notMappedLocalCtxList.removeIf(c -> (c.equals(ctx) || c == null || !c.channel().isActive()));
    notMappedServerCtxList.removeIf(c -> (c.equals(ctx) || c == null || !c.channel().isActive()));
    ctxMap.remove(ctx);
    ctxMap.removeValue(ctx);
    if(oppositeCtx != null && (oppositeCtx.channel().isActive() || oppositeCtx.channel().isOpen())) {
      oppositeCtx.close();
    }
  }
  
  public void closeAllConnections() {
    group.shutdownGracefully();
  }

  public void connectManagementClient() throws InterruptedException {
    // If it is Windows or MAC, it operates in NIO non-blocking mode.
    if (isWindows() || isMac()) {
      group = new NioEventLoopGroup();
      channelClass = NioSocketChannel.class;
    }

    // On Linux base, it operates in epoll mode (higher performance than NIO, dependent on OS).
    else {
      group = new EpollEventLoopGroup();
      channelClass = EpollSocketChannel.class;
    }
    bootstrap.group(group).channel(channelClass);
    try {
      ChannelFuture managementFuture = bootstrap.handler(new ManagementClientChannelInitializer()).connect(addr, port).sync();
      managementFuture.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw e;
    } finally {
      group.shutdownGracefully();
    }
  }
  
  public Channel connectNewClient() {
    bootstrap.handler(new LocalClientChannelInitializer()).connect("127.0.0.1", inPort);
    ChannelFuture serverFuture = bootstrap.handler(new ServerClientChannelInitializer()).connect(addr, port);
    return serverFuture.channel();
  }
  
  static class ManagementClientChannelInitializer extends ChannelInitializer<SocketChannel>{
    @Override
    public void initChannel(SocketChannel ch) {
      ChannelPipeline p = ch.pipeline();
      p.addLast(new ManagementClientHandler());
    }
  }
  
  static class LocalClientChannelInitializer extends ChannelInitializer<SocketChannel>{
    @Override
    public void initChannel(SocketChannel ch) {
      ChannelPipeline p = ch.pipeline();
      p.addLast(new LocalClientHandler());
//      p.addLast(new ChunkedWriteHandler());
    }
  }
  
  static class ServerClientChannelInitializer extends ChannelInitializer<SocketChannel>{
    @Override
    public void initChannel(SocketChannel ch) {
      ChannelPipeline p = ch.pipeline();
      p.addLast(new ServerClientHandler());
//      p.addLast(new ChunkedWriteHandler());
    }
  }
  
  public synchronized void mapLocalCtx(ChannelHandlerContext localCtx) {
    if(!notMappedServerCtxList.isEmpty()) {
      ChannelHandlerContext serverCtx = notMappedServerCtxList.remove(0);
      ctxMap.put(localCtx, serverCtx);
      System.out.println("MAP : " + serverCtx.channel().id().asShortText() + " <==> " + localCtx.channel().id().asShortText());
    } else {
      notMappedLocalCtxList.add(localCtx);
    }
  }
  
  public synchronized void mapServerCtx(ChannelHandlerContext serverCtx) {
    if(!notMappedLocalCtxList.isEmpty()) {
      ChannelHandlerContext localCtx = notMappedLocalCtxList.remove(0);
      ctxMap.put(localCtx, serverCtx);
      System.out.println("MAP : " + serverCtx.channel().id().asShortText() + " <==> " + localCtx.channel().id().asShortText());
    } else {
      notMappedServerCtxList.add(serverCtx);
    }
  }

  // Holder for singleton class.
  private static class SockServerHolder {
    public static final SocketClientMain instance = new SocketClientMain();
  }

  // Singleton class instance return method.
  public static SocketClientMain getInstance() {
    return SockServerHolder.instance;
  }
  
  public static boolean isWindows() {
    return (os.indexOf("win") >= 0);
  }

  public static boolean isMac() {
    return (os.indexOf("mac") >= 0);
  }

  public static boolean isUnix() {
    return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0);
  }

  public static boolean isSolaris() {
    return (os.indexOf("sunos") >= 0);
  }
}
