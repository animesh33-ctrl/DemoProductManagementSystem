package com.security.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.*;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityEventListener {

    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent event) {
        log.info("[AUTH SUCCESS] user={} | authorities={}",
                event.getAuthentication().getName(),
                event.getAuthentication().getAuthorities());
    }

    @EventListener
    public void onLoginFailure(AbstractAuthenticationFailureEvent event) {
        log.warn("[AUTH FAILURE] user={} | reason={}",
                event.getAuthentication().getName(),
                event.getException().getMessage());
    }

    @EventListener
    public void onAccessDenied(AuthorizationDeniedEvent event) {
        log.warn("[ACCESS DENIED] user={} | decision={}",
                event.getAuthentication().get().getName(),
                event.getAuthorizationResult());
    }
}