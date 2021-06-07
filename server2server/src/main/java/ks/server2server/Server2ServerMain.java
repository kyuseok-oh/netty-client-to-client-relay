package ks.server2server;

import java.security.NoSuchAlgorithmException;
import org.apache.commons.lang3.StringUtils;
import ks.relay.common.utils.EncryptUtil;
import ks.server2server.socket.SocketServerMain;

public class Server2ServerMain {
  public static void main(String[] args) {
    if (!(args.length == 1 || args.length == 2)) {
      printManual();
      return;
    }

    final int port = Integer.parseInt(args[0]);

    final String apiKey;
    try {
      if (args.length == 2) {
        if (StringUtils.isBlank(args[1])) {
          printManual();
        }
        apiKey = EncryptUtil.shaEncrypt(args[1].trim());
        SocketServerMain.getInstance().setApiKey(apiKey);
      } else {
        String generatedKey = EncryptUtil.generateRandomPassword(32);
        apiKey = EncryptUtil.shaEncrypt(generatedKey);
        SocketServerMain.getInstance().setApiKey(apiKey);
        System.out.println("Generated API Auth Key : ");
        System.out.println(generatedKey);
      }
    } catch (NoSuchAlgorithmException e) {
      System.out.println("Encrypt API Auth Key failed : " + e.getMessage());
      return;
    }
    SocketServerMain.getInstance().setPort(port);
    SocketServerMain.getInstance().acceptor();
  }

  public static void printManual() {
    System.out.println(
        "Usage : java -jar [Path of This JAR] [Server2Server Port] [(Optional) API Auth Key]");
    System.out
        .println("Example for SSH with API Auth Key Generating : java -jar ./server2server.jar 22");
    System.out.println(
        "Example for SSH with Using Given API Auth Key : java -jar ./server2server.jar 22 YourAuthKeyString");
  }

}
