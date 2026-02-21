package dow.model.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany
    @JoinTable(
            name = "player_loadout_sprites",
            joinColumns = @JoinColumn(name = "loadout_id"),
            inverseJoinColumns = @JoinColumn(name = "sprite_id")
    )
    private List<Sprite> sprites = new ArrayList<>();

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;

    public PlayerLoadout() {
    }

    public PlayerLoadout(GameRoom gameRoom, Player player) {
        this.gameRoom = gameRoom;
        this.player = player;
    }

    public boolean isComplete() {
        return sprites.size() == 5;
    }

    public boolean hasSprite(String name) {
        return sprites.stream().anyMatch(s -> s.getName().equals(name));
    }

    public void addSprite(Sprite sprite) {
        sprites.add(sprite);
    }

    public void removeSprite(String name) {
        sprites.removeIf(s -> s.getName().equals(name));
    }

    public int countSprites() {
        return sprites.size();
    }

    public Long getId() {
        return id;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoom g) {
        this.gameRoom = g;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player p) {
        this.player = p;
    }

    public List<Sprite> getSprites() {
        return sprites;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean l) {
        this.locked = l;
    }
}