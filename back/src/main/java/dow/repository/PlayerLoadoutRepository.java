package dow.repository;

import dow.model.entities.GameRoom;
import dow.model.entities.Player;
import dow.model.entities.PlayerLoadout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerLoadoutRepository extends JpaRepository<PlayerLoadout, Long> {
    Optional<PlayerLoadout> findByGameRoomAndPlayer(GameRoom gameRoom, Player player);

    List<PlayerLoadout> findByGameRoom(GameRoom gameRoom);
}