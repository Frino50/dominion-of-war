package dow.controller;

import dow.service.GameStateService;
import dow.service.UtilsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fight")
public class FightController {

    private final GameStateService gameStateService;
    private final UtilsService utilsService;

    public FightController(GameStateService gameStateService, UtilsService utilsService) {
        this.gameStateService = gameStateService;
        this.utilsService = utilsService;
    }

    @PostMapping("/{gameRoomId}/spawn/{spriteName}")
    public ResponseEntity<Void> spawnUnit(@PathVariable Long gameRoomId,
                                          @PathVariable String spriteName) {
        gameStateService.spawnUnit(gameRoomId, utilsService.getPlayer().getPseudo(), spriteName);
        return ResponseEntity.ok().build();
    }
}