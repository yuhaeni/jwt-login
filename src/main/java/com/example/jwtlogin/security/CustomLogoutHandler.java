package com.example.jwtlogin.security;

import com.example.jwtlogin.security.jwt.JwtAuthenticationProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Slf4j
public class CustomLogoutHandler implements LogoutHandler {

    final JwtAuthenticationProvider jwtAuthenticationProvider;

    public CustomLogoutHandler(JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication == null) {
            return;
        }
        try {
            jwtAuthenticationProvider.destroyToken(request, response);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }
}
