package com.example.jwtlogin.todolist.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import com.example.jwtlogin.member.domain.Member;

@Transactional
public interface ToDoListRepository extends JpaRepository<ToDoList, Long> {
    List<ToDoList> findAllByMemberOrderByCompleteDt(Member member);

}
