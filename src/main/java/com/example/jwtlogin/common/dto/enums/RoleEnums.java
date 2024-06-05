package com.example.jwtlogin.common.dto.enums;

public enum RoleEnums {
    ROLE_MEMBER("MEMBER"),
    ROLE_ADMIN("ADMIN");

    String role;

    RoleEnums(String role) {
        this.role = role;
    }

    public String value() {
        return role;
    }
}
