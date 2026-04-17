package com.backend.backend.web;

import com.backend.backend.dto.folder.FolderRequestDto;
import com.backend.backend.dto.folder.FolderResponseDto;
import com.backend.backend.service.serviceInterface.IFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final IFolderService folderService;

    @GetMapping("/{id}")
    public FolderResponseDto getFolderById(@PathVariable String id){
        FolderResponseDto folderResponseDto = folderService.getFolderById(id);

        return  folderResponseDto;
    }

    @GetMapping
    public List<FolderResponseDto> getAllFolder(){
        return folderService.getAllFolder();
    }

    @GetMapping("/space/{spaceId}")
    public List<FolderResponseDto> getFoldersBySpaceId(@PathVariable String spaceId) {
        return folderService.getFoldersBySpaceId(spaceId);
    }

    @PutMapping("/{id}")
    public FolderResponseDto updateFolder(
            @PathVariable String id,
            @RequestBody FolderRequestDto body){

        FolderResponseDto folderResponseDto = folderService.updateFolder(id,body);

        return folderResponseDto;
    }

    @PostMapping
    public FolderResponseDto addFolder(@RequestBody FolderRequestDto body){
        return folderService.addFolder(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkspace(@PathVariable String id){ folderService.deleteFolder(id);}



}
