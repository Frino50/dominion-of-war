package dow.controller;

import dow.model.dto.UnitStatsDto;
import dow.service.UnitStatsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unit-stats")
public class UnitStatsController {

    private final UnitStatsService unitStatsService;

    public UnitStatsController(UnitStatsService unitStatsService) {
        this.unitStatsService = unitStatsService;
    }

    @GetMapping("/{spriteName}")
    public UnitStatsDto getUnitStatsBySpriteName(@PathVariable String spriteName) {
        return unitStatsService.getUnitStatsBySpriteName(spriteName);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UnitStatsDto createOrUpdateUnitStats(@RequestBody UnitStatsDto unitStatsDto) {
        return unitStatsService.createOrUpdateUnitStats(unitStatsDto);
    }
}
