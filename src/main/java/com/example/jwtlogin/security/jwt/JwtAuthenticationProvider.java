package com.example.jwtlogin.security.jwt;

import com.example.jwtlogin.common.util.AES256;
import com.example.jwtlogin.redis.util.RedisUtils;
import com.example.jwtlogin.security.MemberDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${jwt.encrypt-key}")
    private String ENCRYPT_KEY;

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

    /**
     * build 클레임
     *
     * @param memberSeq
     * @param authorities
     * @return
     */
    public Claims buildClaims(long memberSeq, Set<String> authorities) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(memberSeq));
        claims.put("authorities", authorities);

        return claims;
    }

    /**
     * 토큰 발급
     *
     * @param response
     * @param claims
     */
    public void issueToken(HttpServletResponse response, Claims claims) {
        JwtAuthenticationDto jwtAuthenticationDto = createToken(claims);
        saveAccessTokenToHeader(response, jwtAuthenticationDto.getAccessToken());
        saveRefreshTokenToRedis(claims, jwtAuthenticationDto.getRefreshToken());

        Authentication authentication = getAuthentication(jwtAuthenticationDto.getRefreshToken());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 토큰을 헤더에 저장
     *
     * @param response
     * @param token
     */
    public void saveAccessTokenToHeader(HttpServletResponse response, String token) {
        response.setHeader(ACCESS_TOKEN_HEADER_NAME, encryptToken(token));
    }

    /**
     * 토큰을 암호화
     *
     * @param token
     * @return
     */
    private String encryptToken(String token) {
        String encryptText = StringUtils.EMPTY;
        if (
                StringUtils.isNotBlank(ENCRYPT_KEY)
                        && StringUtils.isNotBlank(token)
        ) {
            try {
                AES256 aes256 = new AES256(ENCRYPT_KEY);
                encryptText = URLEncoder.encode(aes256.encrypt(token), StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("", e);
            }
        }

        return encryptText;
    }

    /**
     * refresh-token redis에 저장
     *
     * @param claims
     * @param token
     */
    private void saveRefreshTokenToRedis(Claims claims, String token) {
        redisUtils.setRedisValueWithTimeout(
                REFRESH_TOKEN_HEADER_NAME.concat(":").concat(claims.getSubject())
                , encryptToken(token)
                , REFRESH_EXPIRE_MILLISECONDS
        );
    }

    /**
     * 토큰 생성
     *
     * @param claims
     * @return
     */
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

    /**
     * 토큰 발급
     *
     * @param claims
     * @param now
     * @param expirationDate
     * @return
     */
    private String generateToken(Claims claims, Date now, Date expirationDate) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }

    /**
     * 토큰 검증
     *
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e;
        }
    }

    /**
     * 토큰 Claims 호출
     *
     * @param token
     * @return
     * @throws ExpiredJwtException
     */
    public Jws<Claims> extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰 검증
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public void validateFilterToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String accessToken = resolveAccessTokenInHeader(request);
        if (
                StringUtils.isNotBlank(accessToken)
                        &&
                        StringUtils.startsWith(accessToken, "Bearer ")
        ) {
            accessToken = StringUtils.replace(StringUtils.trim(accessToken), "Bearer ", "");
        } else {
            accessToken = StringUtils.EMPTY;
        }

        accessToken = decryptToken(accessToken);

        try {
            if (
                    StringUtils.isNotBlank(accessToken)
                            &&
                            validateToken(accessToken)
            ) {
                Authentication authentication = getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (ExpiredJwtException e) {
            String subject = e.getClaims().getSubject();
            String refreshToken = getRefreshTokenInRedis(subject);
            if (StringUtils.isBlank(refreshToken)) {
                removeAuthentication(request, response);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }

            refreshToken = decryptToken(refreshToken);
            try {
                if (validateToken(refreshToken)) {
                    reissueToken(response, e.getClaims());
                } else {
                    removeAuthentication(request, response);
                }
            } catch (Exception ex) {
                log.error("", e);
                removeAuthentication(request, response);
                throw new RuntimeException(ex);
            }

        } catch (Exception e) {
            log.error("", e);
            removeAuthentication(request, response);
            throw new Exception(e);
        }
    }

    public String resolveAccessTokenInHeader(HttpServletRequest request) {
        return request.getHeader(ACCESS_TOKEN_HEADER_NAME);
    }

    /**
     * 토큰을 복호화
     *
     * @param encryptToken
     * @return
     */
    public String decryptToken(String encryptToken) {
        String token = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(ENCRYPT_KEY)
                && StringUtils.isNotBlank(encryptToken)
        ) {
            try {
                AES256 aes256 = new AES256(ENCRYPT_KEY);
                encryptToken = URLDecoder.decode(encryptToken, StandardCharsets.UTF_8);
                token = aes256.decrypt(encryptToken);
            } catch (Exception e) {
                log.error("", e);
            }
        } else {
            token = encryptToken;
        }

        return token;
    }

    public String getRefreshTokenInRedis(String subject) {
        return redisUtils.getRedisValue(REFRESH_TOKEN_HEADER_NAME.concat(":").concat(subject));
    }

    public void reissueToken(HttpServletResponse response, Claims claims) {
        JwtAuthenticationDto jwtAuthenticationDto = createToken(claims);
        saveAccessTokenToHeader(response, jwtAuthenticationDto.getAccessToken());
        modifyRefreshTokenToRedis(claims, jwtAuthenticationDto.getRefreshToken());

        Authentication authentication = getAuthentication(jwtAuthenticationDto.getAccessToken());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void modifyRefreshTokenToRedis(Claims claims, String token) {
        redisUtils.modifyRedisValue(REFRESH_TOKEN_HEADER_NAME.concat(":").concat(claims.getSubject()), token);
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
                .memberSeq(Long.parseLong(claims.getSubject()))
                .authorities(authorities)
                .build();
    }

    public void destroyToken(HttpServletRequest request, HttpServletResponse response) {
        removeAuthentication(request, response);
        setBlackListInRedis(request);
    }

    public void removeAuthentication(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
    }

    private void setBlackListInRedis(HttpServletRequest request) {
        String accessToken = resolveAccessTokenInHeader(request);
        if (
                StringUtils.isNotBlank(accessToken)
                        &&
                        StringUtils.startsWith(accessToken, "Bearer ")
        ) {
            accessToken = StringUtils.replace(StringUtils.trim(accessToken), "Bearer ", "");
        }

        if (StringUtils.isNotBlank(accessToken)) {
            Claims claims = getClaims(accessToken);
            modifyRefreshTokenToRedis(claims, LOGOUT);
        }
    }
}
