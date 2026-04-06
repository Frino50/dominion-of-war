package dow.controller;

import dow.service.GameStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fight")
public class FightController {

    private final GameStateService gameStateService;

    public FightController(GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }

    @PostMapping("/{gameRoomId}/spawn/{spriteName}")
    public ResponseEntity<Void> spawnUnit(@PathVariable Long gameRoomId,
                                          @PathVariable String spriteName) {
        gameStateService.spawnUnit(gameRoomId, spriteName);
        return ResponseEntity.ok().build();
    }
}