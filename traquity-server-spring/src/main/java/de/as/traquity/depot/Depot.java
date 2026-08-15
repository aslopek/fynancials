package de.as.traquity.depot;

import lombok.Data;

@Data
public class Depot {

  private Long id;
  private Long version;
  private String name;
  private String currency;
}
