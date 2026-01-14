package perso.dow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import perso.dow.model.dto.RouteDto;
import perso.dow.model.entities.Role;
import perso.dow.model.entities.Route;

import java.util.List;
import java.util.Set;

public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query("SELECT new perso.dow.model.dto.RouteDto(" +
            "r.id, r.name, r.componentPath, role.name, r.needAuth) " +
            "FROM Route r " +
            "LEFT JOIN r.role role " +
            "ORDER BY r.id")
    List<RouteDto> findAllRoutesAsDto();

    @Query("SELECT new perso.dow.model.dto.RouteDto(" +
            "r.id, r.name, r.componentPath, role.name, r.needAuth) " +
            "FROM Route r " +
            "LEFT JOIN r.role role " +
            "WHERE r.needAuth = false " +
            "OR (r.needAuth = true AND r.role IS NULL) " +
            "OR (r.needAuth = true AND r.role IN :userRoles) " +
            "ORDER BY r.id")
    List<RouteDto> findAvailableRoutesAsDto(@Param("userRoles") Set<Role> userRoles);

    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Route r WHERE r.name = :name AND r.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}