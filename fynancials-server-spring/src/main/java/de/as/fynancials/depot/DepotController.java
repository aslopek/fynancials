package de.as.fynancials.depot;

import de.as.fynancials.depot.api.controller.DepotApiDelegate;
import de.as.fynancials.depot.api.model.DepotCreateDto;
import de.as.fynancials.depot.api.model.DepotReadDto;
import de.as.fynancials.depot.api.model.DepotUpdateDto;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class DepotController implements DepotApiDelegate {

  private static final String DEPOT_URI_PATTERN = "/depots/%d";

  private final DepotService depotService;
  private final DepotMapper depotMapper;

  @Override
  public ResponseEntity<DepotReadDto> createDepot(DepotCreateDto depotCreateDto) {
    Depot depot = depotService.createDepot(depotCreateDto.getName(), depotCreateDto.getCurrency());
    DepotReadDto responseBody = depotMapper.toDto(depot);
    URI locationHeader = URI.create(String.format(DEPOT_URI_PATTERN, depot.getId()));
    return ResponseEntity.created(locationHeader).body(responseBody);
  }

  @Override
  public ResponseEntity<List<DepotReadDto>> getDepots() {
    List<Depot> depots = depotService.getDepots();
    List<DepotReadDto> responseBody = depots.stream().map(depotMapper::toDto).toList();
    for (DepotReadDto depot : responseBody) {
      addLogoLink(depot);
    }
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<DepotReadDto> getDepot(Long id) {
    Depot depot = depotService.getDepot(id);
    DepotReadDto responseBody = depotMapper.toDto(depot);
    if (depotService.hasLogo(id)) {
      responseBody.getLinks().setLogo(String.format(DEPOT_URI_PATTERN, id) + "/logo");
    }
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<DepotReadDto> updateDepot(Long id, DepotUpdateDto depotUpdateDto) {
    Depot depot = depotService.updateDepot(id, depotUpdateDto.getName(), depotUpdateDto.getCurrency(),
        depotUpdateDto.getVersion());
    DepotReadDto responseBody = depotMapper.toDto(depot);
    addLogoLink(responseBody);
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<Void> deleteDepot(Long id) {
    depotService.deleteDepot(id);
    return ResponseEntity.noContent().build();
  }

  private void addLogoLink(DepotReadDto depot) {
    if (depotService.hasLogo(depot.getId())) {
      depot.getLinks().setLogo(String.format(DEPOT_URI_PATTERN, depot.getId()) + "/logo");
    }
  }
}
