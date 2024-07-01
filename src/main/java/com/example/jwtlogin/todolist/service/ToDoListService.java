package com.example.jwtlogin.todolist.service;

import com.example.jwtlogin.common.dto.ResponseDto;
import com.example.jwtlogin.member.domain.Member;
import com.example.jwtlogin.member.domain.MemberRepository;
import com.example.jwtlogin.security.MemberDetails;
import com.example.jwtlogin.todolist.domain.ToDoList;
import com.example.jwtlogin.todolist.domain.ToDoListRepository;
import com.example.jwtlogin.todolist.dto.request.ToDoListSaveRequestDto;
import com.example.jwtlogin.todolist.dto.request.ToDoListUpdateRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToDoListService {

    private final ToDoListRepository toDoListRepository;

    private final MemberRepository memberRepository;

    public ResponseEntity<?> save(ToDoListSaveRequestDto saveRequestDto, HttpServletRequest request) {
        MemberDetails memberDetails = (MemberDetails) request.getAttribute("_memberDetails");
        if (memberDetails == null) {
            throw new RuntimeException("로그인 필요");
        }

        Member member = memberRepository.findById(memberDetails.getMemberSeq())
                .orElseThrow();
        saveRequestDto.setMember(member);

        ToDoList toDoList = saveRequestDto.toEntity();
        toDoListRepository.save(toDoList);

        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .status(HttpServletResponse.SC_OK)
                .build());
    }

    @Transactional
    public ResponseEntity<?> update(ToDoListUpdateRequestDto updateDto, HttpServletRequest request) {
        MemberDetails memberDetails = (MemberDetails) request.getAttribute("_memberDetails");
        if (memberDetails == null) {
            throw new RuntimeException("로그인 필요");
        }

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
                .status(HttpServletResponse.SC_OK)
                .build());
    }

    public ResponseEntity<?> select(HttpServletRequest request) {
        MemberDetails memberDetails = (MemberDetails) request.getAttribute("_memberDetails");
        if (memberDetails == null) {
            throw new RuntimeException("로그인 필요");
        }

        Member member = memberRepository.findById(memberDetails.getMemberSeq())
                .orElseThrow();
        List<ToDoList> toDoLists = toDoListRepository.findAllByMemberOrderByCompleteDt(member);

        return ResponseEntity.ok(ResponseDto.builder()
                .result(true)
                .data(new ToDoList().toDto(toDoLists))
                .build());
    }

}
