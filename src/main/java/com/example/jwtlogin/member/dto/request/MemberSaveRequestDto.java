package com.example.jwtlogin.member.dto.request;

import com.example.jwtlogin.common.dto.enums.RoleEnums;
import com.example.jwtlogin.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
public class MemberSaveRequestDto {

    @NotBlank(message = "Email is required.")
    @Email
    private String email;

    @NotBlank(message = "Password is required.")
    private String password;

    @NotBlank(message = "Name is required.")
    private String name;

    private RoleEnums role;

    @Builder
    public MemberSaveRequestDto(String email, String password, String name, RoleEnums role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public Member toEntity() {
        return Member.builder()
                .email(email)
                .password(password)
                .name(name)
                .role(role)
                .build();
    }
}
