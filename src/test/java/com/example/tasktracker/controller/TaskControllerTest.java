package com.example.tasktracker.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.tasktracker.dto.request.CreateTaskRequest;
import com.example.tasktracker.dto.response.TaskResponse;
import com.example.tasktracker.exception.GlobalExceptionHandler;
import com.example.tasktracker.service.TaskService;

@WebMvcTest(controllers = TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TaskService taskService;

	@Test
	void createReturns201AndBody() throws Exception {
		TaskResponse response = new TaskResponse();
		response.setId(99L);
		response.setTitle("Buy milk");
		response.setDescription(null);
		response.setStatus("PENDING");
		response.setCreatedAt(Instant.parse("2026-03-22T10:00:00Z"));

		when(taskService.create(any(CreateTaskRequest.class))).thenReturn(response);

		mockMvc.perform(post("/v1/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"Buy milk\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(99))
				.andExpect(jsonPath("$.title").value("Buy milk"))
				.andExpect(jsonPath("$.description").value(nullValue()))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.createdAt").exists());
	}

	@Test
	void createWithDescriptionReturns201() throws Exception {
		TaskResponse response = new TaskResponse();
		response.setId(1L);
		response.setTitle("T");
		response.setDescription("D");
		response.setStatus("PENDING");
		response.setCreatedAt(Instant.parse("2026-03-22T10:00:00Z"));

		when(taskService.create(any(CreateTaskRequest.class))).thenReturn(response);

		mockMvc.perform(post("/v1/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"T\",\"description\":\"D\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.description").value("D"));
	}

	@Test
	void createRejectsBlankTitleWith400() throws Exception {
		mockMvc.perform(post("/v1/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.statusCode").value(400))
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors[0].field").value("title"));
	}

	@Test
	void createRejectsOversizedTitle() throws Exception {
		String tooLong = "x".repeat(101);
		String body = "{\"title\":\"" + tooLong + "\"}";
		mockMvc.perform(post("/v1/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("title"));
	}

	@Test
	void createRejectsOversizedDescription() throws Exception {
		String d = "x".repeat(256);
		mockMvc.perform(post("/v1/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"ok\",\"description\":\"" + d + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("description"));
	}
}
