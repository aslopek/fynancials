package de.as.traquity.depot;

import de.as.traquity.depot.api.controller.DepotLogoApiDelegate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class DepotLogoController implements DepotLogoApiDelegate {

  private final DepotService depotService;

  @Override
  public ResponseEntity<Void> deleteLogo(Long id) {
    depotService.deleteLogo(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Resource> getLogo(Long id) {
    return ResponseEntity.ok(depotService.getLogo(id));
  }

  @Override
  public ResponseEntity<Void> setLogo(Long id, Resource body) {
    depotService.setLogo(id, body);
    return ResponseEntity.noContent().build();
  }
}
