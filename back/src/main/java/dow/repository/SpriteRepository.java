package dow.repository;

import dow.model.entities.Sprite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpriteRepository extends JpaRepository<Sprite, Long>, SpriteRepositoryCustom {
    void deleteByName(String name);

    Optional<Sprite> findByName(String name);
}
