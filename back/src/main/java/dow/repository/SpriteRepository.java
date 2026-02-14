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
            WHERE s.name IN :names AND a.type = :animationType
            """)
    List<SpriteInfos> findSpriteInfosByNames(
            @Param("names") List<String> names,
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
            FROM PlayerLoadout pl
            JOIN Animation a ON (
                a.sprite = pl.sprite1 OR
                a.sprite = pl.sprite2 OR
                a.sprite = pl.sprite3 OR
                a.sprite = pl.sprite4 OR
                a.sprite = pl.sprite5
            )
            JOIN a.sprite s
            WHERE pl.player.id = :playerId
              AND pl.gameRoom.id = :gameRoomId
              AND a.type = :animationType
            """)
    List<SpriteInfos> findSpriteInfosByPlayerAndRoom(
            @Param("playerId") Long playerId,
            @Param("gameRoomId") Long gameRoomId,
            @Param("animationType") AnimationType animationType
    );
}