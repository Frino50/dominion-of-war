package dow.model.dto;

public class LoadoutUpdateDto {
    private String playerPseudo;
    private int unitsSelected; // Nombre d'unités sélectionnées (1-5)
    private boolean isLocked;

    public LoadoutUpdateDto() {
    }

    public LoadoutUpdateDto(String playerPseudo, int unitsSelected, boolean isLocked) {
        this.playerPseudo = playerPseudo;
        this.unitsSelected = unitsSelected;
        this.isLocked = isLocked;
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