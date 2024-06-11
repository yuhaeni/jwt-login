package com.example.jwtlogin.todolist.dto.response;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
public class ToDoListSelectResponseDto {

    private String content;

    private Date completeDt;

    private String completeYn;

    @Builder
    public ToDoListSelectResponseDto(String content, Date completeDt, String completeYn) {
        this.content = content;
        this.completeDt = completeDt;
        this.completeYn = completeYn;
    }
}
