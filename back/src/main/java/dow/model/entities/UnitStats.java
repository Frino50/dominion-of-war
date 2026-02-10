package dow.model.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "unit_stats")
public class UnitStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "sprite_id", nullable = false, unique = true)
    private Sprite sprite;

    @Column(nullable = false)
    private int health;

    @Column(nullable = false)
    private int attack;

    @Column(name = "attack_speed", nullable = false)
    private double attackSpeed;

    @Column(name = "move_speed", nullable = false)
    private double moveSpeed;

    @Column(name = "attack_range", nullable = false)
    private int attackRange;

    @Column(nullable = false)
    private int cost;

    @Column(nullable = false)
    private int cooldown;

    public UnitStats() {
    }

    public UnitStats(Sprite sprite, int health, int attack, double attackSpeed,
                     double moveSpeed, int attackRange, int cost, int cooldown) {
        this.sprite = sprite;
        this.health = health;
        this.attack = attack;
        this.attackSpeed = attackSpeed;
        this.moveSpeed = moveSpeed;
        this.attackRange = attackRange;
        this.cost = cost;
        this.cooldown = cooldown;
    }

    public Long getId() {
        return id;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(double attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public int getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(int attackRange) {
        this.attackRange = attackRange;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UnitStats unitStats = (UnitStats) o;
        return health == unitStats.health &&
                attack == unitStats.attack &&
                Double.compare(attackSpeed, unitStats.attackSpeed) == 0 &&
                Double.compare(moveSpeed, unitStats.moveSpeed) == 0 &&
                attackRange == unitStats.attackRange &&
                cost == unitStats.cost &&
                cooldown == unitStats.cooldown &&
                Objects.equals(id, unitStats.id) &&
                Objects.equals(sprite, unitStats.sprite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sprite, health, attack, attackSpeed,
                moveSpeed, attackRange, cost, cooldown);
    }
}