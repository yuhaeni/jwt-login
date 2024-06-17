package com.example.jwtlogin.member.service;

import com.example.jwtlogin.common.dto.ResponseDto;
import com.example.jwtlogin.common.dto.enums.RoleEnums;
import com.example.jwtlogin.member.domain.Member;
import com.example.jwtlogin.member.domain.MemberRepository;
import com.example.jwtlogin.member.dto.request.MemberLoginRequestDto;
import com.example.jwtlogin.member.dto.request.MemberSaveRequestDto;
import com.example.jwtlogin.security.MemberDetailService;
import com.example.jwtlogin.security.MemberDetails;
import com.example.jwtlogin.security.jwt.JwtAuthenticationProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberDetailService memberDetailService;

    private final MemberRepository memberRepository;

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    public Member findById(Long id) {
        // TODO 예외처리 상세하게 필요
        return memberRepository.findById(id).orElseThrow();
    }

    @Transactional
    public ResponseEntity<?> save(MemberSaveRequestDto saveRequestDto) {
        // TODO return 공통 함수 만들기
        if (existsByEmail(saveRequestDto.getEmail())) {
            return ResponseEntity.ok(ResponseDto.builder()
                    .result(false)
                    .status(HttpServletResponse.SC_OK)
                    .message("중복된 email 입니다.")
                    .data(saveRequestDto)
                    .build());
        }

        saveRequestDto.setRole(RoleEnums.ROLE_MEMBER.value());
        Long memberSeq = memberRepository.save(saveRequestDto.toEntity()).getMemberSeq();
        if (memberSeq == null) {
            return ResponseEntity.ok(ResponseDto.builder()
                    .result(false)
                    .status(HttpServletResponse.SC_OK)
                    .message("회원가입 실패")
                    .data(saveRequestDto)
                    .build());
        }

        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .status(HttpServletResponse.SC_OK)
                .build());
    }

    @Transactional
    public boolean existsByEmail(String email) {
        return memberRepository.existsMemberByEmail(email);
    }

    public ResponseEntity<?> login(MemberLoginRequestDto loginRequestDto, HttpServletResponse response) {

        MemberDetails memberDetails = null;
        try {
            memberDetails = memberDetailService.loadUserByUsername(loginRequestDto.getEmail());
        } catch (UsernameNotFoundException e) {
            log.error("", e);
            return ResponseEntity.ok(ResponseDto.builder()
                    .result(false)
                    .status(HttpServletResponse.SC_OK)
                    .message("아이디 또는 비밀번호가 올바르지 않습니다.")
                    .build());
        }

        Claims claims = jwtAuthenticationProvider.buildClaims(memberDetails.getEmail(), memberDetails.getMemberSeq(),
                AuthorityUtils.authorityListToSet(memberDetails.getAuthorities()));
        jwtAuthenticationProvider.issueToken(response, claims);

        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .status(HttpServletResponse.SC_OK)
                .build());
    }

}
