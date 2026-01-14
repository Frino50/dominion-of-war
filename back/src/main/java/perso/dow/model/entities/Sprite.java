package perso.dow.model.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Sprite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private float scale;

    @OneToMany(mappedBy = "sprite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Animation> animations = new ArrayList<>();

    public Sprite() {
    }

    public Sprite(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public List<Animation> getAnimations() {
        return animations;
    }

    public void setAnimations(List<Animation> animations) {
        this.animations = animations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void addAnimation(Animation animation) {
        animation.setSprite(this);
        animations.add(animation);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Sprite sprite = (Sprite) o;
        return scale == sprite.scale && Objects.equals(id, sprite.id) && Objects.equals(name, sprite.name) && Objects.equals(animations, sprite.animations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, scale, animations);
    }
}
