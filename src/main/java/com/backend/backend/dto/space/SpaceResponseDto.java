package com.backend.backend.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceResponseDto {
    private String id;
    private String spaceName;
    private String description;
    private String color;
    private boolean isPrivate;

    private String workspaceid;
    private String workspaceName;


    //private List<FolderDto> folders;

}