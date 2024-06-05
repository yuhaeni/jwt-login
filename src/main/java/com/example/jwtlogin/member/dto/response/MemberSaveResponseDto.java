package com.example.jwtlogin.member.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSaveResponseDto {
    private String email;
    private String name;

    @Builder
    public MemberSaveResponseDto(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
