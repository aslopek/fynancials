package de.as.fynancials.security.stocksplit;

import java.time.LocalDate;
import lombok.Data;

@Data
public class StockSplit {

  private Long securityId;
  private LocalDate exDate;
  private Long quantityOld;
  private Long quantityNew;
}
