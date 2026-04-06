package dow.service;

import dow.model.entities.GameRoom;
import dow.model.game.GameState;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameStateService {

    private final Map<Long, GameState> activeGames = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final UtilsService utilsService;

    public GameStateService(SimpMessagingTemplate messagingTemplate, UtilsService utilsService) {
        this.messagingTemplate = messagingTemplate;
        this.utilsService = utilsService;
    }

    public void initGame(GameRoom gameRoom, String p1Pseudo, String p2Pseudo) {
        GameState state = new GameState(gameRoom.getId(), p1Pseudo, p2Pseudo);
        activeGames.put(gameRoom.getId(), state);
    }

    public void spawnUnit(Long gameRoomId, String spriteName) {
        String ownerPseudo = utilsService.getPseudo();
        GameState state = activeGames.get(gameRoomId);
        if (state == null) return;
        boolean isPlayer1 = ownerPseudo.equals(state.getPlayer1Pseudo());
        state.spawnUnit(ownerPseudo, spriteName, isPlayer1);
    }

    /**
     * Tick global : exécuté toutes les 100ms.
     * Chaque partie est traitée en parallèle pour éviter qu'un grand nombre
     * de parties ne bloque le thread schedulé.
     */
    @Scheduled(fixedRate = 100)
    public void tick() {
        activeGames.entrySet().parallelStream().forEach(entry -> {
            Long gameRoomId = entry.getKey();
            GameState state = entry.getValue();

            state.tick();

            messagingTemplate.convertAndSend(
                    "/topic/game/" + gameRoomId + "/state",
                    state.getUnits()
            );
        });
    }

    public void removeGame(Long gameRoomId) {
        activeGames.remove(gameRoomId);
    }
}