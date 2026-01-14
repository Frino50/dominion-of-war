package dow.repository;

import dow.model.dto.SpriteInfos;
import dow.model.enumeration.AnimationType;

import java.util.List;

public interface SpriteRepositoryCustom {
    List<SpriteInfos> getAllSpritesInfos(AnimationType idleType);

    SpriteInfos getSpritesInfosByTypeAndName(AnimationType idleType, String spriteName);

    SpriteInfos getSpriteInfosByAnimationId(Long animationId);

    List<SpriteInfos> getAllAnimationsBySpriteName(String spriteName);
}