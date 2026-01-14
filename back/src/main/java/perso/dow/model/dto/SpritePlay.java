package perso.dow.model.dto;

public class SpritePlay {
    private SpriteInfos idle;
    private SpriteInfos walk;
    private SpriteInfos attack;

    public SpritePlay(SpriteInfos idle, SpriteInfos walk, SpriteInfos attack) {
        this.idle = idle;
        this.walk = walk;
        this.attack = attack;
    }

    public SpritePlay() {
    }

    public SpriteInfos getIdle() {
        return idle;
    }

    public void setIdle(SpriteInfos idle) {
        this.idle = idle;
    }

    public SpriteInfos getWalk() {
        return walk;
    }

    public void setWalk(SpriteInfos walk) {
        this.walk = walk;
    }

    public SpriteInfos getAttack() {
        return attack;
    }

    public void setAttack(SpriteInfos attack) {
        this.attack = attack;
    }
}
