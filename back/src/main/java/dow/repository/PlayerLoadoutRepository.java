package dow.repository;

import dow.model.dto.LoadoutUpdateDto;
import dow.model.entities.GameRoom;
import dow.model.entities.Player;
import dow.model.entities.PlayerLoadout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
            @Param("currentPlayerId") Long currentPlayerId
    );

    @Query("""
                SELECT COUNT(pl) = 2
                FROM PlayerLoadout pl
                WHERE pl.gameRoom.id = :gameRoomId
                AND pl.locked = true
            """)
    boolean areAllPlayersLocked(@Param("gameRoomId") Long gameRoomId);
}