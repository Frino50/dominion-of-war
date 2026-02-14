package dow.model.dto;

public class LoadoutUpdateDto {
    private Long playerId;
    private String playerPseudo;
    private int unitsSelected; // Nombre d'unités sélectionnées (1-5)
    private boolean isLocked;

    public LoadoutUpdateDto() {
    }

    public LoadoutUpdateDto(Long playerId, String playerPseudo, int unitsSelected, boolean isLocked) {
        this.playerId = playerId;
        this.playerPseudo = playerPseudo;
        this.unitsSelected = unitsSelected;
        this.isLocked = isLocked;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerPseudo() {
        return playerPseudo;
    }

    public void setPlayerPseudo(String playerPseudo) {
        this.playerPseudo = playerPseudo;
    }

    public int getUnitsSelected() {
        return unitsSelected;
    }

    public void setUnitsSelected(int unitsSelected) {
        this.unitsSelected = unitsSelected;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}