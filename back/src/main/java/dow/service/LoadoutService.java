package dow.service;

import dow.model.dto.LoadoutUpdateDto;
import dow.model.dto.SpriteInfos;
import dow.model.entities.*;
import dow.model.enumeration.AnimationType;
import dow.model.enumeration.GameStatus;
import dow.model.enumeration.ParticipantRole;
import dow.repository.GameParticipantRepository;
import dow.repository.GameRoomRepository;
import dow.repository.PlayerLoadoutRepository;
import dow.repository.SpriteRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class LoadoutService {

    private final PlayerLoadoutRepository loadoutRepository;
    private final GameRoomRepository gameRoomRepository;
    private final GameParticipantRepository participantRepository;
    private final SpriteRepository spriteRepository;
    private final GameStateService gameStateService;
    private final SimpMessagingTemplate messagingTemplate;

    public LoadoutService(PlayerLoadoutRepository loadoutRepository,
                          GameRoomRepository gameRoomRepository,
                          SpriteRepository spriteRepository,
                          GameStateService gameStateService,
                          SimpMessagingTemplate messagingTemplate,
                          GameParticipantRepository participantRepository) {
        this.loadoutRepository = loadoutRepository;
        this.gameRoomRepository = gameRoomRepository;
        this.spriteRepository = spriteRepository;
        this.gameStateService = gameStateService;
        this.messagingTemplate = messagingTemplate;
        this.participantRepository = participantRepository;
    }

    @Transactional(readOnly = true)
    public LoadoutUpdateDto getOpponent(Long gameRoomId, Player currentPlayer) {
        return loadoutRepository.getOpponent(gameRoomId, currentPlayer.getId());
    }

    @Transactional
    public SpriteInfos selectUnit(Long gameRoomId, Player player, String spriteName) {
        PlayerLoadout loadout = getOrCreateLoadoutEntity(gameRoomId, player);

        if (loadout.isLocked()) throw new RuntimeException("Temps écoulé");

        if (loadout.hasSprite(spriteName)) {
            loadout.removeSprite(spriteName);
            loadoutRepository.save(loadout);
            notifyLoadoutUpdate(loadout.getGameRoom().getId(), player, loadout);
            return null;
        }

        Sprite sprite = spriteRepository.findByName(spriteName)
                .orElseThrow(() -> new RuntimeException("Unité introuvable: " + spriteName));

        if (loadout.isComplete()) throw new RuntimeException("Loadout déjà plein");

        loadout.addSprite(sprite);
        loadoutRepository.save(loadout);
        notifyLoadoutUpdate(loadout.getGameRoom().getId(), player, loadout);

        return spriteRepository.findSpriteInfosByName(spriteName, AnimationType.IDLE);
    }

    private PlayerLoadout getOrCreateLoadoutEntity(Long gameRoomId, Player player) {
        GameRoom room = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new RuntimeException("Room introuvable"));
        return loadoutRepository.findByGameRoomAndPlayer(room, player)
                .orElseGet(() -> loadoutRepository.save(new PlayerLoadout(room, player)));
    }

    @Transactional
    public void lockLoadout(Long gameRoomId, Player player) {
        PlayerLoadout loadout = loadoutRepository.findByGameRoomAndPlayer(
                gameRoomRepository.getReferenceById(gameRoomId), player
        ).orElseThrow(() -> new RuntimeException("Loadout introuvable"));

        if (!loadout.isComplete()) throw new RuntimeException("Loadout incomplet");

        loadout.setLocked(true);
        loadoutRepository.save(loadout);

        notifyLoadoutUpdate(gameRoomId, player, loadout);

        if (loadoutRepository.areAllPlayersLocked(gameRoomId)) {
            startGame(loadout.getGameRoom());
        }
    }

    @Transactional
    public void autoCompleteLoadouts(Long gameRoomId) {
        GameRoom gameRoom = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new RuntimeException("Room introuvable"));

        List<Sprite> allSprites = spriteRepository.findAll();
        if (allSprites.isEmpty()) return;

        Random random = new Random();

        for (GameParticipant participant : gameRoom.getParticipants()) {
            if (!participant.isPlayer()) continue;

            PlayerLoadout loadout = loadoutRepository.findByGameRoomAndPlayer(gameRoom, participant.getPlayer())
                    .orElseGet(() -> new PlayerLoadout(gameRoom, participant.getPlayer()));

            if (loadout.isLocked() && loadout.isComplete()) continue;

            List<Sprite> candidates = new ArrayList<>(allSprites);
            candidates.removeIf(s -> loadout.hasSprite(s.getName()));

            while (!loadout.isComplete() && !candidates.isEmpty()) {
                loadout.addSprite(candidates.remove(random.nextInt(candidates.size())));
            }

            loadout.setLocked(true);
            loadoutRepository.save(loadout);
        }

        startGame(gameRoom);
    }


    private void notifyLoadoutUpdate(Long roomId, Player player, PlayerLoadout loadout) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + roomId + "/" + player.getPseudo() + "/loadout",
                new LoadoutUpdateDto(player.getPseudo(), loadout.countSprites(), loadout.isLocked())
        );
    }

    private void startGame(GameRoom gameRoom) {
        if (gameRoom.getStatus() == GameStatus.IN_PROGRESS) return;

        String p1 = participantRepository.findPseudoByGameRoomIdAndRole(gameRoom.getId(), ParticipantRole.PLAYER_1);
        String p2 = participantRepository.findPseudoByGameRoomIdAndRole(gameRoom.getId(), ParticipantRole.PLAYER_2);

        gameRoom.setStatus(GameStatus.IN_PROGRESS);
        gameRoom.setStartedAt(LocalDateTime.now());
        gameRoomRepository.save(gameRoom);
        gameStateService.initGame(gameRoom, p1, p2);
        messagingTemplate.convertAndSend("/topic/game/" + gameRoom.getId() + "/phase", "{\"phase\":\"FIGHT\"}");
    }

    @Transactional(readOnly = true)
    public List<SpriteInfos> getMyLoadout(Long gameRoomId, Player player) {
        return loadoutRepository.findMyLoadoutSpriteInfos(gameRoomId, player.getId());
    }
}