package dow.model.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "Route")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String componentPath;

    @Column(nullable = false)
    private boolean needAuth = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    public Route() {
    }

    public Route(Long id, String name, String componentPath, boolean needAuth, Role role) {
        this.id = id;
        this.name = name;
        this.componentPath = componentPath;
        this.needAuth = needAuth;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    public boolean isNeedAuth() {
        return needAuth;
    }

    public void setNeedAuth(boolean needAuth) {
        this.needAuth = needAuth;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return needAuth == route.needAuth && Objects.equals(id, route.id) && Objects.equals(name, route.name) && Objects.equals(componentPath, route.componentPath) && Objects.equals(role, route.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, componentPath, needAuth, role);
    }
}
