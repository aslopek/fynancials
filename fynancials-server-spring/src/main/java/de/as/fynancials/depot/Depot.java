package de.as.fynancials.depot;

import lombok.Data;

@Data
public class Depot {

  private Long id;
  private Long version;
  private String name;
  private String currency;
}
