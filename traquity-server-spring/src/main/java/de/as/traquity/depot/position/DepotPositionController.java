package de.as.traquity.depot.position;

import de.as.traquity.common.error.NotFoundException;
import de.as.traquity.depot.DepotService;
import de.as.traquity.depot.position.api.controller.DepotPositionApiDelegate;
import de.as.traquity.depot.position.api.model.DepotCompositionDto;
import de.as.traquity.depot.position.api.model.LotDto;
import de.as.traquity.security.SecurityService;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class DepotPositionController implements DepotPositionApiDelegate {

  private final DepotService depotService;
  private final SecurityService securityService;
  private final DepotPositionService depotPositionService;
  private final DepotPositionMapper depotPositionMapper;
  private final LotMapper lotMapper;

  @Override
  public ResponseEntity<DepotCompositionDto> getDepotPositions(List<Long> depots) {
    DepotComposition positions;

    if (depots.size() == 1) {
      positions = depotPositionService.getDepotPositions(depots.getFirst(), true);
    } else {
      positions = depotPositionService.getDepotPositions(new HashSet<>(depots), true);
    }

    DepotCompositionDto responseBody = depotPositionMapper.toDepotCompositionDto(positions);
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<List<LotDto>> getLots(Long depotId, Long securityId) {
    if (!depotService.depotExists(depotId) || !securityService.securityExists(securityId)) {
      throw new NotFoundException();
    }
    List<LotDto> responseBody = depotPositionService.getLots(depotId, securityId).stream().map(lotMapper::toDto).toList();
    return ResponseEntity.ok(responseBody);
  }
}
