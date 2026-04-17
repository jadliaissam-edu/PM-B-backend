package com.backend.backend.dao.repositories;

import com.backend.backend.dao.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByListeId(String listeId);
    List<Task> findBySprintId(String sprintId);
    List<Task> findByAssigneeId(String assigneeId);
}
