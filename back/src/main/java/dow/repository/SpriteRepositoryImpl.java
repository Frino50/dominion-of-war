package dow.repository;

import dow.model.dto.SpriteInfos;
import dow.model.enumeration.AnimationType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SpriteRepositoryImpl implements SpriteRepositoryCustom {

    private static final String BASE_SELECT = """
            SELECT new dow.model.dto.SpriteInfos(
                    a.id,
                    s.name,
                    s.name || '/' || a.type || '/' || a.indice || '.png',
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
            """;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SpriteInfos getSpritesInfosByTypeAndName(AnimationType idleType, String spriteName) {
        TypedQuery<SpriteInfos> query = entityManager.createQuery(
                BASE_SELECT + "WHERE a.type = :idleType AND s.name = :spriteName", SpriteInfos.class);
        query.setParameter("idleType", idleType);
        query.setParameter("spriteName", spriteName);
        return query.getSingleResult();
    }

    @Override
    public List<SpriteInfos> getAllSpritesInfos(AnimationType idleType) {
        return getList("WHERE a.type = :idleType", "idleType", idleType);
    }

    @Override
    public List<SpriteInfos> getAllAnimationsBySpriteName(String spriteName) {
        return getList("WHERE s.name = :spriteName ORDER BY a.id", "spriteName", spriteName);
    }

    @Override
    public SpriteInfos getSpriteInfosByAnimationId(Long animationId) {
        return getSingle("WHERE a.id = :animationId", "animationId", animationId);
    }

    private List<SpriteInfos> getList(String whereClause, String paramName, Object paramValue) {
        TypedQuery<SpriteInfos> query = entityManager.createQuery(BASE_SELECT + whereClause, SpriteInfos.class);
        query.setParameter(paramName, paramValue);
        return query.getResultList();
    }

    private SpriteInfos getSingle(String whereClause, String paramName, Object paramValue) {
        TypedQuery<SpriteInfos> query = entityManager.createQuery(BASE_SELECT + whereClause, SpriteInfos.class);
        query.setParameter(paramName, paramValue);
        return query.getSingleResult();
    }
}