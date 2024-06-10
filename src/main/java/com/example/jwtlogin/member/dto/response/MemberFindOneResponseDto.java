package com.example.jwtlogin.member.dto.response;

import lombok.Builder;

public class MemberFindOneResponseDto {

    private Long id;

    @Builder
    public MemberFindOneResponseDto(Long id) {
        this.id = id;
    }
}
