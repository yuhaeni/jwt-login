package com.example.jwtlogin.member.domain;

import com.example.jwtlogin.common.dto.enums.RoleEnums;
import com.example.jwtlogin.todolist.domain.ToDoList;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor
@Entity
@Table
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberSeq;

    @Enumerated(EnumType.STRING)
    private RoleEnums role;

    @Column(length = 50, unique = true, nullable = false)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 15, nullable = false)
    private String name;

    @OneToMany
    private List<ToDoList> toDoList = new ArrayList<>();

    @Builder
    public Member(RoleEnums role, String email, String password, String name) {
        this.role = role;
        this.email = email;
        this.password = password;
        this.name = name;
    }
}
