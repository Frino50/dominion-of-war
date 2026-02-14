package dow.model.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "player_loadout")
public class PlayerLoadout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_room_id", nullable = false)
    private GameRoom gameRoom;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne
    @JoinColumn(name = "sprite_1_id")
    private Sprite sprite1;

    @ManyToOne
    @JoinColumn(name = "sprite_2_id")
    private Sprite sprite2;

    @ManyToOne
    @JoinColumn(name = "sprite_3_id")
    private Sprite sprite3;

    @ManyToOne
    @JoinColumn(name = "sprite_4_id")
    private Sprite sprite4;

    @ManyToOne
    @JoinColumn(name = "sprite_5_id")
    private Sprite sprite5;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;

    public PlayerLoadout() {
    }

    public PlayerLoadout(GameRoom gameRoom, Player player) {
        this.gameRoom = gameRoom;
        this.player = player;
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

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Sprite getSprite1() {
        return sprite1;
    }

    public void setSprite1(Sprite sprite1) {
        this.sprite1 = sprite1;
    }

    public Sprite getSprite2() {
        return sprite2;
    }

    public void setSprite2(Sprite sprite2) {
        this.sprite2 = sprite2;
    }

    public Sprite getSprite3() {
        return sprite3;
    }

    public void setSprite3(Sprite sprite3) {
        this.sprite3 = sprite3;
    }

    public Sprite getSprite4() {
        return sprite4;
    }

    public void setSprite4(Sprite sprite4) {
        this.sprite4 = sprite4;
    }

    public Sprite getSprite5() {
        return sprite5;
    }

    public void setSprite5(Sprite sprite5) {
        this.sprite5 = sprite5;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isComplete() {
        return sprite1 != null && sprite2 != null && sprite3 != null &&
                sprite4 != null && sprite5 != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlayerLoadout that = (PlayerLoadout) o;
        return locked == that.locked &&
                Objects.equals(id, that.id) &&
                Objects.equals(gameRoom, that.gameRoom) &&
                Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gameRoom, player, locked);
    }
}