package com.security;

import com.dto.*;
import com.entity.RefreshTokenEntity;
import com.entity.UserEntity;
import com.enums.Role;
import com.exception.ResourceConflictException;
import com.security.jwt.JwtService;
import com.security.token.RefreshTokenService;
import com.service.interfaces.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserDTO signUp(@Valid SignUpRequestDto signUpRequestDto) {
        UserEntity userEntity = userService.getUserByUsernameOrEmail(signUpRequestDto.getUsername(),signUpRequestDto.getEmail());
        if(userEntity != null){
            throw new ResourceConflictException("Username or email already exists");
        }

        UserEntity user = modelMapper.map(signUpRequestDto, UserEntity.class);
        user.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        user.setRole(Role.USER);

        user = userService.addUser(user);
        return modelMapper.map(user,UserDTO.class);
    }

    public TokenResponse login(@Valid LoginRequestDTO loginRequestDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmailOrUsername(),loginRequestDTO.getPassword())
            );
            String accessToken = jwtService.generateToken(loginRequestDTO.getEmailOrUsername());
            RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(loginRequestDTO.getEmailOrUsername());
            return new TokenResponse(accessToken, refreshToken.getToken());
        }
        catch (Exception ex){
                throw new AuthenticationServiceException("Invalid username or password");
        }

    }

    public LoginResponseDTO refreshToken(HttpServletRequest request) {
        String token = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside the Cookies"));

        RefreshTokenEntity refreshToken = refreshTokenService.verifyToken(token);
        String username = refreshToken.getUsername();
        String newAccessToken = jwtService.generateToken(username);
        return new LoginResponseDTO(newAccessToken);
    }
}
