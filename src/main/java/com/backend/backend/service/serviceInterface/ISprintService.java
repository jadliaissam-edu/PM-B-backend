package com.backend.backend.service.serviceInterface;

import com.backend.backend.dto.space.SpaceResponseDto;
import com.backend.backend.dto.sprint.SprintRequestDto;
import com.backend.backend.dto.sprint.SprintResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISprintService {
    SprintResponseDto addSprint(SprintRequestDto  sprintRequestDto);
    SprintResponseDto updateSprint(String id,SprintRequestDto  sprintRequestDto);
    boolean deleteSprint(String id);
    SprintResponseDto getSprintByid(String id);
    Page<SprintResponseDto> getSprints(Pageable pageable);
}
