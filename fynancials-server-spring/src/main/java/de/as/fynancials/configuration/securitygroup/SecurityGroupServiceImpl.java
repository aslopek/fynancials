package de.as.fynancials.configuration.securitygroup;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.InternalServerErrorException;
import de.as.fynancials.common.error.NotFoundException;
import de.as.fynancials.security.SecurityService;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class SecurityGroupServiceImpl implements SecurityGroupService {

  private static final int MINIMUM_SECURITY_GROUP_SIZE = 2;

  private final SecurityGroupRepository securityGroupRepository;
  private final SecurityGroupMapper securityGroupMapper;
  private final SecurityService securityService;

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
    trimName(securityGroup);
    if (securityGroup.getSecurities() == null || securityGroup.getSecurities().size() < MINIMUM_SECURITY_GROUP_SIZE) {
      throw new BadRequestException();
    }
    verifySecuritiesExist(securityGroup.getSecurities());
    verifySecuritiesAreNotAssignedToAnotherGroup(securityGroup.getSecurities(), null);

    SecurityGroupEntity entity = securityGroupMapper.toEntity(securityGroup);
    entity = persist(entity);
    return securityGroupMapper.fromEntity(entity);
  }

  @Override
  @Transactional
  public SecurityGroup updateSecurityGroup(Long id, SecurityGroup securityGroup)
      throws BadRequestException, ConflictException, NotFoundException {
    SecurityGroupEntity existing = securityGroupRepository.findById(id).orElseThrow(NotFoundException::new);
    if (!existing.getVersion().equals(securityGroup.getVersion())) {
      throw new ConflictException();
    }
    trimName(securityGroup);
    if (securityGroup.getSecurities() == null || securityGroup.getSecurities().size() < MINIMUM_SECURITY_GROUP_SIZE) {
      throw new BadRequestException();
    }
    verifySecuritiesExist(securityGroup.getSecurities());
    verifySecuritiesAreNotAssignedToAnotherGroup(securityGroup.getSecurities(), id);

    SecurityGroupEntity entity = securityGroupMapper.toEntity(securityGroup);
    entity.setId(id);
    entity = persist(entity);
    return securityGroupMapper.fromEntity(entity);
  }

  @Override
  @Transactional
  public void deleteSecurityGroup(Long id) throws ConflictException, NotFoundException {
    SecurityGroupEntity entity = securityGroupRepository.findById(id).orElseThrow(NotFoundException::new);
    try {
      securityGroupRepository.delete(entity);
      securityGroupRepository.flush();
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new ConflictException();
    }
  }

  private SecurityGroupEntity persist(SecurityGroupEntity entity) throws ConflictException {
    SecurityGroupEntity saved;
    try {
      saved = securityGroupRepository.saveAndFlush(entity);
    } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
      throw new ConflictException();
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

  private void verifySecuritiesExist(Collection<Long> securityIds) throws BadRequestException {
    for (Long securityId : securityIds) {
      if (!securityService.securityExists(securityId)) {
        throw new BadRequestException();
      }
    }
  }

  private void trimName(SecurityGroup securityGroup) throws BadRequestException {
    if (securityGroup.getName() == null) {
      throw new BadRequestException();
    }
    securityGroup.setName(securityGroup.getName().trim());
    if (securityGroup.getName().isBlank()) {
      throw new BadRequestException();
    }
  }
}
