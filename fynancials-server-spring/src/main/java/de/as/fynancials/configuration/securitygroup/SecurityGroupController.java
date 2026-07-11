package de.as.fynancials.configuration.securitygroup;

import de.as.fynancials.config.securitygroup.api.controller.SecurityGroupApiDelegate;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupCreateDto;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupReadDto;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupUpdateDto;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class SecurityGroupController implements SecurityGroupApiDelegate {

  private static final String DEPOT_URI_PATTERN = "/config/security-groups/%d";

  private final SecurityGroupService securityGroupService;
  private final SecurityGroupMapper securityGroupMapper;

  @Override
  public ResponseEntity<SecurityGroupReadDto> createSecurityGroup(SecurityGroupCreateDto securityGroupCreateDto) {
    SecurityGroup securityGroup = securityGroupMapper.fromCreateDto(securityGroupCreateDto);
    securityGroup = securityGroupService.createSecurityGroup(securityGroup);
    SecurityGroupReadDto responseBody = securityGroupMapper.toDto(securityGroup);
    URI locationHeader = URI.create(String.format(DEPOT_URI_PATTERN, securityGroup.getId()));
    return ResponseEntity.created(locationHeader).body(responseBody);
  }

  @Override
  public ResponseEntity<Void> deleteSecurityGroup(Long id) {
    securityGroupService.deleteSecurityGroup(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<SecurityGroupReadDto>> getSecurityGroups() {
    List<SecurityGroup> securityGroups = securityGroupService.getSecurityGroups();
    List<SecurityGroupReadDto> responseBody = securityGroups.stream().map(securityGroupMapper::toDto).toList();
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<SecurityGroupReadDto> updateSecurityGroup(Long id,
                                                                  SecurityGroupUpdateDto securityGroupUpdateDto) {
    SecurityGroup securityGroup = securityGroupMapper.fromUpdateDto(securityGroupUpdateDto);
    securityGroup = securityGroupService.updateSecurityGroup(id, securityGroup);
    SecurityGroupReadDto responseBody = securityGroupMapper.toDto(securityGroup);
    return ResponseEntity.ok(responseBody);
  }
}
