package de.as.fynancials.notification.dividendannouncement.config;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.NotFoundException;
import de.as.fynancials.notification.dividendannouncement.datasource.DividendAnnouncementDataSourceService;
import de.as.fynancials.security.SecurityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class DividendAnnouncementConfigServiceImpl implements DividendAnnouncementConfigService {

  private final DividendAnnouncementConfigRepository dividendAnnouncementConfigRepository;
  private final DividendAnnouncementConfigMapper dividendAnnouncementConfigMapper;
  private final SecurityService securityService;
  private final DividendAnnouncementDataSourceService dividendAnnouncementDataSourceService;

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
    validate(dividendAnnouncementConfig);
    DividendAnnouncementConfigEntity entity = dividendAnnouncementConfigMapper.toEntity(dividendAnnouncementConfig);
    entity = persist(entity);
    return dividendAnnouncementConfigMapper.fromEntity(entity);
  }

  @Override
  @Transactional
  public DividendAnnouncementConfig updateDividendAnnouncementConfig(
      DividendAnnouncementConfig dividendAnnouncementConfig)
      throws BadRequestException, ConflictException, NotFoundException {
    validate(dividendAnnouncementConfig);
    DividendAnnouncementConfigEntity existing = dividendAnnouncementConfigRepository
        .findBySecurityId(dividendAnnouncementConfig.getSecurityId()).orElseThrow(NotFoundException::new);
    if (!existing.getVersion().equals(dividendAnnouncementConfig.getVersion())) {
      throw new ConflictException();
    }
    DividendAnnouncementConfigEntity entity = dividendAnnouncementConfigMapper.toEntity(dividendAnnouncementConfig);
    entity = persist(entity);
    return dividendAnnouncementConfigMapper.fromEntity(entity);
  }

  @Override
  @Transactional
  public void deleteDividendAnnouncementConfig(long securityId) throws ConflictException, NotFoundException {
    DividendAnnouncementConfigEntity entity =
        dividendAnnouncementConfigRepository.findBySecurityId(securityId).orElseThrow(NotFoundException::new);
    try {
      dividendAnnouncementConfigRepository.delete(entity);
      dividendAnnouncementConfigRepository.flush();
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new ConflictException();
    }
  }

  private DividendAnnouncementConfigEntity persist(DividendAnnouncementConfigEntity entity) throws ConflictException {
    DividendAnnouncementConfigEntity saved;
    try {
      saved = dividendAnnouncementConfigRepository.saveAndFlush(entity);
    } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
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

    if (!dividendAnnouncementDataSourceService.dataSourceExists(dividendAnnouncementConfig.getDataSourceId())) {
      throw new BadRequestException();
    }

    securityService.getSecurity(dividendAnnouncementConfig.getSecurityId());
  }
}
