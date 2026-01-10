package perso.arcade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import perso.arcade.model.entities.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r.name FROM Role r ORDER BY r.name")
    List<String> findAllRolesNames();

    Optional<Role> findByName(String name);

    List<Role> findByNameIn(List<String> names);
}