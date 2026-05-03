package com.security.token;

import com.config.JwtConfig;
import com.entity.RefreshTokenEntity;
import com.entity.UserEntity;
import com.exception.InvalidActionException;
import com.exception.ResourceNotFoundException;
import com.repository.RefreshTokenRepository;
import com.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;


    public RefreshTokenEntity createRefreshToken(String username) {

        UserEntity user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsername(username);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpiration() / 1000));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.saveAndFlush(refreshToken);
    }

    public RefreshTokenEntity verifyToken(String token) {

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidActionException("Invalid refresh token"));

        if(refreshToken.isRevoked()){
            throw new InvalidActionException("Refresh token revoked");
        }

        if(refreshToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new InvalidActionException("Refresh token expired");
        }

        return refreshToken;
    }

    public void revokeToken(String token){

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        refreshToken.setRevoked(true);

        refreshTokenRepository.saveAndFlush(refreshToken);
    }

    @Transactional
    public void deleteTokensByUsername(String username){
        refreshTokenRepository.deleteByUsername(username);
    }
}