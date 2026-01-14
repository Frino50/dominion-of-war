package perso.dow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import perso.dow.model.entities.Animation;

@Repository
public interface AnimationRepository extends JpaRepository<Animation, Long> {
}
