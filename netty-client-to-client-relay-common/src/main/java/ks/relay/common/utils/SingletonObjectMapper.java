package ks.relay.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SingletonObjectMapper {
  private static class ObjectMapperHolder {
    public static final ObjectMapper mapper = new ObjectMapper();
  }
  
  public static ObjectMapper getObjectMapper() {
    return ObjectMapperHolder.mapper;
  }
}
