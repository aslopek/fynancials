package de.as.fynancials.configuration.securitygroup;

import java.util.Set;
import lombok.Data;

@Data
public class SecurityGroup {

  private Long id;
  private Long version;
  private String name;
  private Set<Long> securities;
}
