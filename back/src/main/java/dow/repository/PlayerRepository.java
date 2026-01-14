package dow.repository;

import dow.model.entities.Player;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByPseudo(String pseudo);

    @EntityGraph(attributePaths = "roles")
    @Query("SELECT p FROM Player p WHERE p.pseudo = :pseudo")
    Optional<Player> findWithRolesByPseudo(String pseudo);

    @EntityGraph(attributePaths = "roles")
    @Query("SELECT p FROM Player p ORDER BY p.pseudo")
    List<Player> findAllWithRoles();

    @EntityGraph(attributePaths = "roles")
    @Query("SELECT p FROM Player p WHERE p.id = :id")
    Optional<Player> findByIdWithRoles(@Param("id") Long id);
}