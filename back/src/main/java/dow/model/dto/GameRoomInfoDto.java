package dow.model.dto;

import dow.model.enumeration.GameStatus;

public class GameRoomInfoDto {
    private Long id;
    private String name;
    private boolean hasPassword;
    private GameStatus status;
    private int playerCount;
    private int spectatorCount;

    public GameRoomInfoDto(Long id, String name, boolean hasPassword, GameStatus status,
                           int playerCount, int spectatorCount) {
        this.id = id;
        this.name = name;
        this.hasPassword = hasPassword;
        this.status = status;
        this.playerCount = playerCount;
        this.spectatorCount = spectatorCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public void setHasPassword(boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getSpectatorCount() {
        return spectatorCount;
    }

    public void setSpectatorCount(int spectatorCount) {
        this.spectatorCount = spectatorCount;
    }
}