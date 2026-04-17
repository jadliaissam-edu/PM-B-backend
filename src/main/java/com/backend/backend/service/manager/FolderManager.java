package com.backend.backend.service.manager;


import com.backend.backend.dao.entities.Folder;
import com.backend.backend.dao.repositories.FolderRepository;
import com.backend.backend.dto.folder.FolderRequestDto;
import com.backend.backend.dto.folder.FolderResponseDto;
import com.backend.backend.mapper.FolderMapper;
import com.backend.backend.service.serviceInterface.IFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderManager implements IFolderService {

    private final FolderMapper folderMapper;
    private final FolderRepository folderRepository;


    @Override
    public FolderResponseDto addFolder(FolderRequestDto folderRequestDTO) {
        Folder folder = folderMapper.toEntity(folderRequestDTO);

        folder.setCreatedAt(LocalDateTime.now());

        folderRepository.save(folder);

        FolderResponseDto folderResponseDto = folderMapper.toResponseDto(folder);

        return folderResponseDto;

    }

    @Override
    public FolderResponseDto updateFolder(String id, FolderRequestDto folderRequestDTO) {

        Folder existingFolder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder introuvable"));

        existingFolder.setName(folderRequestDTO.getName());
        existingFolder.setHidden(folderRequestDTO.isHidden());

        Folder updatedFolder = folderRepository.save(existingFolder);

        FolderResponseDto folderResponseDto = folderMapper.toResponseDto(updatedFolder);

        return folderResponseDto;

    }

    @Override
    @Transactional
    public void deleteFolder(String folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder introuvable"));
        folderRepository.delete(folder);
    }

    @Override
    public FolderResponseDto getFolderById(String id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder introuvable"));

        FolderResponseDto folderResponseDto = folderMapper.toResponseDto(folder);

        return folderResponseDto;
    }

    @Override
    public List<FolderResponseDto> getAllFolder() {
        return folderRepository.findAll().stream()
                .map(folderMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FolderResponseDto> getFoldersBySpaceId(String spaceId) {
        return folderRepository.findBySpace_Id(spaceId).stream()
                .map(folderMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
