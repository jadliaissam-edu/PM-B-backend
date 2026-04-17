package com.backend.backend.dto.task;

import com.backend.backend.dao.enums.Priority;
import com.backend.backend.dao.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskRequestDto {
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDateTime dueDate;

    private String listeId;

    private String sprintId;

    private String assigneeId;


    // private List<String> tagIds;
}
