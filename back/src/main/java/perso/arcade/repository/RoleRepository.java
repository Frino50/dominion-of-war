package perso.arcade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import perso.arcade.model.dto.RoleDto;
import perso.arcade.model.entities.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT new perso.arcade.model.dto.RoleDto(r.id, r.name) FROM Role r ORDER BY r.name")
    List<RoleDto> findAllAsDto();

    @Query("SELECT r.name FROM Role r ORDER BY r.name")
    List<String> findAllRolesNames();
    
    Optional<Role> findByName(String name);

    List<Role> findByNameIn(List<String> names);

    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = :name AND r.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}