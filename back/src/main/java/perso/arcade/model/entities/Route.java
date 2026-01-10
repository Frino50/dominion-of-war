package perso.arcade.model.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "Route")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name shown in URL, e.g. "/territory" or "territory"
    @Column(nullable = false, unique = true)
    private String name;

    // Vue component import path relative to src/views, e.g. "Territory.vue" or with alias "@/views/Territory.vue"
    @Column(nullable = false)
    private String componentPath;

    @Column(nullable = false)
    private boolean needAuth = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role; // required role when needAuth=true; null means any authenticated user

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(id, route.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
