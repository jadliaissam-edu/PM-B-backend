package com.backend.backend.dto.sprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SprintRequestDto {
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String goal;
    @JsonProperty("isActive")
    private boolean isActive;


}
