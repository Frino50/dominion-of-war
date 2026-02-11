package dow.repository;

import dow.model.dto.UnitStatsDto;
import dow.model.entities.Sprite;
import dow.model.entities.UnitStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitStatsRepository extends JpaRepository<UnitStats, Long> {

    /**
     * Recherche les statistiques d'unité par sprite
     */
    Optional<UnitStats> findBySprite(Sprite sprite);

    @Query("SELECT new dow.model.dto.UnitStatsDto(" +
            "s.name, " +
            "COALESCE(us.health, 0), " +
            "COALESCE(us.attack, 0), " +
            "COALESCE(us.attackSpeed, 0.0), " +
            "COALESCE(us.moveSpeed, 0.0), " +
            "COALESCE(us.attackRange, 0), " +
            "COALESCE(us.cost, 0), " +
            "COALESCE(us.cooldown, 0)) " +
            "FROM Sprite s " +
            "LEFT JOIN UnitStats us ON us.sprite = s " +
            "WHERE s.name = :spriteName")
    UnitStatsDto getUnitStatsDtoBySpriteName(@Param("spriteName") String spriteName);
}
