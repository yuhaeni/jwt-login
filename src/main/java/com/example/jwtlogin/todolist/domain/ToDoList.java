package com.example.jwtlogin.todolist.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table
public class ToDoList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long toDoListSeq;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "char(1) default('N')")
    private String completeYn;
}
