package com.example.tasktracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tasktracker.dto.request.CreateTaskRequest;
import com.example.tasktracker.dto.response.TaskResponse;
import com.example.tasktracker.entity.Task;
import com.example.tasktracker.repository.TaskRepository;

@Service
public class TaskService {

	private final TaskRepository taskRepository;

	public TaskService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Transactional
	public TaskResponse create(CreateTaskRequest request) {
		String title = request.getTitle() == null ? "" : request.getTitle().trim();
		String description = request.getDescription();
		if (description != null) {
			description = description.trim();
			if (description.isEmpty()) {
				description = null;
			}
		}

		Task task = new Task();
		task.setTitle(title);
		task.setDescription(description);
		task.setStatus("PENDING");

		Task saved = taskRepository.save(task);
		return toResponse(saved);
	}

	private static TaskResponse toResponse(Task task) {
		TaskResponse r = new TaskResponse();
		r.setId(task.getId());
		r.setTitle(task.getTitle());
		r.setDescription(task.getDescription());
		r.setStatus(task.getStatus());
		r.setCreatedAt(task.getCreatedAt());
		return r;
	}
}
