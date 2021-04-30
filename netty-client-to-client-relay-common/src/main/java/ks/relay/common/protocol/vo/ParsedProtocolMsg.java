package ks.relay.common.protocol.vo;

import ks.relay.common.protocol.enums.FunctionCodes;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder 
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParsedProtocolMsg {
  private final FunctionCodes functionCode;
  private final String body;
}
