package com.backend.backend.service.serviceInterface;

import com.backend.backend.dto.task.TaskRequestDto;
import com.backend.backend.dto.task.TaskResponseDto;

import java.util.List;

public interface ITaskService {
    TaskResponseDto createTask(TaskRequestDto requestDto);

    TaskResponseDto getTaskById(String id);

    List<TaskResponseDto> getAllTasks();

    List<TaskResponseDto> getTasksByListe(String listeId);

    List<TaskResponseDto> getTasksBySprint(String sprintId);

    List<TaskResponseDto> getTasksByAssignee(String assigneeId);

    TaskResponseDto updateTask(String id, TaskRequestDto requestDto);

    boolean deleteTask(String id);

    TaskResponseDto assignTask(String taskId, String assigneeId);

    TaskResponseDto moveToSprint(String taskId, String sprintId);
}
