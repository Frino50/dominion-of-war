package perso.dow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import perso.dow.model.dto.RouteDto;
import perso.dow.service.RouteService;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<RouteDto>> getAvailableRoutes() {
        return ResponseEntity.ok(routeService.getAvailableRoutes());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RouteDto>> getAll() {
        return ResponseEntity.ok(routeService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteDto> create(@RequestBody RouteDto dto) {
        return ResponseEntity.ok(routeService.create(dto));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteDto> update(@RequestBody RouteDto dto) {
        return ResponseEntity.ok(routeService.update(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}