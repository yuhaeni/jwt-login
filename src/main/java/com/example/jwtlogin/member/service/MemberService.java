package com.example.jwtlogin.member.service;

import com.example.jwtlogin.common.dto.ResponseDto;
import com.example.jwtlogin.common.dto.enums.RoleEnums;
import com.example.jwtlogin.member.domain.Member;
import com.example.jwtlogin.member.domain.MemberRepository;
import com.example.jwtlogin.member.dto.request.MemberLoginRequestDto;
import com.example.jwtlogin.member.dto.request.MemberSaveRequestDto;
import com.example.jwtlogin.member.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final JwtTokenProvider jwtTokenProvider;

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

    public ResponseEntity<?> login(MemberLoginRequestDto loginRequestDto) {
        Member member = memberRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 회원입니다."));
        if (!member.getPassword().equals(loginRequestDto.getPassword())) {
            return ResponseEntity.ok(ResponseDto.builder()
                    .result(false)
                    .status(HttpServletResponse.SC_OK)
                    .message("잘못된 비밀번호 입니다.")
                    .build());
        }

        String jwtToken = jwtTokenProvider.createJwt(member.getMemberSeq());
        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .status(HttpServletResponse.SC_OK)
                .data(jwtToken)
                .build());
    }
}
