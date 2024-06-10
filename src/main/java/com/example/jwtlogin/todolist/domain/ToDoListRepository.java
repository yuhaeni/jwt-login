package com.example.jwtlogin.todolist.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ToDoListRepository extends JpaRepository<ToDoList, Long> {
}
