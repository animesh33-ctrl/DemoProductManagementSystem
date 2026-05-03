package com.controller;

import com.advice.ApiResponse;
import com.dto.LoginRequestDTO;
import com.dto.SignUpRequestDto;
import com.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@RequestBody @Valid SignUpRequestDto signUpRequestDto){
        return new ResponseEntity<>(new ApiResponse<>(authService.signUp(signUpRequestDto)), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO, HttpServletResponse httpServletResponse){
        return new ResponseEntity<>(new ApiResponse<>(authService.login(loginRequestDTO,httpServletResponse)), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refreshToken(HttpServletRequest request, HttpServletResponse httpServletResponse){
        return new ResponseEntity<>(new ApiResponse<>(authService.refreshToken(request,httpServletResponse)), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request, HttpServletResponse httpServletResponse){
        authService.logout(request,httpServletResponse);
        return new ResponseEntity<>(new ApiResponse<>("Logged out successfully"), HttpStatus.OK);
    }
}
