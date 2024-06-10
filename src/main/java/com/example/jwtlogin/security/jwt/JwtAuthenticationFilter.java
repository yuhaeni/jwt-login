package com.example.jwtlogin.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            jwtAuthenticationProvider.validateFilterToken(request, response);
        } catch (Exception e) {
            log.error("", e);
            jwtAuthenticationProvider.removeAuthentication(request, response);
            throw new RuntimeException(e);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        Collection<String> excludeUrlPatterns = new LinkedHashSet<>();
        excludeUrlPatterns.add(StaticResourceLocation.CSS.getPatterns().toString());
        excludeUrlPatterns.add(StaticResourceLocation.JAVA_SCRIPT.getPatterns().toString());
        excludeUrlPatterns.add(StaticResourceLocation.IMAGES.getPatterns().toString());
        excludeUrlPatterns.add(StaticResourceLocation.FAVICON.getPatterns().toString());

        return excludeUrlPatterns.stream()
                .anyMatch(pattern -> new AntPathMatcher().match(pattern, request.getServletPath()));
    }

}
