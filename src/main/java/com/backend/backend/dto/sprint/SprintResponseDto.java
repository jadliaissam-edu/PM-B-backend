package com.backend.backend.dto.sprint;

import com.backend.backend.dto.liste.ListeResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SprintResponseDto {
    private  String id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String goal;
    private boolean isActive;

    private List<ListeResponseDto> listes;


}
