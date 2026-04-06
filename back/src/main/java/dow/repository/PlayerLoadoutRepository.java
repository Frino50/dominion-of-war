package dow.repository;

import dow.model.dto.LoadoutUpdateDto;
import dow.model.dto.SpriteInfos;
import dow.model.entities.GameRoom;
import dow.model.entities.Player;
import dow.model.entities.PlayerLoadout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerLoadoutRepository extends JpaRepository<PlayerLoadout, Long> {

    Optional<PlayerLoadout> findByGameRoomAndPlayer(GameRoom gameRoom, Player player);

    @Query("""
                SELECT new dow.model.dto.LoadoutUpdateDto(
                    p.pseudo,
                    SIZE(pl.sprites),
                    COALESCE(pl.locked, false)
                )
                FROM GameParticipant gp
                JOIN gp.player p
                LEFT JOIN PlayerLoadout pl ON pl.player = p AND pl.gameRoom = gp.gameRoom
                WHERE gp.gameRoom.id = :gameRoomId
                AND p.id <> :currentPlayerId
                AND (gp.role = dow.model.enumeration.ParticipantRole.PLAYER_1
                  OR gp.role = dow.model.enumeration.ParticipantRole.PLAYER_2)
            """)
    LoadoutUpdateDto getOpponent(
            @Param("gameRoomId") Long gameRoomId,
            @Param("currentPlayerId") UUID currentPlayerId
    );

    @Query("""
                SELECT COUNT(pl) = 2
                FROM PlayerLoadout pl
                WHERE pl.gameRoom.id = :gameRoomId
                AND pl.locked = true
            """)
    boolean areAllPlayersLocked(@Param("gameRoomId") Long gameRoomId);

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
                JOIN pl.sprites s
                JOIN s.animations a
                WHERE pl.gameRoom.id = :gameRoomId
                AND pl.player.id = :playerId
                AND a.type = dow.model.enumeration.AnimationType.IDLE
            """)
    List<SpriteInfos> findMyLoadoutSpriteInfos(
            @Param("gameRoomId") Long gameRoomId,
            @Param("playerId") UUID playerId
    );
}