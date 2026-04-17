package com.backend.backend.dto.task;

import com.backend.backend.dao.entities.*;
import com.backend.backend.dao.enums.Priority;
import com.backend.backend.dao.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class TaskResponseDto {
    private String id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String listeId;
    private String listeName;

    private String sprintId;
    private String sprintName;

    private String assigneeId;
    private String assigneeName;


//    private List<String> tagIds;
//    private List<String> tagNames;

}
