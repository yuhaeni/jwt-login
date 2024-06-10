package com.example.jwtlogin.security.jwt;

import com.example.jwtlogin.common.dto.enums.RoleEnums;
import com.example.jwtlogin.security.MemberDetailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public final class JwtAuthenticationProvider {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.access.expire-milliseconds}")
    private Long ACCESS_EXPIRE_MILLISECONDS;

    @Value("${jwt.refresh.expire-milliseconds}")
    private Long REFRESH_EXPIRE_MILLISECONDS;

    @Value("${jwt.access.token-header-name}")
    private String ACCESS_TOKEN_HEADER_NAME;

    private Key key;

    private final MemberDetailService memberDetailService;

    public JwtAuthenticationProvider(@Value("${jwt.secret-key}") String secretKey,
                                     MemberDetailService memberDetailService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.memberDetailService = memberDetailService;
    }

    public Claims buildClaims(String email, long memeberSeq, Set<String> authorities) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("memeberSeq", memeberSeq);
        claims.put("authorities", authorities);

        return claims;
    }

    public void issueToken(HttpServletResponse response, Claims claims) {
        JwtAuthenticationDto jwtAuthenticationDto = createToken(claims);
        saveTokenToCookie(response, jwtAuthenticationDto.getAccessToken());
    }

    public void saveTokenToCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_HEADER_NAME, token)
                .httpOnly(true)
                .path("/")
                .maxAge(ACCESS_EXPIRE_MILLISECONDS / 1000)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public JwtAuthenticationDto createToken(Claims claims) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date issueDate = new Date();

        Date accessDate = new Date(issueDate.getTime() + ACCESS_EXPIRE_MILLISECONDS);
        Date refreshDate = new Date(issueDate.getTime() + REFRESH_EXPIRE_MILLISECONDS);

        String accessToken = this.generateToken(claims, issueDate, accessDate);
        String refreshToken = this.generateToken(claims, issueDate, refreshDate);

        return JwtAuthenticationDto.builder()
                .accessToken(accessToken)
                .accessTokenExpirationDate(accessDate)
                .accessTokenExpirationDateStr(sdf.format(accessDate))
                .refreshToken(refreshToken)
                .refreshTokenExpirationDate(refreshDate)
                .refreshTokenExpirationDateStr(sdf.format(refreshDate))
                .build();
    }

    public String generateToken(Claims claims, Date now, Date expirationDate) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(claims.getSubject())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
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

    public String getId(String token, String secretKey) {
        return String.valueOf(extractClaims(token, secretKey).getId());
    }

    private Claims extractClaims(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public String resolveTokenInCookie(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
//            if (){
//              // TODO
//            }
        }

        return null;
    }
}