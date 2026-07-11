package de.as.fynancials.configuration.securitygroup;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.NotFoundException;
import java.util.List;
import java.util.Map;

public interface SecurityGroupService {

  List<SecurityGroup> getSecurityGroups();

  Map<Long, SecurityGroup> getSecurityGroupsBySecurityId();

  SecurityGroup createSecurityGroup(SecurityGroup securityGroup) throws BadRequestException, ConflictException;

  SecurityGroup updateSecurityGroup(Long id, SecurityGroup securityGroup)
      throws BadRequestException, ConflictException, NotFoundException;

  void deleteSecurityGroup(Long id) throws NotFoundException;
}
