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

    public ResponseEntity<?> save(ToDoListSaveRequestDto saveRequestDto) {
        ToDoList toDoList = saveRequestDto.toEntity();
        toDoListRepository.save(toDoList);

        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .build());
    }
}
