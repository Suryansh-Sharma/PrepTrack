package com.suryansh.preptrack.core.features.auth.security;

import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final AppUser user;

    public AppUser getUser() {
        return user;
    }

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

    public java.time.Instant getEmailVerifiedAt() {
        return user.getEmailVerifiedAt();
    }

    public java.time.Instant getDeletedAt() {
        return user.getDeletedAt();
    }

    public java.time.Instant getCreatedAt() {
        return user.getCreatedAt();
    }

    public java.time.Instant getUpdatedAt() {
        return user.getUpdatedAt();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Add roles/permissions when AppUser supports them.
        return List.of();
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