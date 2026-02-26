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

    @Scheduled(fixedRate = 1000)
    public void checkTimers() {
        LocalDateTime now = LocalDateTime.now();

        gameRoomTimers.entrySet().removeIf(entry -> {
            Long gameRoomId = entry.getKey();
            LocalDateTime startTime = entry.getValue();

            long secondsElapsed = between(startTime, now).getSeconds();

            if (secondsElapsed >= SELECTION_TIMEOUT_SECONDS) {
                try {
                    loadoutService.autoCompleteLoadouts(gameRoomId);
                } catch (Exception e) {
                    System.err.println("Erreur lors du remplissation automatique d'unités " + gameRoomId + ": " + e.getMessage());
                }
                return true;
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