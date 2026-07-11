package de.as.fynancials.configuration.securitygroup;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.InternalServerErrorException;
import de.as.fynancials.common.error.NotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class SecurityGroupServiceImpl implements SecurityGroupService {

  private static final int MINIMUM_SECURITY_GROUP_SIZE = 2;

  private final SecurityGroupRepository securityGroupRepository;
  private final SecurityGroupMapper securityGroupMapper;

  @Override
  public List<SecurityGroup> getSecurityGroups() {
    List<SecurityGroupEntity> securityGroups = securityGroupRepository.findAll();
    return securityGroups.stream().map(securityGroupMapper::fromEntity).toList();
  }

  @Override
  public Map<Long, SecurityGroup> getSecurityGroupsBySecurityId() {
    List<SecurityGroup> securityGroups = getSecurityGroups();
    Map<Long, SecurityGroup> groupsBySecurityId = new HashMap<>();
    for (SecurityGroup securityGroup : securityGroups) {
      for (Long securityId : securityGroup.getSecurities()) {
        groupsBySecurityId.put(securityId, securityGroup);
      }
    }
    return groupsBySecurityId;
  }

  @Override
  public SecurityGroup createSecurityGroup(SecurityGroup securityGroup) throws BadRequestException, ConflictException {
    SecurityGroupEntity entity = securityGroupMapper.toEntity(securityGroup);
    trimName(entity);
    if (entity.getSecurities().size() < MINIMUM_SECURITY_GROUP_SIZE) {
      throw new BadRequestException();
    }
    verifySecuritiesAreNotAssignedToAnotherGroup(securityGroup.getSecurities(), null);
    if (securityGroupRepository.existsByName(entity.getName())) {
      throw new ConflictException();
    }
    entity = persist(entity);
    return securityGroupMapper.fromEntity(entity);
  }

  @Override
  public SecurityGroup updateSecurityGroup(Long id, SecurityGroup securityGroup)
      throws BadRequestException, ConflictException, NotFoundException {
    Optional<SecurityGroupEntity> fromDb = securityGroupRepository.findById(id);
    if (fromDb.isEmpty()) {
      throw new NotFoundException();
    }

    SecurityGroupEntity entity = securityGroupMapper.toEntity(securityGroup);
    entity.setId(id);
    entity.setName(securityGroup.getName());
    entity.setSecurities(securityGroup.getSecurities());
    trimName(entity);
    if (entity.getSecurities().size() < MINIMUM_SECURITY_GROUP_SIZE) {
      throw new BadRequestException();
    }
    verifySecuritiesAreNotAssignedToAnotherGroup(securityGroup.getSecurities(), id);

    Optional<SecurityGroupEntity> group = securityGroupRepository.findByName(entity.getName());
    if (group.isPresent() && !group.get().getId().equals(id)) {
      throw new ConflictException();
    }

    entity = persist(entity);
    return securityGroupMapper.fromEntity(entity);
  }

  @Override
  public void deleteSecurityGroup(Long id) throws NotFoundException {
    if (!securityGroupRepository.existsById(id)) {
      throw new NotFoundException();
    }
    securityGroupRepository.deleteById(id);
  }

  private SecurityGroupEntity persist(SecurityGroupEntity entity) throws BadRequestException, ConflictException {
    SecurityGroupEntity saved;
    try {
      saved = securityGroupRepository.saveAndFlush(entity);
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new ConflictException();
    } catch (DataIntegrityViolationException e) {
      throw new BadRequestException();
    } catch (Exception e) {
      log.error(e.getClass().getName() + ": " + e.getMessage());
      throw new InternalServerErrorException();
    }
    return saved;
  }

  private void verifySecuritiesAreNotAssignedToAnotherGroup(Collection<Long> newSecurityGroup, Long ownSecurityGroup)
      throws BadRequestException {
    List<SecurityGroupEntity> existingSecurityGroups = securityGroupRepository.findAll();
    if (ownSecurityGroup != null) {
      existingSecurityGroups =
          existingSecurityGroups.stream().filter(e -> !e.getId().equals(ownSecurityGroup)).toList();
    }

    Set<Long> groupedSecurityIds = new HashSet<>();
    for (SecurityGroupEntity group : existingSecurityGroups) {
      groupedSecurityIds.addAll(group.getSecurities());
    }

    if (!CollectionUtils.intersection(newSecurityGroup, groupedSecurityIds).isEmpty()) {
      throw new BadRequestException();
    }
  }

  private void trimName(SecurityGroupEntity entity) throws BadRequestException {
    if (entity.getName() == null) {
      throw new BadRequestException();
    }
    entity.setName(entity.getName().trim());
    if (entity.getName().isBlank()) {
      throw new BadRequestException();
    }
  }
}
