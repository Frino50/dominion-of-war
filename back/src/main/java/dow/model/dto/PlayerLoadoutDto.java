package dow.model.dto;

import java.util.List;

public class PlayerLoadoutDto {
    private Long id;
    private Long playerId;
    private List<SpriteInfos> selectedUnits; // Les 5 animations sélectionnées
    private boolean isLocked;

    public PlayerLoadoutDto() {
    }

    public PlayerLoadoutDto(Long id, Long playerId, List<SpriteInfos> selectedUnits, boolean isLocked) {
        this.id = id;
        this.playerId = playerId;
        this.selectedUnits = selectedUnits;
        this.isLocked = isLocked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public List<SpriteInfos> getSelectedUnits() {
        return selectedUnits;
    }

    public void setSelectedUnits(List<SpriteInfos> selectedUnits) {
        this.selectedUnits = selectedUnits;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}