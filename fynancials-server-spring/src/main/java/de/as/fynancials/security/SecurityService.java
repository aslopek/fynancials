package de.as.fynancials.security;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.InternalServerErrorException;
import de.as.fynancials.common.error.NoContentException;
import de.as.fynancials.common.error.NotFoundException;
import de.as.fynancials.common.error.UnprocessableEntityException;
import de.as.fynancials.common.pagination.PageContainer;
import de.as.fynancials.security.api.model.SecurityOrderPropertyDto;
import de.as.fynancials.security.api.model.SortOrderDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.Resource;

public interface SecurityService {

  Security createSecurity(Security security)
      throws BadRequestException, ConflictException, InternalServerErrorException;

  void deleteSecurity(Long securityId) throws NotFoundException;

  Map<Long, String> getNamesById();

  Map<Long, String> getNamesById(Set<Long> securityIds);

  PageContainer<Security> getSecurities(int page, int perPage, String search, SecurityOrderPropertyDto orderBy,
                                        SortOrderDto order) throws NoContentException;

  boolean securityExists(long securityId);

  Security getSecurity(Long securityId) throws NotFoundException;

  Security getSecurityByIsin(String isin) throws NotFoundException;

  Security updateSecurity(Security security)
      throws BadRequestException, ConflictException, NotFoundException, InternalServerErrorException;

  boolean hasLogo(Long securityId);

  Resource getLogo(Long securityId) throws NotFoundException;

  void setLogo(Long securityId, Resource logo) throws BadRequestException;

  void deleteLogo(Long securityId);

  BigDecimal getCagr(long securityId, LocalDate startDate, LocalDate endDate) throws BadRequestException, NotFoundException,
      UnprocessableEntityException;
}
