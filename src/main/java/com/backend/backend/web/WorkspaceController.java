package com.backend.backend.web;

import com.backend.backend.dto.workspace.WorkspaceDto;
import com.backend.backend.dto.workspace.WorkspaceRequestDto;
import com.backend.backend.dto.workspace.WorkspaceResponseDto;
import com.backend.backend.service.serviceInterface.IWorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    @Autowired
    private IWorkspaceService workspaceService;

//    @GetMapping
//    public List<WorkspaceResponseDto> getWorkspaces() {
//        return workspaceService.getAllWorkspace();
//    }

    @PostMapping
    public WorkspaceResponseDto createNewCourse(@RequestBody WorkspaceRequestDto body) {
        WorkspaceResponseDto responseDTO = workspaceService.addWorkspace(body);
        return responseDTO;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkspace(@PathVariable String id){
        workspaceService.deleteWorkspace(id);
    }

    @GetMapping("/{id}")
    public WorkspaceResponseDto getWorkspaceById(@PathVariable String id) {
        return workspaceService.getWorkspaceById(id);
    }

    @PutMapping("/{id}")
    public WorkspaceResponseDto updateWorkspace(
            @PathVariable String id,
            @RequestBody WorkspaceRequestDto body) {
        return workspaceService.updateWorkspace(id, body);
    }

    @GetMapping
    public List<WorkspaceResponseDto> getWorkspaceByCurrentUser(){
            List<WorkspaceResponseDto> workspacesResponseDto = workspaceService.getWorkspaceByCurrentUser();

            return workspacesResponseDto;
    }




}