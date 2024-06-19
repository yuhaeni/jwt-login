package com.example.jwtlogin.member.controller;

import com.example.jwtlogin.common.dto.ResponseDto;
import com.example.jwtlogin.member.dto.request.MemberLoginRequestDto;
import com.example.jwtlogin.member.dto.request.MemberSaveRequestDto;
import com.example.jwtlogin.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClientController {

    private final MemberService memberService;

    @GetMapping(value = "/")
    public ResponseEntity<?> getMain() {
        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .status(HttpServletResponse.SC_OK)
                .build());
    }

    @PostMapping(value = "/join")
    public ResponseEntity<?> saveMember(@RequestBody MemberSaveRequestDto memberSaveRequestDto) {
        return memberService.save(memberSaveRequestDto);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> loginMember(@RequestBody MemberLoginRequestDto memberLoginRequestDto,
                                         HttpServletResponse response) {
        return memberService.login(memberLoginRequestDto, response);
    }
}