package dow.service;

import dow.exception.AlreadyExist;
import dow.exception.GameAlreadyInException;
import dow.exception.GameBadPasswordException;
import dow.exception.GameNotFoundException;
import dow.model.dto.GameParticipantWaitingDto;
import dow.model.dto.GameRoomInfoDto;
import dow.model.dto.GameRoomLightDto;
import dow.model.entities.GameParticipant;
import dow.model.entities.GameRoom;
import dow.model.entities.Player;
import dow.model.entities.PlayerLoadout;
import dow.model.enumeration.GameStatus;
import dow.model.enumeration.ParticipantRole;
import dow.repository.GameParticipantRepository;
import dow.repository.GameRoomRepository;
import dow.repository.PlayerLoadoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GameRoomService {

    private static final Logger log = LoggerFactory.getLogger(GameRoomService.class);
    private static final int SELECTION_DURATION = 60;

    private final GameRoomRepository gameRoomRepository;
    private final GameParticipantRepository participantRepository;
    private final PlayerLoadoutRepository loadoutRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameParticipantRepository gameParticipantRepository;
    private final UtilsService utilsService;

    public GameRoomService(GameRoomRepository gameRoomRepository,
                           GameParticipantRepository participantRepository,
                           PlayerLoadoutRepository loadoutRepository,
                           SimpMessagingTemplate messagingTemplate,
                           GameParticipantRepository gameParticipantRepository, UtilsService utilsService) {
        this.gameRoomRepository = gameRoomRepository;
        this.participantRepository = participantRepository;
        this.loadoutRepository = loadoutRepository;
        this.messagingTemplate = messagingTemplate;
        this.gameParticipantRepository = gameParticipantRepository;
        this.utilsService = utilsService;
    }

    public List<GameRoomInfoDto> listAvailableRooms() {
        List<GameStatus> activeStatuses = Arrays.asList(
                GameStatus.WAITING,
                GameStatus.UNIT_SELECTION,
                GameStatus.IN_PROGRESS
        );

        return gameRoomRepository.listAvailableRooms(activeStatuses);
    }

    @Transactional
    public Long createGameRoom(GameRoomLightDto dto) {
        if (gameRoomRepository.findByName(dto.getName()).isPresent()) {
            throw new AlreadyExist("Nom de partie déjà utilisée");
        }
        Player creator = utilsService.getPlayer();
        GameRoom room = gameRoomRepository.save(new GameRoom(dto.getName(), dto.getPassword()));
        participantRepository.save(new GameParticipant(room, creator, ParticipantRole.PLAYER_1));
        loadoutRepository.save(new PlayerLoadout(room, creator));

        broadcastRoomsUpdate();
        return room.getId();
    }

    @Transactional
    public void joinGameRoom(GameRoomLightDto dto, Player player) {
        GameRoom room = gameRoomRepository.findByName(dto.getName())
                .orElseThrow(() -> new GameNotFoundException("Partie introuvable"));

        if (room.hasPassword() && !room.getPassword().equals(dto.getPassword())) {
            throw new GameBadPasswordException("Mot de passe incorrect");
        }

        if (participantRepository.existsByGameRoomAndPlayer(room, player)) {
            throw new GameAlreadyInException("Déjà dans la partie");
        }

        ParticipantRole role = determineRole(room);

        // Notifier les participants actuels du salon
        messagingTemplate.convertAndSend("/topic/game/" + room.getId() + "/participants", getParticipantsWaitingDto(room.getId()));

        if (role == ParticipantRole.PLAYER_2) {
            loadoutRepository.save(new PlayerLoadout(room, player));

            room.setStatus(GameStatus.UNIT_SELECTION);
            room.setStartedAt(LocalDateTime.now());
            room = gameRoomRepository.save(room);
            participantRepository.save(new GameParticipant(room, utilsService.getPlayer(), ParticipantRole.PLAYER_2));
            broadcastRoomsUpdate();
            messagingTemplate.convertAndSend("/topic/game/" + room.getId() + "/phase",
                    Optional.of(Map.of("phase", "UNIT_SELECTION", "duration", SELECTION_DURATION)));
        }
    }

    @Transactional
    public void leaveRoom(Long gameRoomId, Player player) {
        GameRoom room = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new GameNotFoundException("Partie introuvable"));

        // 1. Supprimer le participant et son loadout
        participantRepository.deleteByGameRoomAndPlayer(room, player);
        loadoutRepository.findByGameRoomAndPlayer(room, player).ifPresent(loadoutRepository::delete);

        // On force la synchronisation avec la BDD pour que le count suivant soit juste
        participantRepository.flush();

        List<GameParticipant> remaining = participantRepository.findByGameRoom(room);

        if (remaining.isEmpty()) {
            // 2. Suppression RÉELLE de la room si vide
            gameRoomRepository.delete(room);
            log.info("Room {} supprimée car vide", gameRoomId);
        } else {
            // 3. Notifier les survivants
            messagingTemplate.convertAndSend("/topic/game/" + gameRoomId + "/participants", getParticipantsWaitingDto(gameRoomId));

            // Si un joueur part pendant la sélection, on repasse en WAITING (manque un joueur)
            if (room.getStatus() == GameStatus.UNIT_SELECTION && remaining.size() < 2) {
                room.setStatus(GameStatus.WAITING);
                gameRoomRepository.save(room);
            }
        }

        // 4. Mettre à jour le lobby global
        broadcastRoomsUpdate();
    }

    private void broadcastRoomsUpdate() {
        messagingTemplate.convertAndSend("/topic/rooms", listAvailableRooms());
    }


    private ParticipantRole determineRole(GameRoom room) {
        long players = participantRepository.countByGameRoomAndRoleIn(room,
                Arrays.asList(ParticipantRole.PLAYER_1, ParticipantRole.PLAYER_2));
        if (players == 0) return ParticipantRole.PLAYER_1;
        if (players == 1) return ParticipantRole.PLAYER_2;
        return ParticipantRole.SPECTATOR;
    }

    public GameRoomLightDto findRoomLightDtoById(Long gameRoomId) {
        return gameRoomRepository.findRoomLightDtoById(gameRoomId);
    }

    public List<GameParticipantWaitingDto> getParticipantsWaitingDto(Long gameRoomId) {
        return gameParticipantRepository.findAllParticipantsByRoomId(gameRoomId);
    }

    public Long findGameRoomIdByPlayerIdAndStatusUnitSelection() {
        return gameParticipantRepository.findGameRoomIdByPlayerIdAndStatusUnitSelection(utilsService.getPlayer().getId());
    }
}