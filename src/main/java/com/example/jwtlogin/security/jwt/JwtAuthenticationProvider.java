package com.example.jwtlogin.security.jwt;

import com.example.jwtlogin.redis.util.RedisUtils;
import com.example.jwtlogin.security.MemberDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Value("${jwt.refresh.token-header-name}")
    private String REFRESH_TOKEN_HEADER_NAME;

    private Key key;

    private static String LOGOUT = "logout";

    private final RedisUtils redisUtils;

    public JwtAuthenticationProvider(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    @PostConstruct
    private void init() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims buildClaims(String email, long memeberSeq, Set<String> authorities) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("memeberSeq", memeberSeq);
        claims.put("authorities", authorities);

        return claims;
    }

    public void issueToken(HttpServletResponse response, Claims claims) {
        JwtAuthenticationDto jwtAuthenticationDto = createToken(claims);
        saveAccessTokenToCookie(response, jwtAuthenticationDto.getAccessToken());
        saveRefreshTokenToRedis(claims, jwtAuthenticationDto.getRefreshToken());
    }

    private void saveRefreshTokenToRedis(Claims claims, String token) {
        redisUtils.setRedisValueWithTimeout(REFRESH_TOKEN_HEADER_NAME.concat(":").concat(claims.getSubject()),
                token, REFRESH_EXPIRE_MILLISECONDS);
    }

    private void saveAccessTokenToCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_HEADER_NAME, token)
                .httpOnly(true)
                .path("/")
                .maxAge(ACCESS_EXPIRE_MILLISECONDS)
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

    private String generateToken(Claims claims, Date now, Date expirationDate) {
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
            return isUnexpiredToken(token);
        } catch (Exception e) {
            log.error("", e);
            throw new BadRequestException("유효하지 않은 토큰입니다.");
        }
    }

    private boolean isUnexpiredToken(String token) {
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

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void validateFilterToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String accessToken = resolveTokenInCookie(request);
        try {
            if (StringUtils.isNotBlank(accessToken)) {
                Authentication authentication = getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            removeAuthentication(request, response);

            String subject = e.getClaims().getSubject();
            String refreshToken = getRefreshTokenInRedis(subject);
            if (StringUtils.isEmpty(refreshToken)) {
                removeAuthentication(request, response);
                throw new Exception(e);
            }

            reissueToken(response, e.getClaims());
        } catch (Exception e) {
            log.error("", e);
            removeAuthentication(request, response);
            throw new Exception(e);
        }

    }

    private String getRefreshTokenInRedis(String email) {
        return redisUtils.getRedisValue(REFRESH_TOKEN_HEADER_NAME.concat(":").concat(email));
    }

    private void reissueToken(HttpServletResponse response, Claims claims) {
        JwtAuthenticationDto jwtAuthenticationDto = createToken(claims);
        saveAccessTokenToCookie(response, jwtAuthenticationDto.getAccessToken());
        Authentication authentication = getAuthentication(jwtAuthenticationDto.getAccessToken());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        modifyRefreshTokenToRedis(claims, jwtAuthenticationDto.getRefreshToken());
    }

    private void modifyRefreshTokenToRedis(Claims claims, String token) {
        redisUtils.modifyRedisValue(REFRESH_TOKEN_HEADER_NAME.concat(":").concat(claims.getSubject()), token);
    }

    private void removeTokenInCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_HEADER_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public Authentication getAuthentication(String token) {
        Jws<Claims> jwtClaims = this.extractAllClaims(token);
        MemberDetails memberDetails = this.getMemberDetailServiceFromClaims(jwtClaims.getBody());
        return new UsernamePasswordAuthenticationToken(memberDetails, "", memberDetails.getAuthorities());
    }

    private MemberDetails getMemberDetailServiceFromClaims(Claims claims) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (claims.containsKey("authorities")) {
            List<String> authorityList = (List<String>) claims.get("authorities");
            for (String authority : authorityList) {
                authorities.add(new SimpleGrantedAuthority(authority));
            }
        }

        return MemberDetails.builder()
                .email(claims.getSubject())
                .memberSeq(MapUtils.getLongValue(claims, "memeberSeq"))
                .authorities(authorities)
                .build();
    }

    public String resolveTokenInCookie(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN_HEADER_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    public void destroyToken(HttpServletRequest request, HttpServletResponse response) {
        removeAuthentication(request, response);
        setBlackListInRedis(request);
    }

    private void removeAuthentication(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
        removeTokenInCookie(response);
    }

    private void setBlackListInRedis(HttpServletRequest request) {
        String accessToken = resolveTokenInCookie(request);
        if (StringUtils.isNotBlank(accessToken)) {
            Claims claims = getClaims(accessToken);
            modifyRefreshTokenToRedis(claims, LOGOUT);
        }
    }
}
