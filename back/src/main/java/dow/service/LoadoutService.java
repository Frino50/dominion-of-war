package dow.service;

import dow.model.dto.LoadoutUpdateDto;
import dow.model.dto.PlayerLoadoutDto;
import dow.model.dto.SpriteInfos;
import dow.model.entities.*;
import dow.model.enumeration.AnimationType;
import dow.model.enumeration.GameStatus;
import dow.repository.GameRoomRepository;
import dow.repository.PlayerLoadoutRepository;
import dow.repository.SpriteRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
public class LoadoutService {

    private final PlayerLoadoutRepository loadoutRepository;
    private final GameRoomRepository gameRoomRepository;
    private final SpriteRepository spriteRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public LoadoutService(PlayerLoadoutRepository loadoutRepository,
                          GameRoomRepository gameRoomRepository,
                          SpriteRepository spriteRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.loadoutRepository = loadoutRepository;
        this.gameRoomRepository = gameRoomRepository;
        this.spriteRepository = spriteRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public PlayerLoadoutDto getPlayerLoadout(Long gameRoomId, Player player) {
        PlayerLoadout loadout = getOrCreateLoadoutEntity(gameRoomId, player);
        return buildFullLoadoutDto(loadout);
    }

    @Transactional(readOnly = true)
    public List<LoadoutUpdateDto> findOpponentsStatus(Long gameRoomId, Player currentPlayer) {
        // 1 seule requête SQL au lieu de N boucles
        return loadoutRepository.findOpponentsStatus(gameRoomId, currentPlayer.getId());
    }

    @Transactional
    public PlayerLoadoutDto selectUnit(Long gameRoomId, Player player, String spriteName) {
        PlayerLoadout loadout = getOrCreateLoadoutEntity(gameRoomId, player);

        if (loadout.isLocked()) throw new RuntimeException("Temps écoulé");
        if (hasSprite(loadout, spriteName)) throw new RuntimeException("Unité déjà sélectionnée");

        Sprite sprite = spriteRepository.findByName(spriteName)
                .orElseThrow(() -> new RuntimeException("Unité introuvable: " + spriteName));

        // Remplissage intelligent du premier slot vide
        fillFirstEmptySlot(loadout, sprite);

        loadoutRepository.save(loadout);
        notifyLoadoutUpdate(loadout.getGameRoom().getId(), player, loadout);

        return buildFullLoadoutDto(loadout);
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

            // Remplir les slots vides
            List<Sprite> candidates = new ArrayList<>(allSprites);
            // On retire ceux déjà présents pour éviter les doublons
            candidates.removeIf(s -> hasSprite(loadout, s.getName()));

            while (!loadout.isComplete() && !candidates.isEmpty()) {
                Sprite picked = candidates.remove(random.nextInt(candidates.size()));
                fillFirstEmptySlot(loadout, picked);
            }

            loadout.setLocked(true);
            loadoutRepository.save(loadout);
            notifyLoadoutUpdate(gameRoomId, participant.getPlayer(), loadout);
        }

        // Comme tout le monde est forcé à locked, on démarre
        startGame(gameRoom);
    }

    private PlayerLoadout getOrCreateLoadoutEntity(Long gameRoomId, Player player) {
        GameRoom room = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new RuntimeException("Room introuvable"));
        return loadoutRepository.findByGameRoomAndPlayer(room, player)
                .orElseGet(() -> loadoutRepository.save(new PlayerLoadout(room, player)));
    }

    private PlayerLoadoutDto buildFullLoadoutDto(PlayerLoadout loadout) {
        // Collecter les noms non-nulls pour faire 1 seule requête
        List<String> spriteNames = Stream.of(
                loadout.getSprite1(), loadout.getSprite2(), loadout.getSprite3(),
                loadout.getSprite4(), loadout.getSprite5()
        ).filter(Objects::nonNull).map(Sprite::getName).toList();

        List<SpriteInfos> units = Collections.emptyList();
        if (!spriteNames.isEmpty()) {
            // Optimisation : 1 requête SQL pour récupérer toutes les infos d'un coup
            units = spriteRepository.findSpriteInfosByNames(spriteNames, AnimationType.IDLE);
        }

        return new PlayerLoadoutDto(
                loadout.getId(),
                loadout.getPlayer().getId(),
                units, // constructeur à adapter si l'ordre est différent dans votre DTO
                loadout.isLocked()
        );
    }

    private void fillFirstEmptySlot(PlayerLoadout loadout, Sprite sprite) {
        if (loadout.getSprite1() == null) loadout.setSprite1(sprite);
        else if (loadout.getSprite2() == null) loadout.setSprite2(sprite);
        else if (loadout.getSprite3() == null) loadout.setSprite3(sprite);
        else if (loadout.getSprite4() == null) loadout.setSprite4(sprite);
        else if (loadout.getSprite5() == null) loadout.setSprite5(sprite);
        else throw new RuntimeException("Loadout déjà plein");
    }

    private boolean hasSprite(PlayerLoadout l, String name) {
        return Stream.of(l.getSprite1(), l.getSprite2(), l.getSprite3(), l.getSprite4(), l.getSprite5())
                .filter(Objects::nonNull)
                .anyMatch(s -> s.getName().equals(name));
    }

    private int countUnits(PlayerLoadout l) {
        return (int) Stream.of(l.getSprite1(), l.getSprite2(), l.getSprite3(), l.getSprite4(), l.getSprite5())
                .filter(Objects::nonNull).count();
    }

    private void notifyLoadoutUpdate(Long roomId, Player player, PlayerLoadout loadout) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + roomId + "/loadout",
                new LoadoutUpdateDto(player.getId(), player.getPseudo(), countUnits(loadout), loadout.isLocked())
        );
    }

    private void startGame(GameRoom gameRoom) {
        if (gameRoom.getStatus() == GameStatus.IN_PROGRESS) return;

        gameRoom.setStatus(GameStatus.IN_PROGRESS);
        gameRoom.setStartedAt(LocalDateTime.now());
        gameRoomRepository.save(gameRoom);

        messagingTemplate.convertAndSend("/topic/game/" + gameRoom.getId() + "/phase", "{\"phase\":\"FIGHT\"}");
    }
}