package com.backend.backend.web;

import com.backend.backend.dto.task.TaskRequestDto;
import com.backend.backend.dto.task.TaskResponseDto;
import com.backend.backend.service.manager.TaskManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskManager taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody TaskRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(requestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable String id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/liste/{listeId}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByListe(@PathVariable String listeId) {
        return ResponseEntity.ok(taskService.getTasksByListe(listeId));
    }

    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<List<TaskResponseDto>> getTasksBySprint(@PathVariable String sprintId) {
        return ResponseEntity.ok(taskService.getTasksBySprint(sprintId));
    }

    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByAssignee(@PathVariable String assigneeId) {
        return ResponseEntity.ok(taskService.getTasksByAssignee(assigneeId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable String id,
            @RequestBody TaskRequestDto requestDto) {
        return ResponseEntity.ok(taskService.updateTask(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/assign/{assigneeId}")
    public ResponseEntity<TaskResponseDto> assignTask(
            @PathVariable String taskId,
            @PathVariable String assigneeId) {
        return ResponseEntity.ok(taskService.assignTask(taskId, assigneeId));
    }

    @PatchMapping("/{taskId}/sprint/{sprintId}")
    public ResponseEntity<TaskResponseDto> moveToSprint(
            @PathVariable String taskId,
            @PathVariable String sprintId) {
        return ResponseEntity.ok(taskService.moveToSprint(taskId, sprintId));
    }

}
