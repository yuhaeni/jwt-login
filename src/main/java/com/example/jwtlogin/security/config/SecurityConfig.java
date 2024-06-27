package com.example.jwtlogin.security.config;

import com.example.jwtlogin.common.dto.enums.RoleEnums;
import com.example.jwtlogin.member.domain.MemberRepository;
import com.example.jwtlogin.redis.util.RedisUtils;
import com.example.jwtlogin.security.CustomLogoutHandler;
import com.example.jwtlogin.security.MemberDetailService;
import com.example.jwtlogin.security.jwt.JwtAuthenticationFilter;
import com.example.jwtlogin.security.jwt.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberRepository memberRepository;

    private final RedisUtils redisUtils;

    @Bean
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/join", "/login")
                                .permitAll()
                                .requestMatchers("/api/v1/member/**", "/api/v1/todolist/**")
                                .hasAnyAuthority(RoleEnums.ROLE_MEMBER.value())
                )
                // TODO form login 구현
//                .formLogin((formLogin) ->
//                        formLogin.usernameParameter("email")
//                                .passwordParameter("password")
//                                .defaultSuccessUrl("/main", true)
//                )
                .headers(
                        httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer
                                .frameOptions(
                                        FrameOptionsConfig::sameOrigin
                                )
                )
                .addFilterBefore(jwtAuthenticationFilter(jwtAuthenticationProvider()),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(jwtAuthenticationProvider()),
                        LogoutFilter.class)
                .logout(
                        (log) -> log
                                .logoutUrl("/logout")
                                .addLogoutHandler(customLogoutHandler(jwtAuthenticationProvider()))
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                )
        ;

        return httpSecurity.build();
    }

    @Bean
    public LogoutHandler customLogoutHandler(JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new CustomLogoutHandler(jwtAuthenticationProvider);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                web
                        .ignoring()
                        .requestMatchers("/")
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()
                        );
    }


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new JwtAuthenticationFilter(jwtAuthenticationProvider);
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider() {
        return new JwtAuthenticationProvider(redisUtils);
    }

    @Bean
    public MemberDetailService memberDetailService() {
        return new MemberDetailService(memberRepository);
    }

}