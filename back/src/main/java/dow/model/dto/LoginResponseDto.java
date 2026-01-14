package dow.model.dto;

public class LoginResponseDto {
    private String token;
    private String pseudo;

    public LoginResponseDto(String token, String pseudo) {
        this.token = token;
        this.pseudo = pseudo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
}
