package perso.arcade.model.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "player")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String pseudo;

    @Column(nullable = false)
    private String password;

    public Player() {
    }

    public Player(Long id, String pseudo, String hashedPassword) {
        this.id = id;
        this.pseudo = pseudo;
        this.password = hashedPassword;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(id, player.id) && Objects.equals(pseudo, player.pseudo) && Objects.equals(password, player.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pseudo, password);
    }
}