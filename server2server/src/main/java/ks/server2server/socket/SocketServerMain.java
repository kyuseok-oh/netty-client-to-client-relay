package ks.server2server.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import ks.server2server.protocol.Server2ServerManagementProtocol;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SocketServerMain {
  Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  
  private static final String os = System.getProperty("os.name").toLowerCase();
  
  public static final String SOCKET_HANDLER = "SocketHandler";
  public static final String CLIENT_SOCKET_HANDLER = "ClientSocketHandler";
  public static final String SERVER_MANAGER_SOCKET_HANDLER = "ClientSocketHandler";
  
  // Holder for singleton class.
  private static class SockServerHolder {
    public static final SocketServerMain instance = new SocketServerMain();
  }
  
  private static HashMap<Channel, ChannelDescriptor> channelDescriptorMap = new HashMap<>();
  
  List<ChannelHandlerContext> notMappedClientCtxList = new ArrayList<>();
  List<ChannelHandlerContext> notMappedServerCtxList = new ArrayList<>();
  
  @Getter @Setter private Channel serverManagerChannel = null;

  @Getter @Setter private int port;
  
  private static ObjectMapper objectMapper;

  public void acceptor() {
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    Class<? extends ServerChannel> channelClass;

    // If it is Windows or MAC, it operates in NIO non-blocking mode.
    if (isWindows() || isMac()) {
      bossGroup = new NioEventLoopGroup(1);
      workerGroup = new NioEventLoopGroup();
      channelClass = NioServerSocketChannel.class;
    }

    // On Linux base, it operates in epoll mode (higher performance than NIO, dependent on OS).
    else {
      bossGroup = new EpollEventLoopGroup(1);
      workerGroup = new EpollEventLoopGroup();
      channelClass = EpollServerSocketChannel.class;
    }
    
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup).channel(channelClass)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
              ChannelPipeline p = ch.pipeline();
              p.addLast(SOCKET_HANDLER, new SocketHandler());
            }
          });
      ChannelFuture f = b.bind(this.port).sync();
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }

  // Singleton class instance return method.
  public static SocketServerMain getInstance() {
    return SockServerHolder.instance;
  }

  //os 구별용 메소드
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

  public static Map<Channel, ChannelDescriptor> getChannelDescriptorMap() {
    return channelDescriptorMap;
  }
  
  public synchronized void mapClientCtx(ChannelHandlerContext clientCtx) throws JsonProcessingException, InterruptedException {
    if(!notMappedServerCtxList.isEmpty()) {
      ChannelHandlerContext serverCtx = notMappedServerCtxList.remove(0);
      channelDescriptorMap.get(serverCtx.channel()).setOpposite(clientCtx.channel());
      channelDescriptorMap.get(clientCtx.channel()).setOpposite(serverCtx.channel());
      System.out.println("MAP : " + serverCtx.channel().id().asShortText() + " <==> " + clientCtx.channel().id().asShortText());
    } else {
      notMappedClientCtxList.add(clientCtx);
      Channel svrManagerChannel = SocketServerMain.getInstance().getServerManagerChannel();
      if ((svrManagerChannel != null) && svrManagerChannel.isActive()) {
        Server2ServerManagementProtocol.sendNewClientConnectRequest(clientCtx.channel());
      }
    }
  }
  
  public synchronized void mapServerCtx(ChannelHandlerContext serverCtx) {
    if(!notMappedClientCtxList.isEmpty()) {
      ChannelHandlerContext clientCtx = notMappedClientCtxList.remove(0);
      channelDescriptorMap.get(serverCtx.channel()).setOpposite(clientCtx.channel());
      channelDescriptorMap.get(clientCtx.channel()).setOpposite(serverCtx.channel());
      System.out.println("MAP : " + serverCtx.channel().id().asShortText() + " <==> " + clientCtx.channel().id().asShortText());
    } else {
      notMappedServerCtxList.add(serverCtx);
    }
  }
  
  public synchronized void clearNotMappedCtxList() {
    notMappedClientCtxList.clear();
    notMappedServerCtxList.clear();
  }
  
  public synchronized void deleteCtxFromNotMappedCtxList(ChannelHandlerContext ctx) {
    notMappedClientCtxList.removeIf(c -> (c.equals(ctx) || c == null || !c.channel().isActive()));
    notMappedServerCtxList.removeIf(c -> (c.equals(ctx) || c == null || !c.channel().isActive()));
  }
  
  public static ObjectMapper getObjectMapper() {
    if (objectMapper == null) {
      objectMapper = new ObjectMapper();
    }
    return objectMapper;
  }
}
