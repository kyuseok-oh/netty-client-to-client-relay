package ks.client2client;

import ks.client2client.socket.SocketClientMain;

public class Client2ClientMain 
{
    public static void main( String[] args ) throws InterruptedException
    {
      if(args.length != 3) {
        System.out.println("Usage : java -jar [Path of This JAR] [Inbound Port for Your Server App] [Server2Server Address] [Server2Server Port]");
        System.out.println("Example for SSH : java -jar ./client2client.jar 22 10.0.0.1 4000");
        return;
      }
      final int inboundPort = Integer.parseInt(args[0]);
      final String addr = args[1];
      final int port = Integer.parseInt(args[2]);
      
      SocketClientMain.getInstance().setAddr(addr);
      SocketClientMain.getInstance().setInPort(inboundPort);
      SocketClientMain.getInstance().setPort(port);
      SocketClientMain.getInstance().connectManagementClient();
      SocketClientMain.getInstance().closeAllConnections();
    }
    
}
