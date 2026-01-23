package dow.repository;

import dow.model.dto.SpriteInfos;
import dow.model.entities.Sprite;
import dow.model.enumeration.AnimationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpriteRepository extends JpaRepository<Sprite, Long> {

    void deleteByName(String name);

    Optional<Sprite> findByName(String name);

    @Query("""
            SELECT new dow.model.dto.SpriteInfos(
                    a.id,
                    s.name,
                    CONCAT(s.name, '/', a.type, '/', a.indice, '.png'),
                    a.width,
                    a.height,
                    a.frames,
                    s.scale,
                    a.frameRate,
                    a.hitboxX,
                    a.hitboxY,
                    a.hitboxWidth,
                    a.hitboxHeight
            )
            FROM Sprite s
            JOIN s.animations a
            WHERE a.type = :animationType AND s.name = :spriteName
            """)
    SpriteInfos findSpriteInfosByTypeAndName(
            @Param("animationType") AnimationType animationType,
            @Param("spriteName") String spriteName
    );

    @Query("""
            SELECT new dow.model.dto.SpriteInfos(
                    a.id,
                    s.name,
                    CONCAT(s.name, '/', a.type, '/', a.indice, '.png'),
                    a.width,
                    a.height,
                    a.frames,
                    s.scale,
                    a.frameRate,
                    a.hitboxX,
                    a.hitboxY,
                    a.hitboxWidth,
                    a.hitboxHeight
            )
            FROM Sprite s
            JOIN s.animations a
            WHERE a.type = :animationType
            """)
    List<SpriteInfos> findAllSpriteInfosByAnimationType(
            @Param("animationType") AnimationType animationType
    );

    @Query("""
            SELECT new dow.model.dto.SpriteInfos(
                    a.id,
                    s.name,
                    CONCAT(s.name, '/', a.type, '/', a.indice, '.png'),
                    a.width,
                    a.height,
                    a.frames,
                    s.scale,
                    a.frameRate,
                    a.hitboxX,
                    a.hitboxY,
                    a.hitboxWidth,
                    a.hitboxHeight
            )
            FROM Sprite s
            JOIN s.animations a
            WHERE s.name = :spriteName
            ORDER BY a.id
            """)
    List<SpriteInfos> findAllAnimationsBySpriteName(@Param("spriteName") String spriteName);

    @Query("""
            SELECT new dow.model.dto.SpriteInfos(
                    a.id,
                    s.name,
                    CONCAT(s.name, '/', a.type, '/', a.indice, '.png'),
                    a.width,
                    a.height,
                    a.frames,
                    s.scale,
                    a.frameRate,
                    a.hitboxX,
                    a.hitboxY,
                    a.hitboxWidth,
                    a.hitboxHeight
            )
            FROM Sprite s
            JOIN s.animations a
            WHERE a.id = :animationId
            """)
    SpriteInfos findSpriteInfosByAnimationId(@Param("animationId") Long animationId);
}