package ks.server2server;

import ks.server2server.socket.SocketServerMain;

public class Server2ServerMain 
{
    public static void main( String[] args )
    {
      if(args.length != 1) {
        System.out.println("Usage : java -jar [Path of This JAR] [Server2Server Port]");
        System.out.println("Example for SSH : java -jar ./server2server.jar 22");
        return;
      }
      
      final int port = Integer.parseInt(args[0]);
      
      
      SocketServerMain.getInstance().setPort(port);
      
      SocketServerMain.getInstance().acceptor();
    }
}
