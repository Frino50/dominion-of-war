package dow.controller;

import dow.model.dto.LoadoutUpdateDto;
import dow.model.dto.SpriteInfos;
import dow.service.LoadoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loadout")
public class LoadoutController {

    private final LoadoutService loadoutService;

    public LoadoutController(LoadoutService loadoutService) {
        this.loadoutService = loadoutService;
    }

    @GetMapping("/{gameRoomId}/opponents")
    public LoadoutUpdateDto getOpponent(@PathVariable Long gameRoomId) {
        return loadoutService.getOpponent(gameRoomId);
    }

    @PostMapping("/{gameRoomId}/select/{spriteName}")
    public ResponseEntity<SpriteInfos> selectUnit(
            @PathVariable Long gameRoomId,
            @PathVariable String spriteName) {
        return ResponseEntity.ok(loadoutService.selectUnit(gameRoomId, spriteName));
    }

    @PostMapping("/{gameRoomId}/lock")
    public ResponseEntity<Void> lockLoadout(@PathVariable Long gameRoomId) {
        loadoutService.lockLoadout(gameRoomId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{gameRoomId}/my-loadout")
    public List<SpriteInfos> getMyLoadout(@PathVariable Long gameRoomId) {
        return loadoutService.getMyLoadout(gameRoomId);
    }
}