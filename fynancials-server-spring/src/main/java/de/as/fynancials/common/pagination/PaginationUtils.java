package de.as.fynancials.common.pagination;

import de.as.fynancials.common.error.BadRequestException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PaginationUtils {

  private static final int DEFAULT_PAGE_NUMBER = 0;
  private static final int DEFAULT_PAGE_SIZE = 10;

  public static int getPageNumber(Integer page) throws BadRequestException {
    if (page != null && page < 0) {
      throw new BadRequestException();
    }
    return page == null ? DEFAULT_PAGE_NUMBER : page;
  }

  public static int getPageSize(Integer pageSize) throws BadRequestException {
    if (pageSize != null && pageSize < 1) {
      throw new BadRequestException();
    }
    return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
  }
}
