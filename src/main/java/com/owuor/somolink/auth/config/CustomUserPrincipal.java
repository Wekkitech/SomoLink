package com.owuor.somolink.auth.config;

import com.owuor.somolink.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserPrincipal implements UserDetails {

    private final User user;

    public CustomUserPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }


    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;  // or implement your own logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // or implement your own logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // or implement your own logic
    }

    @Override
    public boolean isEnabled() {
        return true;  // or implement your own logic
    }
}
