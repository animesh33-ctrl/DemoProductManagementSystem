package com.filters;

import com.security.ratelimit.LoginRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final LoginRateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if(request.getRequestURI().contains("/auth/login")) {

            String ip = request.getRemoteAddr();

            if(!rateLimiter.isAllowed(ip)) {
                response.setStatus(429);
                response.getWriter().write("Too many login attempts. Try again later.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}