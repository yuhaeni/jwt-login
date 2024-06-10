package com.example.jwtlogin.todolist.service;

import com.example.jwtlogin.common.dto.ResponseDto;
import com.example.jwtlogin.member.domain.Member;
import com.example.jwtlogin.member.domain.MemberRepository;
import com.example.jwtlogin.security.jwt.JwtAuthenticationProvider;
import com.example.jwtlogin.todolist.domain.ToDoList;
import com.example.jwtlogin.todolist.domain.ToDoListRepository;
import com.example.jwtlogin.todolist.dto.request.ToDoListSaveRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToDoListService {

    private final ToDoListRepository toDoListRepository;

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    private final MemberRepository memberRepository;

    public ResponseEntity<?> save(ToDoListSaveRequestDto saveRequestDto) {
        try {
            Member member = getMemeber(saveRequestDto.getJwtToken());
            saveRequestDto.setEmail(member.getEmail());

            ToDoList toDoList = saveRequestDto.toEntity();
            toDoListRepository.save(toDoList);
        } catch (BadRequestException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .build());
    }

    private Member getMemeber(String token) throws BadRequestException {
        Authentication authentication = jwtAuthenticationProvider.getAuthentication(token);
        Long id = Long.parseLong(authentication.getName());

        return memberRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 회원입니다."));
    }
}
