package com.security.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditLogService {

    public void logRequest(HttpServletRequest request, Authentication authentication, int status) {

        String username = "anonymous";

        if(authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        log.info("AUDIT LOG | user={} | method={} | uri={} | status={} | ip={}",
                username, method, uri, status, ip);
    }
}