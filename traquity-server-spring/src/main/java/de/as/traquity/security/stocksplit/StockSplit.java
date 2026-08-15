package de.as.traquity.security.stocksplit;

import java.time.LocalDate;
import lombok.Data;

@Data
public class StockSplit {

  private Long securityId;
  private LocalDate exDate;
  private Long quantityOld;
  private Long quantityNew;
}
