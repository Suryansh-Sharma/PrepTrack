package com.suryansh.preptrack.core.features.auth.security;

import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public record UserPrincipal(AppUser user) implements UserDetails {

    public Integer getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getDisplayName() {
        return user.getDisplayName();
    }

    public String getTimezone() {
        return user.getTimezone();
    }

    public String getStatus() {
        return user.getStatus().toString();
    }

    public String getPlan() {
        return user.getPlan().toString();
    }

    public String getRole() { return user.getRole().toString(); }

    public Instant getEmailVerifiedAt() {
        return user.getEmailVerifiedAt();
    }

    public Instant getDeletedAt() {
        return user.getDeletedAt();
    }

    public Instant getCreatedAt() {
        return user.getCreatedAt();
    }

    public Instant getUpdatedAt() {
        return user.getUpdatedAt();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of( new SimpleGrantedAuthority( "ROLE_" + user.getRole().name() ) );
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        // Email is the authentication identifier.
        return user.getEmail();
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
        return user.getDeletedAt() == null && user.getStatus() == AppUser.Status.ACTIVE;
    }
}