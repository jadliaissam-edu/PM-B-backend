package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.Liste;
import com.backend.backend.dao.entities.Sprint;
import com.backend.backend.dao.entities.Task;
import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.repositories.ListeRepository;
import com.backend.backend.dao.repositories.SprintRepository;
import com.backend.backend.dao.repositories.TaskRepository;
import com.backend.backend.dao.repositories.UserRepository;
import com.backend.backend.dto.task.TaskRequestDto;
import com.backend.backend.dto.task.TaskResponseDto;
import com.backend.backend.mapper.TaskMapper;
import com.backend.backend.service.serviceInterface.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskManager implements ITaskService {

    private final TaskMapper taskMapper;
    private final ListeRepository listeRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Override
    public TaskResponseDto createTask(TaskRequestDto requestDto) {
        Task task=taskMapper.toEntity(requestDto);

        Liste liste=listeRepository.findById(requestDto.getListeId()).orElseThrow(() -> new RuntimeException("liste not found"));
        task.setListe(liste);

        if(requestDto.getSprintId()!=null){
            Sprint sprint=sprintRepository.findById(requestDto.getSprintId()).orElseThrow(() -> new RuntimeException("sprint not found"));
            task.setSprint(sprint);
        }

        if(requestDto.getAssigneeId()!=null){
            User assignee=userRepository.findById(requestDto.getAssigneeId()).orElseThrow(() -> new RuntimeException("assignee not found"));
            task.setAssignee(assignee);
        }

        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskMapper.toDto(taskRepository.save(task));
    }

    @Override
    public TaskResponseDto getTaskById(String id) {
        Task task=taskRepository.findById(id).orElseThrow(() -> new RuntimeException("task not found"));
        return taskMapper.toDto(task);
    }

    @Override
    public List<TaskResponseDto> getAllTasks() {
        return taskRepository.findAll().stream().map(taskMapper::toDto).toList();
    }

    @Override
    public List<TaskResponseDto> getTasksByListe(String listeId) {
        return taskRepository.findByListeId(listeId).stream().map(taskMapper ::toDto).toList();
    }

    @Override
    public List<TaskResponseDto> getTasksBySprint(String sprintId) {
        return taskRepository.findBySprintId(sprintId).stream().map(taskMapper::toDto).toList();
    }

    @Override
    public List<TaskResponseDto> getTasksByAssignee(String assigneeId) {
        return taskRepository.findByAssigneeId(assigneeId).stream().map(taskMapper::toDto).toList();
    }

    @Override
    public TaskResponseDto updateTask(String id, TaskRequestDto requestDto) {

        Task task=taskRepository.findById(id).orElseThrow(() -> new RuntimeException("task not found"));

        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        task.setStatus(requestDto.getStatus());
        task.setPriority(requestDto.getPriority());
        task.setDueDate(requestDto.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());

        if(requestDto.getAssigneeId()!=null){
            User assignee=userRepository.findById(requestDto.getAssigneeId()).orElseThrow(() -> new RuntimeException("assignee not found"));
            task.setAssignee(assignee);
        }

        if(requestDto.getSprintId()!=null){
            Sprint sprint=sprintRepository.findById(requestDto.getSprintId()).orElseThrow(() -> new RuntimeException("sprint not found"));
            task.setSprint(sprint);
        }

        return taskMapper.toDto(taskRepository.save(task));
    }

    @Override
    public boolean deleteTask(String id) {
        if(id==null){
            return false;
        }
        Task task=taskRepository.findById(id).orElseThrow(() -> new RuntimeException("task not found"));
        taskRepository.delete(task);
        return true;
    }

    @Override
    public TaskResponseDto assignTask(String taskId, String assigneeId) {
        Task task=taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("task not found"));
        User assignee=userRepository.findById(assigneeId).orElseThrow(() -> new RuntimeException("user not found"));
        task.setAssignee(assignee);
        task.setUpdatedAt(LocalDateTime.now());
        return taskMapper.toDto(taskRepository.save(task));
    }

    @Override
    public TaskResponseDto moveToSprint(String taskId, String sprintId) {
        Task task=taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("task not found"));
        Sprint sprint=sprintRepository.findById(sprintId).orElseThrow(() -> new RuntimeException("sprint not found"));
        task.setSprint(sprint);
        task.setUpdatedAt(LocalDateTime.now());
        return taskMapper.toDto(taskRepository.save(task));
    }
}
