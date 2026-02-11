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

    /**
     * Récupère les statistiques d'unité par nom de sprite
     */
    public UnitStatsDto getUnitStatsBySpriteName(String spriteName) {
        return unitStatsRepository.getUnitStatsDtoBySpriteName(spriteName);
    }

    public UnitStatsDto createOrUpdateUnitStats(UnitStatsDto unitStatsDto) {
        // 1. Récupérer le Sprite (obligatoire pour la relation OneToOne)
        Sprite sprite = spriteRepository.findByName(unitStatsDto.getSpriteName())
                .orElseThrow(() -> new EntityNotFoundException("Sprite non trouvé : " + unitStatsDto.getSpriteName()));

        // 2. Chercher si des stats existent déjà ou en créer de nouvelles
        UnitStats unitStats = unitStatsRepository.findBySprite(sprite)
                .orElse(new UnitStats());

        // 3. Mapper les données et appliquer la logique de "0 si null"
        // Note : Pour les types primitifs (int/double), ils ne sont jamais null dans le DTO,
        // mais nous appliquons une logique de sécurité.
        unitStats.setSprite(sprite);
        unitStats.setHealth(Math.max(0, unitStatsDto.getHealth()));
        unitStats.setAttack(Math.max(0, unitStatsDto.getAttack()));
        unitStats.setAttackSpeed(unitStatsDto.getAttackSpeed() > 0 ? unitStatsDto.getAttackSpeed() : 0.0);
        unitStats.setMoveSpeed(unitStatsDto.getMoveSpeed() > 0 ? unitStatsDto.getMoveSpeed() : 0.0);
        unitStats.setAttackRange(Math.max(0, unitStatsDto.getAttackRange()));
        unitStats.setCost(Math.max(0, unitStatsDto.getCost()));
        unitStats.setCooldown(Math.max(0, unitStatsDto.getCooldown()));

        // 4. Sauvegarder
        UnitStats savedStats = unitStatsRepository.save(unitStats);

        // 5. Retourner le DTO mis à jour (avec l'ID généré)
        unitStatsDto.setId(savedStats.getId());
        return unitStatsDto;
    }
}
