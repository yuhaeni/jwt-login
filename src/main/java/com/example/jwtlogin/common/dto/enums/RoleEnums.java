package com.example.jwtlogin.common.dto.enums;

public enum RoleEnums {
    ROLE_MEMBER("ROLE_MEMBER"),
    ROLE_ADMIN("ROLE_ADMIN");

    String role;

    RoleEnums(String role) {
        this.role = role;
    }

    public String value() {
        return role;
    }
}
