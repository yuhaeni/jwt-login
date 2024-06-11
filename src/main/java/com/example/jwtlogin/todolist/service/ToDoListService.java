package com.example.jwtlogin.todolist.service;

import com.example.jwtlogin.common.dto.ResponseDto;
import com.example.jwtlogin.member.domain.Member;
import com.example.jwtlogin.member.domain.MemberRepository;
import com.example.jwtlogin.todolist.domain.ToDoList;
import com.example.jwtlogin.todolist.domain.ToDoListRepository;
import com.example.jwtlogin.todolist.dto.request.ToDoListSaveRequestDto;
import com.example.jwtlogin.todolist.dto.request.ToDoListSelectRequestDto;
import com.example.jwtlogin.todolist.dto.request.ToDoListUpdateRequestDto;
import com.example.jwtlogin.todolist.dto.response.ToDoListSelectResponseDto;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToDoListService {

    private final ToDoListRepository toDoListRepository;

    private final MemberRepository memberRepository;

    public ResponseEntity<?> save(ToDoListSaveRequestDto saveRequestDto) {
        Member member = memberRepository.findByEmail(saveRequestDto.getEmail());
        saveRequestDto.setMemberSeq(member.getMemberSeq());

        ToDoList toDoList = saveRequestDto.toEntity();
        toDoListRepository.save(toDoList);

        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .build());
    }

    @Transactional
    public ResponseEntity<?> update(ToDoListUpdateRequestDto updateDto) {
        try {
            ToDoList toDoList = toDoListRepository.findById(updateDto.getToDoListSeq())
                    .orElseThrow(NoSuchElementException::new);
            toDoList.updateToDoList(updateDto);
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.ok(ResponseDto.builder()
                    .result(false)
                    .build());
        }

        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .build());
    }

    public ResponseEntity<?> select(@RequestBody ToDoListSelectRequestDto requestDto) {
        List<ToDoList> toDoLists = toDoListRepository.findAllByMemberSeq(requestDto.getMemberSeq());
        List<ToDoListSelectResponseDto> responseDto = new ToDoList().toDto(toDoLists);

        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .data(responseDto)
                .build());
    }

}
