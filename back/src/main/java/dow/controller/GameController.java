package dow.controller;

import dow.model.dto.GameParticipantWaitingDto;
import dow.model.dto.GameRoomInfoDto;
import dow.model.dto.GameRoomLightDto;
import dow.service.GameRoomService;
import dow.service.LoadoutTimerService;
import dow.service.UtilsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameRoomService gameRoomService;
    private final UtilsService utilsService;
    private final LoadoutTimerService loadoutTimerService;

    public GameController(GameRoomService gameRoomService, UtilsService utilsService, LoadoutTimerService loadoutTimerService) {
        this.gameRoomService = gameRoomService;
        this.utilsService = utilsService;
        this.loadoutTimerService = loadoutTimerService;
    }

    @GetMapping("/rooms")
    public List<GameRoomInfoDto> listRooms() {
        return gameRoomService.listAvailableRooms();
    }

    @PostMapping("/create")
    public Long createRoom(@RequestBody GameRoomLightDto dto) {
        return gameRoomService.createGameRoom(dto);
    }

    @PostMapping("/join")
    public void joinRoom(@RequestBody GameRoomLightDto dto) {
        gameRoomService.joinGameRoom(dto, utilsService.getPlayer());
    }

    @GetMapping("/participants/{gameRoomId}")
    public List<GameParticipantWaitingDto> getParticipantsWaitingDto(@PathVariable Long gameRoomId) {
        return gameRoomService.getParticipantsWaitingDto(gameRoomId);
    }

    @PostMapping("/leave/{gameRoomId}")
    public ResponseEntity<Void> leaveRoom(@PathVariable Long gameRoomId) {
        gameRoomService.leaveRoom(gameRoomId, utilsService.getPlayer());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/room/{gameRoomId}")
    public GameRoomLightDto findRoomInfoLightDtoById(@PathVariable Long gameRoomId) {
        return gameRoomService.findRoomLightDtoById(gameRoomId);
    }

    @PostMapping("/start-selection/{gameRoomId}")
    public ResponseEntity<Void> startSelectionPhase(@PathVariable Long gameRoomId) {
        loadoutTimerService.startTimer(gameRoomId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/remaining-time/{gameRoomId}")
    public ResponseEntity<Integer> getRemainingTime(@PathVariable Long gameRoomId) {
        int remainingTime = loadoutTimerService.getRemainingTime(gameRoomId);
        return ResponseEntity.ok(remainingTime);
    }

    @GetMapping("/room-id/{gameStatus}")
    public ResponseEntity<Long> findGameRoomIdByPlayerIdAndStatus(@PathVariable String gameStatus) {
        long roomId = gameRoomService.findGameRoomIdByPlayerIdAndStatus(gameStatus);
        return ResponseEntity.ok(roomId);
    }
}