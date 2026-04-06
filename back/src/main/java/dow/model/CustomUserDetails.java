package dow.model;


import dow.model.entities.Player;
import dow.model.entities.Role;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final Player player;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(Player player) {
        this.player = player;
        this.authorities = buildAuthorities(player.getRoles());
    }

    public CustomUserDetails(Player player, List<GrantedAuthority> authorities) {
        this.player = player;
        this.authorities = authorities;
    }

    private static List<GrantedAuthority> buildAuthorities(Set<Role> roles) {
        return roles.stream()
                .filter(Objects::nonNull)
                .map(Role::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return player.getPassword();
    }

    @Override
    @NonNull
    public String getUsername() {
        return player.getEmail();
    }

    public String getPseudo() {
        return player.getPseudo();
    }

    public UUID getId() {
        return player.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}