package com.example.jwtlogin.todolist.controller;

import com.example.jwtlogin.todolist.dto.request.ToDoListSaveRequestDto;
import com.example.jwtlogin.todolist.dto.request.ToDoListSelectRequestDto;
import com.example.jwtlogin.todolist.dto.request.ToDoListUpdateRequestDto;
import com.example.jwtlogin.todolist.service.ToDoListService;
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
    public ResponseEntity<?> saveToDoList(@RequestBody ToDoListSaveRequestDto saveRequestDto) {
        return toDoListService.save(saveRequestDto);
    }

    @PutMapping
    public ResponseEntity<?> updateToDoList(@Valid @RequestBody ToDoListUpdateRequestDto updateRequestDto) {
        return toDoListService.update(updateRequestDto);
    }

    @GetMapping
    public ResponseEntity<?> selectToDoList(@RequestBody ToDoListSelectRequestDto selectRequestDto) {
        return toDoListService.select(selectRequestDto);
    }

}
