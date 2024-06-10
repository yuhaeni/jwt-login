package com.example.jwtlogin.todolist.dto.request;

import com.example.jwtlogin.todolist.domain.ToDoList;
import java.util.Date;
import lombok.Data;

@Data
public class ToDoListSaveRequestDto {

    private String jwtToken;
    private String email;
    private String content;
    private Date completeDt;

    public ToDoList toEntity() {
        return ToDoList.builder()
                .content(this.content)
                .completeDt(this.completeDt)
                .build();
    }
}
