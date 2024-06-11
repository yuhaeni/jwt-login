package com.example.jwtlogin.todolist.dto.request;

import com.example.jwtlogin.member.domain.Member;
import com.example.jwtlogin.todolist.domain.ToDoList;
import java.util.Date;
import lombok.Data;

@Data
public class ToDoListSaveRequestDto {

    private String email;

    private String content;

    private Date completeDt;

    private Long memberSeq;

    public ToDoList toEntity() {
        return ToDoList.builder()
                .content(this.content)
                .completeDt(this.completeDt)
                .memeberSeq(this.memberSeq)
                .build();
    }
}
