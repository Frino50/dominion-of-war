package dow.model.dto;

public class RouteDto {
    private Long id;
    private String name;
    private String componentPath;
    private String roleName;
    private boolean needAuth;

    public RouteDto() {
    }

    public RouteDto(Long id, String name, String componentPath, String roleName, boolean needAuth) {
        this.id = id;
        this.name = name;
        this.componentPath = componentPath;
        this.roleName = roleName;
        this.needAuth = needAuth;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isNeedAuth() {
        return needAuth;
    }

    public void setNeedAuth(boolean needAuth) {
        this.needAuth = needAuth;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
