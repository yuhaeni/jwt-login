package com.example.jwtlogin.member.domain;

import com.example.jwtlogin.todolist.domain.ToDoList;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Getter
@NoArgsConstructor
@Entity
@Table
@DynamicInsert
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberSeq;

    @Column
    private String role;

    @Column(length = 50, unique = true, nullable = false)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 15, nullable = false)
    private String name;

    @OneToMany(mappedBy = "toDoListSeq", fetch = FetchType.LAZY)
    private List<ToDoList> toDoList = new ArrayList<>();

    @Builder
    public Member(String role, String email, String password, String name) {
        this.role = role;
        this.email = email;
        this.password = password;
        this.name = name;
    }
}
