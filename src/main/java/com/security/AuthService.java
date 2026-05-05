package com.security;

import com.config.JwtConfig;
import com.dto.*;
import com.entity.RefreshTokenEntity;
import com.entity.UserEntity;
import com.enums.Role;
import com.exception.ResourceConflictException;
import com.repository.UserRepository;
import com.security.jwt.JwtService;
import com.security.lockoutservice.AccountLockoutService;
import com.security.tokenservice.RefreshTokenService;
import com.security.tokenservice.TokenBlacklistService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;
    private final AccountLockoutService accountLockoutService;


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

            // Check if account locked BEFORE attempting auth
            userRepository.findByUsernameOrEmail(
                            loginRequestDTO.getEmailOrUsername(), loginRequestDTO.getEmailOrUsername())
                    .ifPresent(user -> {
                        if (!user.isAccountNonLocked()) {
                            throw new AuthenticationServiceException(
                                    "Account locked. Try again after " + user.getLockedUntil()
                            );
                        }
                    });

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmailOrUsername(),loginRequestDTO.getPassword())
            );

            UserDetails userDetails = userService.loadUserByUsername(loginRequestDTO.getEmailOrUsername());
            accountLockoutService.handleSuccessfulLogin(userDetails.getUsername());

            //delete any existing refresh tokens for the user to prevent multiple active sessions
            refreshTokenService.deleteTokensByUsername(userDetails.getUsername());


            //generate new tokens
            String accessToken = jwtService.generateToken(userDetails);
            RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

            //set refresh token in cookie
            Cookie cookie = new Cookie("refreshToken", refreshToken.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge((int) (jwtConfig.getRefreshTokenExpiration()/1000)); // 7 days

            //set cookie in servlet response
            httpServletResponse.addCookie(cookie);

            //return the accesstoken
            return new LoginResponseDTO(accessToken);
        }
        catch (AuthenticationServiceException ex) {
            throw ex; // re-throw lockout message as-is

        } catch (Exception ex) {
            // Failed login — increment counter
            try {
                accountLockoutService.handleFailedAttempt(loginRequestDTO.getEmailOrUsername());
            } catch (Exception ignored) {}

            throw new AuthenticationServiceException("Invalid username or password");
        }

    }

    public LoginResponseDTO refreshToken(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        Cookie[] cookies = request.getCookies();
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

        refreshTokenService.revokeToken(token);
        refreshTokenService.deleteTokensByUsername(userDetails.getUsername());
        RefreshTokenEntity newToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        Cookie cookie = new Cookie("refreshToken", newToken.getToken());
        cookie.setMaxAge((int) (jwtConfig.getRefreshTokenExpiration()/1000)); // 7 days
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);

        String newAccessToken = jwtService.generateToken(userDetails);
        return new LoginResponseDTO(newAccessToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse httpServletResponse) {
//        Cookie[] cookies = request.getCookies();
//        if(cookies == null){
//            throw new AuthenticationServiceException("No cookies found in the request");
//        }
//        Arrays.stream(cookies).filter(cookie -> "refreshToken".equals(cookie.getName()))
//                .findFirst()
//                .ifPresent(cookie -> {
//                    try{
//                        refreshTokenService.revokeToken(cookie.getValue());
//                    }
//                    catch (Exception ignored){}
//                });

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new AuthenticationServiceException("Not authenticated");
        }

        String username = authentication.getName();

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            long remainingExpiry = jwtService.getRemainingExpiry(accessToken);
            if (remainingExpiry > 0) {
                tokenBlacklistService.blacklist(accessToken, remainingExpiry);
            }
        }

        refreshTokenService.deleteTokensByUsername(username);

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);

    }
}
