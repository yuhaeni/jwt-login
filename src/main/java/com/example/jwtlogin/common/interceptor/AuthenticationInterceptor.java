package com.example.jwtlogin.common.interceptor;

import com.example.jwtlogin.security.MemberDetails;
import com.example.jwtlogin.security.jwt.JwtAuthenticationProvider;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    final JwtAuthenticationProvider jwtAuthenticationProvider;

    public AuthenticationInterceptor(JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String accessToken = jwtAuthenticationProvider.resolveAccessTokenInHeader(request);
        if (
                StringUtils.isNotBlank(accessToken)
                        && StringUtils.startsWith(accessToken, "Bearer ")
        ) {
            accessToken = StringUtils.replace(accessToken, "Bearer ", "");
        }

        accessToken = jwtAuthenticationProvider.decryptToken(accessToken);
        if (StringUtils.isBlank(accessToken)) {
            throw new RuntimeException("로그인 필요");
        }

        try {
            if (jwtAuthenticationProvider.validateToken(accessToken)) {
                Authentication authentication = jwtAuthenticationProvider.getAuthentication(accessToken);
                MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();

                request.setAttribute("_memberDetails", memberDetails);
            }
        } catch (ExpiredJwtException e) {
            String subject = e.getClaims().getSubject();
            String refreshToken = jwtAuthenticationProvider.getRefreshTokenInRedis(subject);
            if (StringUtils.isBlank(refreshToken)) {
                jwtAuthenticationProvider.removeAuthentication(request, response);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }

            refreshToken = jwtAuthenticationProvider.decryptToken(refreshToken);
            try {
                if (jwtAuthenticationProvider.validateToken(refreshToken)) {
                    jwtAuthenticationProvider.reissueToken(response, e.getClaims());
                } else {
                    jwtAuthenticationProvider.removeAuthentication(request, response);
                }
            } catch (Exception ex) {
                log.error("", ex);
                jwtAuthenticationProvider.removeAuthentication(request, response);
                throw new RuntimeException(ex);
            }

        } catch (Exception e) {
            log.error("", e);
            jwtAuthenticationProvider.removeAuthentication(request, response);
            throw e;
        }

        return true;
    }
}
