package dow.service;

import dow.model.dto.UnitStatsDto;
import dow.model.entities.Sprite;
import dow.model.entities.UnitStats;
import dow.repository.SpriteRepository;
import dow.repository.UnitStatsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnitStatsService {

    private final UnitStatsRepository unitStatsRepository;
    private final SpriteRepository spriteRepository;

    public UnitStatsService(UnitStatsRepository unitStatsRepository,
                            SpriteRepository spriteRepository) {
        this.unitStatsRepository = unitStatsRepository;
        this.spriteRepository = spriteRepository;
    }

    public UnitStatsDto getUnitStatsBySpriteName(String spriteName) {
        return unitStatsRepository.getUnitStatsDtoBySpriteName(spriteName);
    }

    public UnitStatsDto createOrUpdateUnitStats(UnitStatsDto unitStatsDto) {
        Sprite sprite = spriteRepository.findByName(unitStatsDto.getSpriteName())
                .orElseThrow(() -> new EntityNotFoundException("Sprite non trouvé : " + unitStatsDto.getSpriteName()));

        UnitStats unitStats = unitStatsRepository.findBySprite(sprite)
                .orElse(new UnitStats());

        unitStats.setSprite(sprite);
        unitStats.setHealth(Math.max(0, unitStatsDto.getHealth()));
        unitStats.setAttack(Math.max(0, unitStatsDto.getAttack()));
        unitStats.setAttackSpeed(unitStatsDto.getAttackSpeed() > 0 ? unitStatsDto.getAttackSpeed() : 0.0);
        unitStats.setMoveSpeed(unitStatsDto.getMoveSpeed() > 0 ? unitStatsDto.getMoveSpeed() : 0.0);
        unitStats.setAttackRange(Math.max(0, unitStatsDto.getAttackRange()));
        unitStats.setCost(Math.max(0, unitStatsDto.getCost()));
        unitStats.setCooldown(Math.max(0, unitStatsDto.getCooldown()));

        UnitStats savedStats = unitStatsRepository.save(unitStats);

        unitStatsDto.setId(savedStats.getId());
        return unitStatsDto;
    }
}
