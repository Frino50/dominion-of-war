package dow.model.dto;

public class SpriteInfos {
    private final Long animationId;
    private String name;
    private String imageUrl;
    private int width;
    private int height;
    private int frames;
    private float scale;
    private int frameRate;
    private Integer hitboxX;
    private Integer hitboxY;
    private Integer hitboxWidth;
    private Integer hitboxHeight;

    public SpriteInfos(Long animationId, String name, String imageUrl, int width, int height, int frames, float scale, int frameRate, Integer hitboxX, Integer hitboxY, Integer hitboxWidth, Integer hitboxHeight) {
        this.animationId = animationId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.width = width;
        this.height = height;
        this.frames = frames;
        this.scale = scale;
        this.frameRate = frameRate;
        this.hitboxX = hitboxX;
        this.hitboxY = hitboxY;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
    }

    public Long getAnimationId() {
        return animationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public int getFrames() {
        return frames;
    }

    public void setFrames(int frames) {
        this.frames = frames;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
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
}