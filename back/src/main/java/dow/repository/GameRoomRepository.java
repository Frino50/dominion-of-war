package dow.repository;

import dow.model.dto.GameRoomInfoDto;
import dow.model.entities.GameRoom;
import dow.model.enumeration.GameStatus;
import dow.model.projection.GameRoomLightProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    Optional<GameRoom> findByName(String name);

    @Query("""
            SELECT new dow.model.dto.GameRoomInfoDto(
                r.id,
                r.name,
                (r.password IS NOT NULL AND r.password <> ''),
                r.status,
                CAST(SUM(CASE WHEN p.role IN (dow.model.enumeration.ParticipantRole.PLAYER_1, dow.model.enumeration.ParticipantRole.PLAYER_2) THEN 1 ELSE 0 END) AS int),
                CAST(SUM(CASE WHEN p.role = dow.model.enumeration.ParticipantRole.SPECTATOR THEN 1 ELSE 0 END) AS int)
            )
            FROM GameRoom r
            LEFT JOIN GameParticipant p ON p.gameRoom = r
            WHERE r.status IN :statuses
            GROUP BY r.id, r.name, r.password, r.status
            """)
    List<GameRoomInfoDto> listAvailableRooms(@Param("statuses") List<GameStatus> statuses);

    GameRoomLightProjection findRoomProjectedById(Long id);
}