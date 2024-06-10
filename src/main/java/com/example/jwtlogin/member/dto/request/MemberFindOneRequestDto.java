package com.example.jwtlogin.member.dto.request;

import com.example.jwtlogin.member.domain.Member;
import lombok.Builder;
import lombok.Data;

@Data
public class MemberFindOneRequestDto {

    String email;

    @Builder
    public MemberFindOneRequestDto(String email) {
        this.email = email;
    }

    public Member toEntity() {
        return Member.builder()
                .email(email)
                .build();
    }
}
