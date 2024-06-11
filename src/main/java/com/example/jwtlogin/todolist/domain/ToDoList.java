package com.example.jwtlogin.todolist.domain;

import com.example.jwtlogin.todolist.dto.request.ToDoListUpdateRequestDto;
import com.example.jwtlogin.todolist.dto.response.ToDoListSelectResponseDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@NoArgsConstructor
@Entity
@Table
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public class ToDoList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long toDoListSeq;

    @Column(nullable = false)
    private Date completeDt;

    @Column(nullable = false)
    private String content;

    @Column
    private String completeYn;

    @Column(updatable = false)
    @CreatedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
    private LocalDateTime regDt;

    @LastModifiedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
    private LocalDateTime modifyDt;

    @Column
    private Long memberSeq;

    @Builder
    public ToDoList(Long toDoListSeq, Date completeDt, String content, String completeYn, Long memeberSeq) {
        this.toDoListSeq = toDoListSeq;
        this.completeDt = completeDt;
        this.content = content;
        this.completeYn = completeYn;
        this.memberSeq = memeberSeq;
    }

    public void updateToDoList(ToDoListUpdateRequestDto updateDto) {
        if (updateDto.getCompleteDt() != null) {
            this.completeDt = updateDto.getCompleteDt();
        }
        if (updateDto.getCompleteYn() != null) {
            this.completeYn = updateDto.getCompleteYn();
        }
        if (updateDto.getContent() != null) {
            this.content = updateDto.getContent();
        }
    }

    public List<ToDoListSelectResponseDto> toDto(List<ToDoList> toDoList) {
        List<ToDoListSelectResponseDto> responseDtos = new ArrayList<>();
        for (ToDoList item : toDoList) {
            responseDtos.add(ToDoListSelectResponseDto.builder()
                    .completeDt(item.getCompleteDt())
                    .completeYn(item.getCompleteYn())
                    .content(item.getContent())
                    .build());
        }

        return responseDtos;
    }
}