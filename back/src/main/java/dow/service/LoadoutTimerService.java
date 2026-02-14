package dow.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.Duration.between;

@Service
public class LoadoutTimerService {

    private static final int SELECTION_TIMEOUT_SECONDS = 60;
    private final LoadoutService loadoutService;
    private final Map<Long, LocalDateTime> gameRoomTimers = new ConcurrentHashMap<>();

    public LoadoutTimerService(LoadoutService loadoutService) {
        this.loadoutService = loadoutService;
    }

    public void startTimer(Long gameRoomId) {
        gameRoomTimers.put(gameRoomId, LocalDateTime.now());
    }

    @Scheduled(fixedRate = 1000) // Vérifie toutes les secondes
    public void checkTimers() {
        LocalDateTime now = LocalDateTime.now();

        gameRoomTimers.entrySet().removeIf(entry -> {
            Long gameRoomId = entry.getKey();
            LocalDateTime startTime = entry.getValue();

            long secondsElapsed = between(startTime, now).getSeconds();

            if (secondsElapsed >= SELECTION_TIMEOUT_SECONDS) {
                // Le temps est écoulé, compléter automatiquement
                try {
                    loadoutService.autoCompleteLoadouts(gameRoomId);
                } catch (Exception e) {
                    // Log l'erreur mais continue
                    System.err.println("Error auto-completing loadouts for room " + gameRoomId + ": " + e.getMessage());
                }
                return true; // Retirer du map
            }

            return false;
        });
    }

    public int getRemainingTime(Long gameRoomId) {
        LocalDateTime startTime = gameRoomTimers.get(gameRoomId);
        if (startTime == null) {
            return 0;
        }

        long secondsElapsed = between(startTime, LocalDateTime.now()).getSeconds();
        int remaining = SELECTION_TIMEOUT_SECONDS - (int) secondsElapsed;
        return Math.max(0, remaining);
    }
}