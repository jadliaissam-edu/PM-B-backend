package com.backend.backend.dto.folder;

import lombok.Data;

@Data
public class FolderRequestDto {
    private String name;
    private String description;
    private boolean isHidden;
    private String spaceId;
}

