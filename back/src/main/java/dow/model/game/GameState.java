package dow.model.game;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameState {

    private final Long gameRoomId;
    private final String player1Pseudo;
    private final String player2Pseudo;

    private final List<UnitInstance> units = new CopyOnWriteArrayList<>();

    public GameState(Long gameRoomId, String player1Pseudo, String player2Pseudo) {
        this.gameRoomId = gameRoomId;
        this.player1Pseudo = player1Pseudo;
        this.player2Pseudo = player2Pseudo;
    }

    public synchronized void spawnUnit(String ownerPseudo, String spriteName, boolean isPlayer1) {
        units.add(new UnitInstance(
                UUID.randomUUID().toString(),
                spriteName,
                ownerPseudo,
                isPlayer1 ? 0.0 : 100.0,  // position initiale en % de l'écran
                isPlayer1 ? 1.0 : -1.0    // vitesse : positif = droite, négatif = gauche
        ));
    }

    public synchronized void tick() {
        units.forEach(u -> u.setX(u.getX() + u.getSpeed()));
        units.removeIf(u -> u.getX() < 0 || u.getX() > 100);
    }

    public Long getGameRoomId() {
        return gameRoomId;
    }

    public String getPlayer1Pseudo() {
        return player1Pseudo;
    }

    public String getPlayer2Pseudo() {
        return player2Pseudo;
    }

    public List<UnitInstance> getUnits() {
        return units;
    }
}