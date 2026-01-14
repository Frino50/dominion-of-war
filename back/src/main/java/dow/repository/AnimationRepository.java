package dow.repository;

import dow.model.entities.Animation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimationRepository extends JpaRepository<Animation, Long> {
}
