package com.example.jwtlogin.todolist.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
public class ToDoListSelectResponseDto {

    private String content;

    private LocalDate completeDt;

    private String completeYn;

    @Builder
    public ToDoListSelectResponseDto(String content, LocalDate completeDt, String completeYn) {
        this.content = content;
        this.completeDt = completeDt;
        this.completeYn = completeYn;
    }
}
