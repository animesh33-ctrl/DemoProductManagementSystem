package com.security.token;

import com.entity.RefreshTokenEntity;
import com.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_DURATION_DAYS = 7;

    public RefreshTokenEntity createRefreshToken(String username) {

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsername(username);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_DURATION_DAYS));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.saveAndFlush(refreshToken);
    }

    public RefreshTokenEntity verifyToken(String token) {

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if(refreshToken.isRevoked()){
            throw new RuntimeException("Refresh token revoked");
        }

        if(refreshToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    public void revokeToken(String token){

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        refreshToken.setRevoked(true);

        refreshTokenRepository.saveAndFlush(refreshToken);
    }

    public void deleteTokensByUsername(String username){
        refreshTokenRepository.deleteByUsername(username);
    }
}