package com.example.jwtlogin.todolist.dto.request;

import java.util.Date;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ToDoListUpdateRequestDto {

    private Long toDoListSeq;

    private String content;

    private Date completeDt;

    @Pattern(regexp = "^[YN]$", message = "Invalid completeYn. Should be 'Y' or 'N'.")
    private String completeYn;

}
