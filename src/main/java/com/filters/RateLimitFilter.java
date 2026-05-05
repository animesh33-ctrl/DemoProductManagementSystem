package com.filters;

import com.advice.ApiError;
import com.advice.ApiResponse;
import com.security.ratelimit.LoginRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final LoginRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if(request.getRequestURI().contains("/auth/login")) {

            String ip = request.getRemoteAddr();

            if(!rateLimiter.isAllowed(ip)) {
                ApiError apiError = new ApiError(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Too many login attempts. Try again later.",
                        null
                );
                ApiResponse<?> apiResponse = new ApiResponse<>(apiError);

                response.setStatus(429);
                response.setContentType("application/json");
                objectMapper.writeValue(response.getOutputStream(), apiResponse);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}