package dow.service;

import dow.model.dto.LoadoutUpdateDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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
    public LoadoutUpdateDto getOpponent(Long gameRoomId, Player currentPlayer) {
        return loadoutRepository.getOpponent(gameRoomId, currentPlayer.getId());
    }

    @Transactional
    public SpriteInfos selectUnit(Long gameRoomId, Player player, String spriteName) {
        PlayerLoadout loadout = getOrCreateLoadoutEntity(gameRoomId, player);

        if (loadout.isLocked()) throw new RuntimeException("Temps écoulé");

        if (hasSprite(loadout, spriteName)) {
            removeSprite(loadout, spriteName);
            loadoutRepository.save(loadout);
            notifyLoadoutUpdate(loadout.getGameRoom().getId(), player, loadout);
            return null;
        }

        Sprite sprite = spriteRepository.findByName(spriteName)
                .orElseThrow(() -> new RuntimeException("Unité introuvable: " + spriteName));

        fillFirstEmptySlot(loadout, sprite);
        loadoutRepository.save(loadout);
        notifyLoadoutUpdate(loadout.getGameRoom().getId(), player, loadout);

        return spriteRepository.findSpriteInfosByName(spriteName, AnimationType.IDLE);
    }

    private void removeSprite(PlayerLoadout loadout, String spriteName) {
        if (loadout.getSprite1() != null && loadout.getSprite1().getName().equals(spriteName)) loadout.setSprite1(null);
        else if (loadout.getSprite2() != null && loadout.getSprite2().getName().equals(spriteName))
            loadout.setSprite2(null);
        else if (loadout.getSprite3() != null && loadout.getSprite3().getName().equals(spriteName))
            loadout.setSprite3(null);
        else if (loadout.getSprite4() != null && loadout.getSprite4().getName().equals(spriteName))
            loadout.setSprite4(null);
        else if (loadout.getSprite5() != null && loadout.getSprite5().getName().equals(spriteName))
            loadout.setSprite5(null);
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
                "/topic/game/" + roomId + "/" + player.getPseudo() + "/loadout",
                new LoadoutUpdateDto(player.getPseudo(), countUnits(loadout), loadout.isLocked())
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