package com.example.tasktracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tasktracker.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
