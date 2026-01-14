package perso.dow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import perso.dow.model.entities.Sprite;

import java.util.Optional;

@Repository
public interface SpriteRepository extends JpaRepository<Sprite, Long>, SpriteRepositoryCustom {
    void deleteByName(String name);

    Optional<Sprite> findByName(String name);
}
