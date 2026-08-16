package com.suryansh.preptrack.core.features.auth.query.session;

import com.suryansh.preptrack.core.features.auth.domain.repository.RefreshTokenRepository;
import com.suryansh.preptrack.core.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionQueryHandler {
    private static final Logger logger = LoggerFactory.getLogger(SessionQueryHandler.class);
    private final CurrentUserService currentUserService;
    private final RefreshTokenRepository refreshTokenRepository;
    public List<SessionInfoDto> getAllSession() {
        try{
            Integer userId = currentUserService.getUserId();
            return refreshTokenRepository.findByUserId(userId)
                    .stream()
                    .map(e -> new SessionInfoDto(
                            e.getId(),
                            e.getExpiresAt(),
                            e.getCreatedAt(),
                            e.getLastUsedAt(),
                            e.getDeviceInfo(),
                            e.getIpAddress()
                    ))
                    .toList();
        }catch (Exception e){
            logger.error(e.getMessage());
            throw e;
        }
    }
}
