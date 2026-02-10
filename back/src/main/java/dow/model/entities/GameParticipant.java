package dow.model.entities;

import dow.model.enumeration.ParticipantRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "game_participant")
public class GameParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_room_id", nullable = false)
    private GameRoom gameRoom;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public GameParticipant() {
        this.joinedAt = LocalDateTime.now();
    }

    public GameParticipant(GameRoom gameRoom, Player player, ParticipantRole role) {
        this();
        this.gameRoom = gameRoom;
        this.player = player;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public Player getPlayer() {
        return player;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public void setRole(ParticipantRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public boolean isPlayer() {
        return role == ParticipantRole.PLAYER_1 || role == ParticipantRole.PLAYER_2;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isSpectator() {
        return role == ParticipantRole.SPECTATOR;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GameParticipant that = (GameParticipant) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(gameRoom, that.gameRoom) &&
                Objects.equals(player, that.player) &&
                role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gameRoom, player, role);
    }
}