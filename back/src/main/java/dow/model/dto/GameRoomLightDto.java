package dow.model.dto;

public class GameRoomLightDto {
    private String name;
    private String password; // Peut être null

    public GameRoomLightDto() {
    }

    public GameRoomLightDto(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}