package com.suryansh.preptrack.core.features.auth.security;

import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import com.suryansh.preptrack.core.features.auth.domain.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AppUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return new UserPrincipal(user);
    }
}
