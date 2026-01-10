package perso.arcade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import perso.arcade.model.dto.RouteDto;
import perso.arcade.model.entities.Route;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query("SELECT new perso.arcade.model.dto.RouteDto(" +
            "r.id, r.name, r.componentPath, role.name, r.needAuth) " +
            "FROM Route r " +
            "LEFT JOIN r.role role " +
            "ORDER BY r.id")
    List<RouteDto> findAllRoutesAsDto();

    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Route r WHERE r.name = :name AND r.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}