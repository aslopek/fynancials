package de.as.fynancials.notification.dividendannouncement.config;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.NotFoundException;
import de.as.fynancials.security.SecurityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class DividendAnnouncementConfigServiceImpl implements DividendAnnouncementConfigService {

  private final DividendAnnouncementConfigRepository dividendAnnouncementConfigRepository;
  private final DividendAnnouncementConfigMapper dividendAnnouncementConfigMapper;
  private final SecurityService securityService;

  @Override
  public List<DividendAnnouncementConfig> getDividendAnnouncementConfigs() {
    return dividendAnnouncementConfigRepository.findAllByOrderBySecurityIdAsc().stream()
        .map(dividendAnnouncementConfigMapper::fromEntity).toList();
  }

  @Override
  public DividendAnnouncementConfig getDividendAnnouncementConfig(long securityId) throws NotFoundException {
    DividendAnnouncementConfigEntity entity =
        dividendAnnouncementConfigRepository.findBySecurityId(securityId).orElseThrow(NotFoundException::new);
    return dividendAnnouncementConfigMapper.fromEntity(entity);
  }

  @Override
  public DividendAnnouncementConfig createDividendAnnouncementConfig(
      DividendAnnouncementConfig dividendAnnouncementConfig)
      throws BadRequestException, ConflictException, NotFoundException {
    if (dividendAnnouncementConfigRepository.existsBySecurityId(dividendAnnouncementConfig.getSecurityId())) {
      throw new ConflictException();
    }

    validate(dividendAnnouncementConfig);
    DividendAnnouncementConfigEntity entity = dividendAnnouncementConfigMapper.toEntity(dividendAnnouncementConfig);
    entity = persist(entity);
    return dividendAnnouncementConfigMapper.fromEntity(entity);
  }

  @Override
  public DividendAnnouncementConfig updateDividendAnnouncementConfig(
      DividendAnnouncementConfig dividendAnnouncementConfig) throws BadRequestException, NotFoundException {
    validate(dividendAnnouncementConfig);
    DividendAnnouncementConfigEntity entity = dividendAnnouncementConfigMapper.toEntity(dividendAnnouncementConfig);
    entity = persist(entity);
    return dividendAnnouncementConfigMapper.fromEntity(entity);
  }

  @Override
  public void deleteDividendAnnouncementConfig(long securityId) throws NotFoundException {
    long deletedCount = dividendAnnouncementConfigRepository.deleteAllBySecurityId(securityId);
    if (deletedCount == 0) {
      throw new NotFoundException();
    }
  }

  private DividendAnnouncementConfigEntity persist(DividendAnnouncementConfigEntity entity)
      throws BadRequestException, ConflictException {
    DividendAnnouncementConfigEntity saved;
    try {
      saved = dividendAnnouncementConfigRepository.saveAndFlush(entity);
    } catch (DataIntegrityViolationException e) {
      throw new BadRequestException();
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new ConflictException();
    }
    return saved;
  }

  private void validate(DividendAnnouncementConfig dividendAnnouncementConfig)
      throws BadRequestException, NotFoundException {
    if (dividendAnnouncementConfig.getExternalSecurityId() == null) {
      throw new BadRequestException();
    }

    if (dividendAnnouncementConfig.getExternalSecurityId().trim().isBlank()) {
      throw new BadRequestException();
    }

    securityService.getSecurity(dividendAnnouncementConfig.getSecurityId());
  }
}
