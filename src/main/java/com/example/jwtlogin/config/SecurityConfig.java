package com.example.jwtlogin.config;

import com.example.jwtlogin.common.dto.enums.RoleEnums;
import com.example.jwtlogin.member.domain.MemberRepository;
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
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberRepository memberRepository;

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
//                .formLogin((formLogin) ->
//                        formLogin.usernameParameter("email")
//                                .passwordParameter("password")
//                                .defaultSuccessUrl("/main", true)
//                )
                .headers(
                        httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer
                                .frameOptions(
                                        HeadersConfigurer.FrameOptionsConfig::sameOrigin
                                )
                )
                .addFilterBefore(jwtAuthenticationFilter(jwtAuthenticationProvider(memberDetailService())),
                        UsernamePasswordAuthenticationFilter.class)
        ;

        return httpSecurity.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                web
                        .ignoring()
                        .requestMatchers("/main")
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()
                        );
    }


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new JwtAuthenticationFilter(jwtAuthenticationProvider);
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(MemberDetailService memberDetailService) {
        return new JwtAuthenticationProvider(memberDetailService);
    }

    @Bean
    public MemberDetailService memberDetailService() {
        return new MemberDetailService(memberRepository);
    }

}