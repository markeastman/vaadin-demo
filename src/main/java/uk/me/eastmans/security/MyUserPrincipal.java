package uk.me.eastmans.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class MyUserPrincipal implements UserDetails {
    final private User user;

    private Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

    public MyUserPrincipal(User user) {
        this.user = user;
        buildGrantedAuthorities();
    }

    public User getUser() {
        return user;
    }

    public Set<Persona> getPersonas() {
        return user.getPersonas();
    }

    public Persona getCurrentPersona() {
        return user.getDefaultPersona();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;//user.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;//user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;//user.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    public void buildGrantedAuthorities() {
        // Now set up the authorities from the default persona
        grantedAuthorities = new HashSet<>();
        Set<Authority> authorities = Collections.emptySet();
        if (user.getDefaultPersona() != null)
            authorities = user.getDefaultPersona().getAuthorities();
        authorities.forEach(
                authority -> grantedAuthorities.add( new SimpleGrantedAuthority(authority.getName())));

    }
}