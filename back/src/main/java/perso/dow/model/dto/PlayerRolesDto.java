package perso.dow.model.dto;

import java.util.List;

public class PlayerRolesDto {
    private Long id;
    private String pseudo;
    private List<String> roleNames;

    public PlayerRolesDto() {
    }

    public PlayerRolesDto(Long id, String pseudo, List<String> roleNames) {
        this.id = id;
        this.pseudo = pseudo;
        this.roleNames = roleNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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