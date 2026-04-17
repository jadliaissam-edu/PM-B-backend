package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.Sprint;
import com.backend.backend.dao.repositories.ListeRepository;
import com.backend.backend.dao.repositories.SpaceRepository;
import com.backend.backend.dao.repositories.SprintRepository;
import com.backend.backend.dto.sprint.SprintRequestDto;
import com.backend.backend.dto.sprint.SprintResponseDto;
import com.backend.backend.mapper.SprintMapper;
import com.backend.backend.service.serviceInterface.ISprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SprintManager implements ISprintService {
    private final SprintRepository sprintRepository;
    private final SprintMapper sprintMapper;

    @Override
    public SprintResponseDto addSprint(SprintRequestDto sprintRequestDto) {
        Sprint sprint=sprintMapper.toEntity(sprintRequestDto);
        Sprint saved=sprintRepository.save(sprint);
        return sprintMapper.toDto(saved);
    }

    @Override
    public SprintResponseDto updateSprint(String id, SprintRequestDto sprintRequestDto) {
        Sprint sprint=sprintRepository.findById(id).orElseThrow(()->new IllegalArgumentException("Sprint not found"));
        sprint.setName(sprintRequestDto.getName());
        sprint.setGoal(sprintRequestDto.getGoal());
        sprint.setStartDate(sprintRequestDto.getStartDate());
        sprint.setEndDate(sprintRequestDto.getEndDate());
        sprint.setActive(sprintRequestDto.isActive());
        Sprint saved=sprintRepository.save(sprint);
        return sprintMapper.toDto(saved);
    }

    @Override
    public boolean deleteSprint(String id) {
        if(id==null){
            return false;
        }
        sprintRepository.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Sprint not found"));
        sprintRepository.deleteById(id);
        return true;
    }

    @Override
    public SprintResponseDto getSprintByid(String id) {
        Sprint sprint=sprintRepository.findById(id).orElseThrow(()->new IllegalArgumentException("Sprint not found"));

        return sprintMapper.toDto(sprint);
    }

    @Override
    public Page<SprintResponseDto> getSprints(Pageable pageable) {
        return sprintRepository.findAll(pageable).map(sprintMapper::toDto);
    }
}
