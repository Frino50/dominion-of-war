package dow.controller;

import dow.model.dto.LoadoutUpdateDto;
import dow.model.dto.PlayerLoadoutDto;
import dow.model.dto.SpriteInfos;
import dow.service.LoadoutService;
import dow.service.UtilsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loadout")
public class LoadoutController {

    private final LoadoutService loadoutService;
    private final UtilsService utilsService;

    public LoadoutController(LoadoutService loadoutService, UtilsService utilsService) {
        this.loadoutService = loadoutService;
        this.utilsService = utilsService;
    }

    @GetMapping("/{gameRoomId}")
    public List<SpriteInfos> findSpriteInfosByPlayerAndRoom(@PathVariable Long gameRoomId) {
        return loadoutService.findSpriteInfosByPlayerAndRoom(gameRoomId);
    }

    @GetMapping("/{gameRoomId}/opponents")
    public LoadoutUpdateDto loadOpponentStatus(@PathVariable Long gameRoomId) {
        return loadoutService.loadOpponentStatus(gameRoomId, utilsService.getPlayer());
    }

    @PostMapping("/{gameRoomId}/select/{spriteName}")
    public ResponseEntity<PlayerLoadoutDto> selectUnit(
            @PathVariable Long gameRoomId,
            @PathVariable String spriteName) {
        PlayerLoadoutDto loadout = loadoutService.selectUnit(gameRoomId, utilsService.getPlayer(), spriteName);
        return ResponseEntity.ok(loadout);
    }

    @PostMapping("/{gameRoomId}/lock")
    public ResponseEntity<Void> lockLoadout(@PathVariable Long gameRoomId) {
        loadoutService.lockLoadout(gameRoomId, utilsService.getPlayer());
        return ResponseEntity.ok().build();
    }
}