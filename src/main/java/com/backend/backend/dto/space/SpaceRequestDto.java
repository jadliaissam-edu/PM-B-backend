package com.backend.backend.dto.space;

import lombok.Data;

import java.util.List;

@Data

public class SpaceRequestDto {
    private String name;
    private String description;
    private String color;
    private boolean isPrivate;

    private String workspaceId;


}
