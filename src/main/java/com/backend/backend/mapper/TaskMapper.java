package com.backend.backend.mapper;

import com.backend.backend.dao.entities.Task;
import com.backend.backend.dto.task.TaskRequestDto;
import com.backend.backend.dto.task.TaskResponseDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    @Autowired
    private ModelMapper modelMapper;

    public TaskResponseDto toDto(Task task) {
        if (task == null) {
            return null;
        }
        TaskResponseDto dto = modelMapper.map(task, TaskResponseDto.class);

        if (task.getListe() != null) {
            dto.setListeId(task.getListe().getId());
            dto.setListeName(task.getListe().getName());
        }

        if (task.getSprint() != null) {
            dto.setSprintId(task.getSprint().getId());
            dto.setSprintName(task.getSprint().getName());
        }

        if (task.getAssignee() != null) {
            dto.setAssigneeId(task.getAssignee().getId());
            dto.setAssigneeName(task.getAssignee().getName());
        }

        return dto;
    }

    public Task toEntity(TaskRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        Task task = new Task();

        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        task.setStatus(requestDto.getStatus());
        task.setPriority(requestDto.getPriority());
        task.setDueDate(requestDto.getDueDate());

        return task;
    }
}
