package com.example.tasktracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tasktracker.dto.request.CreateTaskRequest;
import com.example.tasktracker.dto.response.TaskResponse;
import com.example.tasktracker.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@PostMapping
	public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
		TaskResponse body = taskService.create(request);
		return ResponseEntity.status(201).body(body);
	}
}
