package dow.repository;

import dow.model.dto.GameParticipantWaitingDto;
import dow.model.entities.GameParticipant;
import dow.model.entities.GameRoom;
import dow.model.entities.Player;
import dow.model.enumeration.GameStatus;
import dow.model.enumeration.ParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameParticipantRepository extends JpaRepository<GameParticipant, Long> {
    List<GameParticipant> findByGameRoom(GameRoom gameRoom);

    boolean existsByGameRoomAndPlayer(GameRoom gameRoom, Player player);

    long countByGameRoomAndRoleIn(GameRoom room, List<ParticipantRole> list);

    void deleteByGameRoomAndPlayer(GameRoom room, Player player);

    @Query("SELECT new dow.model.dto.GameParticipantWaitingDto(p.player.pseudo, p.role) " +
            "FROM GameParticipant p " +
            "WHERE p.gameRoom.id = :gameRoomId")
    List<GameParticipantWaitingDto> findAllParticipantsByRoomId(Long gameRoomId);

    @Query("SELECT p.gameRoom.id " +
            "FROM GameParticipant p " +
            "WHERE p.player.id = :playerId " +
            "AND p.gameRoom.status != :gameStatus ")
    Long findGameRoomActive(UUID playerId, GameStatus gameStatus);

    @Query("SELECT gp.player.pseudo FROM GameParticipant gp WHERE gp.gameRoom.id = :gameRoomId AND gp.role = :role")
    String findPseudoByGameRoomIdAndRole(@Param("gameRoomId") Long gameRoomId, @Param("role") ParticipantRole role);
}