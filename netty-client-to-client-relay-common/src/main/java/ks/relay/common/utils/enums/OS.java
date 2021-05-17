package ks.relay.common.utils.enums;

import java.util.Locale;

public enum OS {
  WINDOWS,
  MAC,
  UNIX_LIKE,
  SOLARLIS,
  OTHER
  ;
  
  public static OS getOS() {
    String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
    
    if (os.indexOf("win") >= 0) {
      return OS.WINDOWS;
    }
    
    if (os.indexOf("mac") >= 0) {
      return OS.MAC;
    }
    
    if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0) {
      return OS.UNIX_LIKE;
    }
    
    if (os.indexOf("sunos") >= 0) {
      return OS.SOLARLIS;
    }
    
    return OS.OTHER;
  }
}
