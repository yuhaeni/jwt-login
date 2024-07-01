package com.example.jwtlogin.todolist.controller;

import com.example.jwtlogin.todolist.dto.request.ToDoListSaveRequestDto;
import com.example.jwtlogin.todolist.dto.request.ToDoListUpdateRequestDto;
import com.example.jwtlogin.todolist.service.ToDoListService;
import jakarta.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/todolist")
public class ToDoListController {

    private final ToDoListService toDoListService;

    @PostMapping
    public ResponseEntity<?> saveToDoList(@RequestBody ToDoListSaveRequestDto saveRequestDto, HttpServletRequest request) {
        return toDoListService.save(saveRequestDto, request);
    }

    @PutMapping
    public ResponseEntity<?> updateToDoList(@RequestBody @Valid ToDoListUpdateRequestDto updateRequestDto, HttpServletRequest request) {
        return toDoListService.update(updateRequestDto, request);
    }

    @GetMapping
    public ResponseEntity<?> selectToDoList(HttpServletRequest request) {
        return toDoListService.select(request);
    }

}
