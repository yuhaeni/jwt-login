package com.example.jwtlogin.security;

import com.example.jwtlogin.common.dto.enums.RoleEnums;
import java.util.Collection;
import java.util.HashSet;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
public class MemberDetails implements UserDetails {

    private long memberSeq;
    private String email;
    String password;

    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        HashSet<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(RoleEnums.ROLE_MEMBER.value()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return String.valueOf(email);
    }

    public long getMemberSeq() {
        return memberSeq;
    }

    public MemberDetails(String email) {
        this.email = email;
    }

    @Builder
    public MemberDetails(long memberSeq, String email, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.memberSeq = memberSeq;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }
}
