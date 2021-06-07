package ks.client2client;

import java.security.NoSuchAlgorithmException;
import org.apache.commons.lang3.StringUtils;
import ks.client2client.socket.SocketClientMain;
import ks.relay.common.utils.EncryptUtil;

public class Client2ClientMain {
  public static void main(String[] args) throws InterruptedException {
    if (args.length != 4) {
      printManual();
      return;
    }

    final int inboundPort = Integer.parseInt(args[0]);
    final String addr = args[1];
    final int port = Integer.parseInt(args[2]);

    if (StringUtils.isBlank(args[3])) {
      printManual();
    }

    final String apiKey = args[3].trim();

    SocketClientMain.getInstance().setAddr(addr);
    SocketClientMain.getInstance().setInPort(inboundPort);
    SocketClientMain.getInstance().setPort(port);
    try {
      SocketClientMain.getInstance().setApiKey(EncryptUtil.shaEncrypt(apiKey));
    } catch (NoSuchAlgorithmException e) {
      System.out.println("Encrypt API Auth Key failed : " + e.getMessage());
      return;
    }
    SocketClientMain.getInstance().connectManagementClient();
    SocketClientMain.getInstance().closeAllConnections();
  }

  public static void printManual() {
    System.out.println(
        "Usage : java -jar [Path of This JAR] [Inbound Port for Your Server App] [Server2Server Address] [Server2Server Port] [API Auth Key]");
    System.out.println(
        "Example for SSH : java -jar ./client2client.jar 22 10.0.0.1 4000 YourAuthKeyString");
  }

}
