package dow.model.dto;

import java.util.List;
import java.util.UUID;

public class PlayerRolesDto {
    private UUID id;
    private String pseudo;
    private String email;
    private List<String> roleNames;

    public PlayerRolesDto() {
    }

    public PlayerRolesDto(UUID id, String pseudo, String email, List<String> roleNames) {
        this.id = id;
        this.pseudo = pseudo;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }
}