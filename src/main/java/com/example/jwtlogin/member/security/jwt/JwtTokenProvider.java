package com.example.jwtlogin.member.security.jwt;

import com.example.jwtlogin.common.dto.enums.RoleEnums;
import com.example.jwtlogin.member.security.MemberDetailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.micrometer.common.util.StringUtils;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public final class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.access.expire-second}")
    private Long ACCESS_EXPIRE_SECOND;

    @Value("${jwt.refresh.expire-second}")
    private Long REFRESH_EXPIRE_SECOND;

    private Key key;

    private final MemberDetailService memberDetailService;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey, MemberDetailService memberDetailService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.memberDetailService = memberDetailService;
    }

    public String createJwt(Long id) {
        Instant issueDt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expireDt = issueDt.plus(REFRESH_EXPIRE_SECOND, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(Date.from(issueDt))
                .setExpiration(Date.from(expireDt))
                .signWith(key)
                .compact();
    }

    public String createRefreshJwt(String token) {
        Claims claims = getClaims(token);

        Instant issueDt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expireDt = issueDt.plus(REFRESH_EXPIRE_SECOND, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setSubject(claims.getSubject())
                .setIssuedAt(Date.from(issueDt))
                .setExpiration(Date.from(expireDt))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) throws BadRequestException {
        try {
            return isExpiredToken(token);
        } catch (Exception e) {
            log.error("", e);
            throw new BadRequestException("유효하지 않은 토큰입니다.");
        }
    }

    private boolean isExpiredToken(String token) {
        try {
            Jws<Claims> claims = extractAllClaims(token);

            Date expireDt = claims.getBody().getExpiration();
            Date now = new Date();

            return expireDt.before(now);
        } catch (ExpiredJwtException e) {
            log.error("", e);
            return true;
        }
    }

    public Jws<Claims> extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public Authentication getAuthentication(String jwt) throws BadRequestException {
        String memeberId = getClaims(jwt).getSubject();
        if (StringUtils.isBlank(memeberId)) {
            throw new BadRequestException("등록되지 않은 토큰입니다.");
        }

        HashSet<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(RoleEnums.ROLE_MEMBER.value()));

        return new UsernamePasswordAuthenticationToken(memberDetailService.loadUserByUsername(memeberId), jwt,
                authorities);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
