package com.backend.backend.service.serviceInterface;

import com.backend.backend.dto.folder.FolderRequestDto;
import com.backend.backend.dto.folder.FolderResponseDto;


import java.util.List;

public interface IFolderService {
    
    public FolderResponseDto addFolder(FolderRequestDto folderRequestDTO);
    public FolderResponseDto updateFolder(String id, FolderRequestDto folderRequestDTO);
    public void deleteFolder(String folderId);

    public FolderResponseDto getFolderById(String id);
    public List<FolderResponseDto> getAllFolder();
    public List<FolderResponseDto> getFoldersBySpaceId(String spaceId);
}
