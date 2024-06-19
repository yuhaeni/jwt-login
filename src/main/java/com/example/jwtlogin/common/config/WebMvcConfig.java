package com.example.jwtlogin.common.config;

import com.example.jwtlogin.common.interceptor.AuthenticationInterceptor;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;

    private final List<String> addEndPointList = Arrays.asList("/api/v1/member", "/api/v1/todolist");

    public WebMvcConfig(AuthenticationInterceptor authenticationInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns(addEndPointList)
                .excludePathPatterns(PathRequest.toStaticResources().atCommonLocations().toString());
    }
}
