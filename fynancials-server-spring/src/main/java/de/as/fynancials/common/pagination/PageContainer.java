package de.as.fynancials.common.pagination;

import java.util.List;
import lombok.Data;

@Data
public class PageContainer<T> {
  private long total;
  private int currentPage;
  private int lastPage;
  private int pageSize;
  private List<T> items;
}
