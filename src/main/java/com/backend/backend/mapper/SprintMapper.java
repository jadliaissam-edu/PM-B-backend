package com.backend.backend.mapper;

import com.backend.backend.dao.entities.Sprint;
import com.backend.backend.dto.sprint.SprintRequestDto;
import com.backend.backend.dto.sprint.SprintResponseDto;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.Collections;
import java.util.stream.Collectors;
@Component
public class SprintMapper {

    @Autowired
    private ModelMapper modelMapper;

    private final ListeMapper listeMapper;

    public SprintMapper(ListeMapper listeMapper) {
        this.listeMapper = listeMapper;
    }

    public SprintResponseDto toDto(Sprint sprint) {
        if (sprint == null) return null;

        SprintResponseDto dto = modelMapper.map(sprint, SprintResponseDto.class);

        if (sprint.getListes() != null) {
            dto.setListes(
                    sprint.getListes()
                            .stream()
                            .map(liste -> listeMapper.toDto(liste))
                            .collect(Collectors.toList())
            );
        } else {
            dto.setListes(Collections.emptyList());
        }

        return dto;
    }

    public Sprint toEntity(SprintRequestDto request) {
        if (request == null) return null;

        Sprint sprint = new Sprint();
        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setActive(request.isActive());
        return sprint;
    }
}