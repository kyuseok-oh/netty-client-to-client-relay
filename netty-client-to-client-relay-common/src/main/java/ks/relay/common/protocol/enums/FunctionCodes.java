package ks.relay.common.protocol.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FunctionCodes {
  NOT_DEFINED(-1),                      // Not Defined Code
  managementSocketAccessRequest(1),     // Management Socket Access Function Request Code : 00 00 00 01
  managementSocketAccessResponse(2),    // Management Socket Access Function Response Code : 00 00 00 02
  newClientConnectRequest(3),           // New Client Connect Function Request Code : 00 00 00 03
  newClientConnectResponse(4),          // New Client Connect Function Response Code : 00 00 00 04
  healthCheckRequest(5),                // Health Check Function Request Code : 00 00 00 05
  healthCheckResponse(6)                // Health Check Function Response Code : 00 00 00 06
  ;

  @Getter private int code;
  
  public static FunctionCodes fromCode(int code) {
    FunctionCodes retFunctionCodes = FunctionCodes.NOT_DEFINED;
    for(FunctionCodes fc : FunctionCodes.values()) {
      if(fc.getCode() == code) {
        retFunctionCodes = fc;
        break;
      }
    }
    return retFunctionCodes;
  }
}
