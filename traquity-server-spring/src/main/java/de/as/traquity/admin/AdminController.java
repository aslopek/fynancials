package de.as.traquity.admin;

import de.as.traquity.admin.api.controller.AdminApiDelegate;
import de.as.traquity.admin.api.model.DatabaseConfigDto;
import de.as.traquity.admin.api.model.ThirdPartyLicenseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class AdminController implements AdminApiDelegate {

  private final AdminService adminService;
  private final ThirdPartyLicenseService thirdPartyLicenseService;

  @Override
  public ResponseEntity<DatabaseConfigDto> getDatabaseConfig() {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(adminService.getDatabaseConfig());
  }

  @Override
  public ResponseEntity<List<ThirdPartyLicenseDto>> getThirdPartyLicenses() {
    return ResponseEntity.ok(thirdPartyLicenseService.getThirdPartyLicenses());
  }

  @Override
  public ResponseEntity<Long> getPid() {
    return ResponseEntity.ok(ProcessHandle.current().pid());
  }

  @Override
  public ResponseEntity<Boolean> getDevModeActive() {
    return ResponseEntity.ok(adminService.isDevModeActive());
  }

  @Override
  public ResponseEntity<Void> setDevModeActive(Boolean body) {
    adminService.setDevModeActive(body);
    return ResponseEntity.noContent().build();
  }
}
