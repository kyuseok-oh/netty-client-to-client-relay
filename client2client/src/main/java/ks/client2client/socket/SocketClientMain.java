package ks.client2client.socket;

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
import io.netty.handler.timeout.IdleStateHandler;
import ks.client2client.protocol.ManagementClient;
import ks.relay.common.utils.enums.OS;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SocketClientMain {
  @Getter @Setter private int inPort;
  @Getter @Setter private String addr;
  @Getter @Setter private int port;
  
  @Getter private ManagementClient managementClient = new ManagementClient();
  
  private static final int IDLE_TIME_SECONDS = 20; // Idle Seconds For Health Check(HeartBeat)
  
  EventLoopGroup group;
  Class<? extends SocketChannel> channelClass;
  Bootstrap bootstrap = new Bootstrap();
  
  private static OS os = OS.getOS();
  
  public void closeLocalAndServerCtx(ChannelHandlerContext ctx, Channel opposite) {
    if(ctx.channel().isActive() || ctx.channel().isOpen()) {
      ctx.close();
    }
    if(opposite != null && (opposite.isActive() || opposite.isOpen())) {
      opposite.close();
    }
  }
  
  public void closeAllConnections() {
    group.shutdownGracefully();
  }

  public void connectManagementClient() throws InterruptedException {
    switch(os) {
      // On Linux base, it operates in epoll mode (higher performance than NIO, dependent on OS).
      case UNIX_LIKE:
        group = new EpollEventLoopGroup();
        channelClass = EpollSocketChannel.class;
        break;
        
      // If it is Windows or MAC, it operates in NIO non-blocking mode.
      case MAC:
      case SOLARLIS:
      case OTHER:
      case WINDOWS:
      default:
        group = new NioEventLoopGroup();
        channelClass = NioSocketChannel.class;
        break;
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
    return bootstrap.handler(new ServerClientChannelInitializer(bootstrap, inPort)).connect(addr, port).channel();
  }
  
  static class ManagementClientChannelInitializer extends ChannelInitializer<SocketChannel>{
    @Override
    public void initChannel(SocketChannel ch) {
      ChannelPipeline p = ch.pipeline();
      p.addLast(new IdleStateHandler(0, 0, IDLE_TIME_SECONDS));
      p.addLast(new ManagementChannelHandler());
    }
  }
  
  static class LocalClientChannelInitializer extends ChannelInitializer<SocketChannel>{
    private Channel opposite;
    
    LocalClientChannelInitializer(Channel opposite) {
      this.opposite = opposite;
    }
    
    @Override
    public void initChannel(SocketChannel ch) {
      ChannelPipeline p = ch.pipeline();
      p.addLast(new ClientHandler(opposite));
    }
  }
  
  static class ServerClientChannelInitializer extends ChannelInitializer<SocketChannel>{
    private Bootstrap bootStrap;
    private final int inPort;
    ServerClientChannelInitializer(Bootstrap bootStrap, int inPort){
      this.bootStrap = bootStrap;
      this.inPort = inPort;
    }
    @Override
    public void initChannel(SocketChannel ch) {
      Channel localChannel = bootStrap.handler(new LocalClientChannelInitializer(ch)).connect("127.0.0.1", inPort).channel();
      ChannelPipeline p = ch.pipeline();
      p.addLast(new ClientHandler(localChannel));
      System.out.println("MAP : " + ch.id().asShortText() + " <==> " + localChannel.id().asShortText());
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
}
