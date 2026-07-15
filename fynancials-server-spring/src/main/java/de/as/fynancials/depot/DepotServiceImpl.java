package de.as.fynancials.depot;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.InternalServerErrorException;
import de.as.fynancials.common.error.NoContentException;
import de.as.fynancials.common.error.NotFoundException;
import de.as.fynancials.common.image.ImageService;
import de.as.fynancials.configuration.ServerConfigurationService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class DepotServiceImpl implements DepotService {

  private final ImageService imageService;
  private final DepotLogoRepository depotLogoRepository;
  private final DepotRepository depotRepository;
  private final DepotMapper depotMapper;
  private final ServerConfigurationService serverConfigurationService;

  @Override
  public Depot createDepot(String name, String currency) throws BadRequestException, ConflictException {
    DepotEntity depotEntity = new DepotEntity();
    depotEntity.setName(name);
    depotEntity.setCurrency(currency);
    processDepot(depotEntity);
    depotEntity = persist(depotEntity);
    return depotMapper.fromEntity(depotEntity);
  }

  @Override
  public List<Depot> getDepots() throws NoContentException {
    List<DepotEntity> depots = depotRepository.findAll();
    if (depots.isEmpty()) {
      throw new NoContentException();
    }

    return depots.stream().map(depotMapper::fromEntity).toList();
  }

  @Override
  public boolean depotExists(long depotId) {
    return depotRepository.existsById(depotId);
  }

  @Override
  public Depot getDepot(Long id) throws NotFoundException {
    DepotEntity depotEntity = depotRepository.findById(id).orElseThrow(NotFoundException::new);
    return depotMapper.fromEntity(depotEntity);
  }


  @Override
  @Transactional
  public Depot updateDepot(Long id, String name, String currency, Long version) throws BadRequestException,
      NotFoundException, ConflictException {
    DepotEntity existing = depotRepository.findById(id).orElseThrow(NotFoundException::new);
    if (!existing.getVersion().equals(version)) {
      throw new ConflictException();
    }
    if (!existing.getCurrency().equals(currency)) {
      throw new BadRequestException();
    }
    DepotEntity depotEntity = new DepotEntity();
    depotEntity.setId(id);
    depotEntity.setName(name);
    depotEntity.setCurrency(currency);
    depotEntity.setVersion(version);
    processDepot(depotEntity);

    depotEntity = persist(depotEntity);
    return depotMapper.fromEntity(depotEntity);
  }

  @Override
  @Transactional
  public void deleteDepot(Long id) throws ConflictException, NotFoundException {
    DepotEntity entity = depotRepository.findById(id).orElseThrow(NotFoundException::new);
    try {
      depotRepository.delete(entity);
      depotRepository.flush();
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new ConflictException();
    }
  }

  @Override
  public boolean depotsHaveSameCurrency(Set<Long> depotIds) throws NotFoundException {
    List<DepotEntity> depots = depotRepository.findAllById(depotIds);
    if (depots.size() < depotIds.size()) {
      throw new NotFoundException();
    }
    Set<String> currencies = depots.stream().map(DepotEntity::getCurrency).collect(Collectors.toSet());
    return currencies.size() <= 1;
  }

  @Override
  public boolean hasLogo(Long depotId) {
    return depotLogoRepository.existsById(depotId);
  }

  @Override
  public Resource getLogo(Long depotId) throws NotFoundException {
    Optional<DepotLogoEntity> optionalLogo = depotLogoRepository.findById(depotId);
    if (optionalLogo.isEmpty()) {
      throw new NotFoundException();
    }
    return new ByteArrayResource(optionalLogo.get().getLogo());
  }

  @Override
  public void setLogo(Long depotId, Resource logo) throws BadRequestException {
    if (!depotRepository.existsById(depotId)) {
      throw new BadRequestException();
    }

    byte[] pngBytes;

    try {
      pngBytes = logo.getInputStream().readAllBytes();
    } catch (IOException e) {
      throw new BadRequestException();
    }

    if (!imageService.isPng(pngBytes)) {
      throw new BadRequestException();
    }

    pngBytes = imageService.scaleImage(pngBytes);
    DepotLogoEntity logoEntity = depotLogoRepository.findById(depotId).orElseGet(DepotLogoEntity::new);
    logoEntity.setId(depotId);
    logoEntity.setLogo(pngBytes);
    depotLogoRepository.saveAndFlush(logoEntity);
  }

  @Override
  public void deleteLogo(Long depotId) {
    try {
      depotLogoRepository.deleteById(depotId);
    } catch (EmptyResultDataAccessException e) {
    }
  }

  private void processDepot(DepotEntity depot) throws BadRequestException {
    if (depot.getName() == null || depot.getName().isBlank()) {
      throw new BadRequestException();
    }
    depot.setName(depot.getName().trim());

    Set<String> supportedCurrencies = serverConfigurationService.getSupportedCurrencies();
    if (!supportedCurrencies.contains(depot.getCurrency())) {
      throw new BadRequestException();
    }
  }

  private DepotEntity persist(DepotEntity depot) throws ConflictException, InternalServerErrorException {
    DepotEntity saved;
    try {
      saved = depotRepository.saveAndFlush(depot);
    } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
      throw new ConflictException();
    } catch (Exception e) {
      log.error("{}: {}", e.getClass().getName(), e.getMessage());
      throw new InternalServerErrorException();
    }
    return saved;
  }
}
