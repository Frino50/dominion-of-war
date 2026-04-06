package dow.model.dto;

import java.util.List;
import java.util.UUID;

public class PlayerRolesDto {
    private UUID id;
    private String pseudo;
    private List<String> roleNames;

    public PlayerRolesDto() {
    }

    public PlayerRolesDto(UUID id, String pseudo, List<String> roleNames) {
        this.id = id;
        this.pseudo = pseudo;
        this.roleNames = roleNames;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }
}