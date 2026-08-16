package com.suryansh.preptrack.core.features.auth.command.refreshToken;

import com.suryansh.preptrack.core.exception.InvalidCredentialsException;
import com.suryansh.preptrack.core.features.auth.command.login.LoginResponse;
import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import com.suryansh.preptrack.core.features.auth.domain.RefreshToken;
import com.suryansh.preptrack.core.features.auth.domain.repository.RefreshTokenRepository;
import com.suryansh.preptrack.core.features.auth.security.UserPrincipal;
import com.suryansh.preptrack.core.security.CurrentUserService;
import com.suryansh.preptrack.core.security.JwtService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenCommandHandler {
    @Value("${security.jwt.expiration-time}")
    private long accessTokenExpiration;
    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDays;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public RefreshTokenCommandHandler(RefreshTokenRepository refreshTokenRepository, @Value("${security.jwt.refresh-token-day}") long refreshTokenDays, JwtService jwtService, CurrentUserService currentUserService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDays = refreshTokenDays;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
    }

    public RefreshTokenResponse create(AppUser user, String device, String ip) {
        Instant now = Instant.now();
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(token);
        refreshToken.setUser(user);
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(now.plus(refreshTokenDays, ChronoUnit.DAYS));
        refreshToken.setDeviceInfo(device);
        refreshToken.setIpAddress(ip);
        refreshTokenRepository.save(refreshToken);
        return new RefreshTokenResponse(token, refreshToken.getExpiresAt());
    }


    public LoginResponse.AuthenticationInfo refresh(RefreshTokenCommand command) {
        var refreshToken = refreshTokenRepository.findById(command.refreshToken()).orElseThrow(() -> new InvalidCredentialsException("Invalid Refresh Token"));

        if (refreshToken.getRevokedAt() != null) {
            throw new InvalidCredentialsException("Refresh Token has been revoked");
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpiration);
        if (refreshToken.getExpiresAt().isBefore(now)) {
            throw new InvalidCredentialsException("Refresh Token has expired");
        }

        var user = refreshToken.getUser();
        if (user.getDeletedAt() != null) {
            throw new InvalidCredentialsException("User account is deleted");
        }
        if (user.getStatus() != AppUser.Status.ACTIVE) {
            throw new InvalidCredentialsException("User account is not active");
        }

        refreshToken.setLastUsedAt(now);
        refreshTokenRepository.save(refreshToken);

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateToken(principal);
        return new LoginResponse.AuthenticationInfo(accessToken,"Bearer",accessTokenExpiration,command.refreshToken(),refreshToken.getExpiresAt(),now,expiresAt);

    }

    public void logout(@Valid RefreshTokenCommand command) {
        var refreshToken = refreshTokenRepository.findById(command.refreshToken()).orElseThrow(() -> new InvalidCredentialsException("Invalid Refresh Token"));
        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(refreshToken);
        }
    }


    public void logoutAll() {
        Integer userId = currentUserService.getUserId();
        refreshTokenRepository.LogoutAll(userId);
    }
}
