package com.example.jwtlogin.todolist.domain;

import com.example.jwtlogin.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@NoArgsConstructor
@Entity
@Table
public class ToDoList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long toDoListSeq;

    @Column(nullable = false)
    private Date completeDt;

    @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "char(1) default('N')")
    private String completeYn;

    @CreatedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
    private LocalDateTime regDt;

    @LastModifiedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
    private LocalDateTime modifyDt;

    @ManyToOne
    @JoinColumn(name = "memeberSeq")
    private Member memeber;

    @Builder
    public ToDoList(Date completeDt, String content, String completeYn) {
        this.completeDt = completeDt;
        this.content = content;
        this.completeYn = completeYn;
    }
}
