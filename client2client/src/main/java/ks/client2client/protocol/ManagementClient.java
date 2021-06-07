package ks.client2client.protocol;

import ks.relay.common.protocol.vo.ChannelDescriptor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ManagementClient extends ChannelDescriptor {
  private boolean isAvailable = false;
  private int healthCheckCounter = 0;
}
