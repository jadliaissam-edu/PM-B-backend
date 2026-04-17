package com.backend.backend.mapper;

import com.backend.backend.dao.entities.Folder;
import com.backend.backend.dao.entities.Liste;
import com.backend.backend.dao.repositories.FolderRepository;
import com.backend.backend.dto.liste.ListeRequestDto;
import com.backend.backend.dto.liste.ListeResponseDto;
import com.backend.backend.dto.space.SpaceResponseDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ListeMapper {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FolderRepository folderRepository;

    public ListeResponseDto toDto(Liste liste) {
        if (liste == null) return null;
        ListeResponseDto dto = modelMapper.map(liste, ListeResponseDto.class);
        dto.setFolderId(liste.getFolder().getId());
        dto.setFolderName(liste.getFolder().getName());
        if (liste.getSprint() != null) {
            dto.setSprintId(liste.getSprint().getId());
            dto.setSprintName(liste.getSprint().getName());
        }
        return dto;
    }
    public Liste toEntity(ListeRequestDto requestDto) {
        if (requestDto == null) return null;

        Liste liste = new Liste();
        liste.setName(requestDto.getName());
        liste.setType(requestDto.getType());
        liste.setOrder(requestDto.getOrder());

        Folder folder = folderRepository.findById(requestDto.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        liste.setFolder(folder);
        liste.setSprint(null);
        return liste;
    }

}
