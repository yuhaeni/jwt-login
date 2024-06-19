package com.example.jwtlogin.todolist.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ToDoListRepository extends JpaRepository<ToDoList, Long> {
    List<ToDoList> findAllByMemberSeqOrderByCompleteDt(Long memberSeq);
}
