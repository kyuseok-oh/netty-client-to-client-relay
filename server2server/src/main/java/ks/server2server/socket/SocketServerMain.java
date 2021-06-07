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
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import ks.relay.common.protocol.vo.ChannelDescriptor;
import ks.relay.common.utils.EncryptUtil;
import ks.relay.common.utils.enums.OS;
import ks.server2server.protocol.Server2ServerManagementProtocol;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SocketServerMain {
  Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  
  private static final OS os = OS.getOS();
  
  public static final String SOCKET_HANDLER = "SocketHandler";
  public static final String CLIENT_SOCKET_HANDLER = "ClientSocketHandler";
  public static final String SERVER_MANAGER_SOCKET_HANDLER = "ServerManagerSocketHandler";
  
  // Holder for singleton class.
  private static class SockServerHolder {
    public static final SocketServerMain instance = new SocketServerMain();
  }
  
  private static HashMap<Channel, ChannelDescriptor> channelDescriptorMap = new HashMap<>();
  
  List<ChannelHandlerContext> notMappedClientCtxList = new ArrayList<>();
  List<ChannelHandlerContext> notMappedServerCtxList = new ArrayList<>();
  
  @Getter @Setter private Channel serverManagerChannel = null;

  @Getter @Setter private int port;
  @Getter private String apiKey;
  @Getter private EncryptUtil encryptUtil;
  
  public void acceptor() {
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    Class<? extends ServerChannel> channelClass;

    switch(os) {
      // On Linux base, it operates in epoll mode (higher performance than NIO, dependent on OS).
      case UNIX_LIKE:
        bossGroup = new EpollEventLoopGroup(1);
        workerGroup = new EpollEventLoopGroup();
        channelClass = EpollServerSocketChannel.class;
        break;
        
      // If it is Windows or MAC, it operates in NIO non-blocking mode.
      case WINDOWS:
      case MAC:
      case SOLARLIS:
      case OTHER:
      default:
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        channelClass = NioServerSocketChannel.class;
        break;
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

  public static Map<Channel, ChannelDescriptor> getChannelDescriptorMap() {
    return channelDescriptorMap;
  }
  
  public synchronized void mapClientCtx(ChannelHandlerContext clientCtx) throws JsonProcessingException, InterruptedException, GeneralSecurityException {
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

  public void setApiKey(String apiKey) throws NoSuchAlgorithmException {
    this.apiKey = apiKey;
    StringBuilder sb = new StringBuilder();
    String tmpStr = apiKey;
    while(sb.length() < 256) {
      tmpStr = EncryptUtil.shaEncrypt(tmpStr);
      sb.append(EncryptUtil.shaEncrypt(tmpStr));
    }
    this.encryptUtil = new EncryptUtil(sb.toString());
  }
  
}
