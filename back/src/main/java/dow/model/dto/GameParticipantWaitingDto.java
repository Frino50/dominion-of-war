package dow.model.dto;

import dow.model.enumeration.ParticipantRole;

public class GameParticipantWaitingDto {
    private String pseudo;
    private ParticipantRole role;

    public GameParticipantWaitingDto(String pseudo, ParticipantRole role) {
        this.pseudo = pseudo;
        this.role = role;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public void setRole(ParticipantRole role) {
        this.role = role;
    }
}
