package com.example.tasktracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.tasktracker.dto.request.CreateTaskRequest;
import com.example.tasktracker.dto.response.TaskResponse;
import com.example.tasktracker.entity.Task;
import com.example.tasktracker.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@InjectMocks
	private TaskService taskService;

	private Instant fixedNow;

	@BeforeEach
	void setUp() {
		fixedNow = Instant.parse("2026-03-22T12:00:00Z");
	}

	@Test
	void createPersistsTitleOnlyAndReturnsPending() {
		CreateTaskRequest req = new CreateTaskRequest();
		req.setTitle("Buy milk");

		when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
			Task t = invocation.getArgument(0);
			t.setId(1L);
			t.setCreatedAt(fixedNow);
			return t;
		});

		TaskResponse response = taskService.create(req);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("Buy milk");
		assertThat(response.getDescription()).isNull();
		assertThat(response.getStatus()).isEqualTo("PENDING");
		assertThat(response.getCreatedAt()).isEqualTo(fixedNow);

		ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
		verify(taskRepository).save(captor.capture());
		assertThat(captor.getValue().getTitle()).isEqualTo("Buy milk");
		assertThat(captor.getValue().getDescription()).isNull();
		assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
	}

	@Test
	void createTrimsTitleAndDescription() {
		CreateTaskRequest req = new CreateTaskRequest();
		req.setTitle("  Trim me  ");
		req.setDescription("  notes  ");

		when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
			Task t = invocation.getArgument(0);
			t.setId(2L);
			t.setCreatedAt(fixedNow);
			return t;
		});

		TaskResponse response = taskService.create(req);

		assertThat(response.getTitle()).isEqualTo("Trim me");
		assertThat(response.getDescription()).isEqualTo("notes");
	}

	@Test
	void createTrimsDescriptionToNullWhenBlank() {
		CreateTaskRequest req = new CreateTaskRequest();
		req.setTitle("T");
		req.setDescription("   ");

		when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
			Task t = invocation.getArgument(0);
			t.setId(3L);
			t.setCreatedAt(fixedNow);
			return t;
		});

		TaskResponse response = taskService.create(req);

		assertThat(response.getDescription()).isNull();
		ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
		verify(taskRepository).save(captor.capture());
		assertThat(captor.getValue().getDescription()).isNull();
	}

	@Test
	void createWithTitleAndDescription() {
		CreateTaskRequest req = new CreateTaskRequest();
		req.setTitle("Task");
		req.setDescription("Longer text");

		when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
			Task t = invocation.getArgument(0);
			t.setId(4L);
			t.setCreatedAt(fixedNow);
			return t;
		});

		TaskResponse response = taskService.create(req);

		assertThat(response.getTitle()).isEqualTo("Task");
		assertThat(response.getDescription()).isEqualTo("Longer text");
	}
}
