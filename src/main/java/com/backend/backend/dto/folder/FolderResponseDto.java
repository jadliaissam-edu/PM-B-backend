package com.backend.backend.dto.folder;

import com.backend.backend.dao.entities.Liste;
import com.backend.backend.dto.liste.ListeResponseDto;
import lombok.Data;

import java.util.List;

@Data
public class FolderResponseDto {

    private String id;
    private String spaceId;
    private String name;
    private boolean isHidden;
    private List<ListeResponseDto> listes;
}
