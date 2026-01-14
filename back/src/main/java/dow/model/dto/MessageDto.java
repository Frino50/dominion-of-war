package dow.model.dto;

import java.time.LocalDateTime;

public class MessageDto {
    private Long id;
    private String player;
    private String content;
    private LocalDateTime timestamp;

    public MessageDto() {
    }

    public MessageDto(Long id, String player, String content, LocalDateTime timestamp) {
        this.id = id;
        this.player = player;
        this.content = content;
        this.timestamp = timestamp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
