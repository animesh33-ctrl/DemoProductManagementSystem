package com.security;

import com.config.JwtConfig;
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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final JwtConfig jwtConfig;

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

    public LoginResponseDTO login(@Valid LoginRequestDTO loginRequestDTO, HttpServletResponse httpServletResponse) {
        //1st authenticate the user using AuthenticationManager, if authentication fails an exception will be thrown
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmailOrUsername(),loginRequestDTO.getPassword())
            );

            UserDetails userDetails = userService.loadUserByUsername(loginRequestDTO.getEmailOrUsername());

            //delete any existing refresh tokens for the user to prevent multiple active sessions
            refreshTokenService.deleteTokensByUsername(userDetails.getUsername());


            //generate new tokens
            String accessToken = jwtService.generateToken(userDetails);
            RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

            //set refresh token in cookie
            Cookie cookie = new Cookie("refreshToken", refreshToken.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/auth/refresh");
            cookie.setMaxAge((int) (jwtConfig.getRefreshTokenExpiration()/1000)); // 7 days

            //set cookie in servlet response
            httpServletResponse.addCookie(cookie);

            //return the accesstoken
            return new LoginResponseDTO(accessToken);
        }
        catch (Exception ex){
                throw new AuthenticationServiceException("Invalid username or password");
        }

    }

    public LoginResponseDTO refreshToken(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        Cookie[] cookies = request.getCookies();
        System.out.println("cookies: " + Arrays.toString(cookies));
        if(cookies == null){
            throw new AuthenticationServiceException("No cookies found in the request");
        }

        String token = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside the Cookies"));

        RefreshTokenEntity old = refreshTokenService.verifyToken(token);
        UserDetails userDetails = userService.loadUserByUsername(old.getUsername());

        refreshTokenService.deleteTokensByUsername(userDetails.getUsername());
        RefreshTokenEntity newToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        Cookie cookie = new Cookie("refreshToken", newToken.getToken());
        cookie.setMaxAge((int) (jwtConfig.getRefreshTokenExpiration()/1000)); // 7 days
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth/refresh");
        httpServletResponse.addCookie(cookie);

        String newAccessToken = jwtService.generateToken(userDetails);
        return new LoginResponseDTO(newAccessToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            throw new AuthenticationServiceException("No cookies found in the request");
        }
        Arrays.stream(cookies).filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> {
                    try{
                        refreshTokenService.revokeToken(cookie.getValue());
                    }
                    catch (Exception ignored){}
                });

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth/refresh");
        httpServletResponse.addCookie(cookie);

    }
}
