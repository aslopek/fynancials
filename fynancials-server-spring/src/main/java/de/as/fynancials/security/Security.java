package de.as.fynancials.security;

import de.as.fynancials.security.api.model.PriceMetaInfoDto;
import de.as.fynancials.security.api.model.SecurityTypeDto;
import java.util.List;
import lombok.Data;

@Data
public class Security {

  private Long id;
  private Long version;
  private String isin;
  private List<String> symbols;
  private String name;
  private String wkn;
  private String sector;
  private SecurityTypeDto securityType;
  private PriceMetaInfoDto priceMetaInfo;
}
