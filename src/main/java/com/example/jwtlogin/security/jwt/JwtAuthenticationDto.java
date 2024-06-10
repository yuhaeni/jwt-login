package com.example.jwtlogin.security.jwt;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
public class JwtAuthenticationDto {

    private String accessToken;

    private String accessTokenExpirationDateStr;

    private Date accessTokenExpirationDate;

    private String refreshToken;

    private String refreshTokenExpirationDateStr;

    private Date refreshTokenExpirationDate;

    @Builder
    public JwtAuthenticationDto(String accessToken, String accessTokenExpirationDateStr, Date accessTokenExpirationDate,
                                String refreshToken, String refreshTokenExpirationDateStr,
                                Date refreshTokenExpirationDate) {
        this.accessToken = accessToken;
        this.accessTokenExpirationDateStr = accessTokenExpirationDateStr;
        this.accessTokenExpirationDate = accessTokenExpirationDate;
        this.refreshToken = refreshToken;
        this.refreshTokenExpirationDateStr = refreshTokenExpirationDateStr;
        this.refreshTokenExpirationDate = refreshTokenExpirationDate;
    }
}
