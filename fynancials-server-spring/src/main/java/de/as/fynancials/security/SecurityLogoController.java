package de.as.fynancials.security;

import de.as.fynancials.security.api.controller.SecurityLogoApiDelegate;
import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Data
@Controller
class SecurityLogoController implements SecurityLogoApiDelegate {

  private final SecurityService securityService;

  @Override
  public ResponseEntity<Void> deleteLogo(Long id) {
    securityService.deleteLogo(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Resource> getLogo(Long id) {
    return ResponseEntity.ok(securityService.getLogo(id));
  }

  @Override
  public ResponseEntity<Void> setLogo(Long id, Resource body) {
    securityService.setLogo(id, body);
    return ResponseEntity.noContent().build();
  }
}
