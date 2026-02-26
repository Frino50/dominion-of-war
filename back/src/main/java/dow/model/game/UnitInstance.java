package dow.model.game;

public class UnitInstance {
    private final String id;
    private final String spriteName;
    private final String ownerPseudo;
    private final double speed;
    private double x;

    public UnitInstance(String string, String spriteName, String ownerPseudo, double x, double v1) {
        this.id = string;
        this.spriteName = spriteName;
        this.ownerPseudo = ownerPseudo;
        this.x = x;
        this.speed = v1;
    }


    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getSpeed() {
        return speed;
    }

    public String getOwnerPseudo() {
        return ownerPseudo;
    }

    public String getSpriteName() {
        return spriteName;
    }
}