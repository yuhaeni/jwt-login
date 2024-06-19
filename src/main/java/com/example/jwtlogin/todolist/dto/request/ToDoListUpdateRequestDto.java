package com.example.jwtlogin.todolist.dto.request;

import java.time.LocalDate;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ToDoListUpdateRequestDto {

    private Long toDoListSeq;

    private String content;

    private LocalDate completeDt;

    @Pattern(regexp = "^[YN]$", message = "Invalid completeYn. Should be 'Y' or 'N'.")
    private String completeYn;

}
