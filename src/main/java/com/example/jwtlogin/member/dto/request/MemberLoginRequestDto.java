package com.example.jwtlogin.member.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
public class MemberLoginRequestDto {

    String email;
    String password;

    @Builder
    public MemberLoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
