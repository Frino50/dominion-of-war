package dow.repository;

import dow.model.entities.UnitStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitStatsRepository extends JpaRepository<UnitStats, Long> {
    Optional<UnitStats> findBySpriteId(Long spriteId);
}