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
    @JoinColumn(name = "unit_1_id")
    private UnitStats unit1;

    @ManyToOne
    @JoinColumn(name = "unit_2_id")
    private UnitStats unit2;

    @ManyToOne
    @JoinColumn(name = "unit_3_id")
    private UnitStats unit3;

    @ManyToOne
    @JoinColumn(name = "unit_4_id")
    private UnitStats unit4;

    @ManyToOne
    @JoinColumn(name = "unit_5_id")
    private UnitStats unit5;

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

    public UnitStats getUnit1() {
        return unit1;
    }

    public void setUnit1(UnitStats unit1) {
        this.unit1 = unit1;
    }

    public UnitStats getUnit2() {
        return unit2;
    }

    public void setUnit2(UnitStats unit2) {
        this.unit2 = unit2;
    }

    public UnitStats getUnit3() {
        return unit3;
    }

    public void setUnit3(UnitStats unit3) {
        this.unit3 = unit3;
    }

    public UnitStats getUnit4() {
        return unit4;
    }

    public void setUnit4(UnitStats unit4) {
        this.unit4 = unit4;
    }

    public UnitStats getUnit5() {
        return unit5;
    }

    public void setUnit5(UnitStats unit5) {
        this.unit5 = unit5;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isComplete() {
        return unit1 != null && unit2 != null && unit3 != null &&
                unit4 != null && unit5 != null;
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