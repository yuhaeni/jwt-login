package com.example.jwtlogin.common.interceptor;

import com.example.jwtlogin.security.MemberDetails;
import com.example.jwtlogin.security.jwt.JwtAuthenticationProvider;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration
public class AuthenticationInterceptor implements HandlerInterceptor {

    final JwtAuthenticationProvider jwtAuthenticationProvider;

    public AuthenticationInterceptor(JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String accessToken = jwtAuthenticationProvider.resolveTokenInCookie(request);
        if (StringUtils.isBlank(accessToken)) {
            throw new RuntimeException("로그인 필요");
        }

        try {
            if (jwtAuthenticationProvider.validateToken(accessToken)) {
                Authentication authentication = jwtAuthenticationProvider.getAuthentication(accessToken);
                MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();

                request.setAttribute("_memberDetails", memberDetails);
            }
        } catch (BadRequestException e) {
            throw new BadRequestException();
        }

        return true;
    }
}
