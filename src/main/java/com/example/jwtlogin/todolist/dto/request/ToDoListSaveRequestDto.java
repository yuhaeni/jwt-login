package com.example.jwtlogin.todolist.dto.request;

import com.example.jwtlogin.member.domain.Member;
import com.example.jwtlogin.todolist.domain.ToDoList;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ToDoListSaveRequestDto {

    private String content;

    private LocalDate completeDt;

    private Member member;

    public ToDoList toEntity() {
        return ToDoList.builder()
                .content(this.content)
                .completeDt(this.completeDt)
                .member(this.member)
                .build();
    }
}
