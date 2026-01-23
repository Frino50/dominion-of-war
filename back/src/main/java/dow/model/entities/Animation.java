package dow.model.entities;

import dow.model.enumeration.AnimationType;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Animation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "frames", nullable = false)
    private int frames;

    @Column(name = "width", nullable = false)
    private int width;

    @Column(name = "height", nullable = false)
    private int height;

    @Column(name = "indice", nullable = false)
    private int indice;

    @Column(name = "frame_rate", nullable = false)
    private int frameRate;

    @Column(name = "hitbox_x")
    private Integer hitboxX;

    @Column(name = "hitbox_y")
    private Integer hitboxY;

    @Column(name = "hitbox_width")
    private Integer hitboxWidth;

    @Column(name = "hitbox_height")
    private Integer hitboxHeight;

    @Enumerated(EnumType.STRING)
    private AnimationType type;

    @ManyToOne
    @JoinColumn(name = "sprite_id")
    private Sprite sprite;

    public Animation() {
    }

    public Animation(int frames, int width, int height, AnimationType type, int indice, int frameRate) {
        this.frames = frames;
        this.width = width;
        this.height = height;
        this.type = type;
        this.indice = indice;
        this.frameRate = frameRate;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public int getFrames() {
        return frames;
    }

    public void setFrames(int frames) {
        this.frames = frames;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public AnimationType getType() {
        return type;
    }

    public void setType(AnimationType type) {
        this.type = type;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public int getIndice() {
        return indice;
    }

    public void setIndice(int indice) {
        this.indice = indice;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public Integer getHitboxX() {
        return hitboxX;
    }

    public void setHitboxX(Integer hitboxX) {
        this.hitboxX = hitboxX;
    }

    public Integer getHitboxY() {
        return hitboxY;
    }

    public void setHitboxY(Integer hitboxY) {
        this.hitboxY = hitboxY;
    }

    public Integer getHitboxWidth() {
        return hitboxWidth;
    }

    public void setHitboxWidth(Integer hitboxWidth) {
        this.hitboxWidth = hitboxWidth;
    }

    public Integer getHitboxHeight() {
        return hitboxHeight;
    }

    public void setHitboxHeight(Integer hitboxHeight) {
        this.hitboxHeight = hitboxHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Animation animation = (Animation) o;
        return frames == animation.frames && width == animation.width && height == animation.height && indice == animation.indice && frameRate == animation.frameRate && Objects.equals(id, animation.id) && Objects.equals(hitboxX, animation.hitboxX) && Objects.equals(hitboxY, animation.hitboxY) && Objects.equals(hitboxWidth, animation.hitboxWidth) && Objects.equals(hitboxHeight, animation.hitboxHeight) && type == animation.type && Objects.equals(sprite, animation.sprite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, frames, width, height, indice, frameRate, hitboxX, hitboxY, hitboxWidth, hitboxHeight, type, sprite);
    }
}