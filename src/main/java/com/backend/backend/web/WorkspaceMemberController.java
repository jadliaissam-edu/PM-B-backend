package com.backend.backend.web;


import com.backend.backend.dto.workspaceMember.InviteMemberRequestDto;
import com.backend.backend.dto.workspaceMember.RoleRequest;
import com.backend.backend.dto.workspaceMember.WorkspaceMemberRequestDto;
import com.backend.backend.dto.workspaceMember.WorkspaceMemberResponseDto;
import com.backend.backend.service.serviceInterface.IWorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaceMembers")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final IWorkspaceMemberService workspaceMemberService;

    @GetMapping("/{id}")
    public WorkspaceMemberResponseDto getWorkspaceMemberById(@PathVariable String id){
        return workspaceMemberService.getWorkspaceMemberById(id);
    }

    @GetMapping("/User/{id}")
    public List<WorkspaceMemberResponseDto> getWorkspaceMemberByUserId(@PathVariable String id){

        List<WorkspaceMemberResponseDto> workspaceMemberResponseDto =  workspaceMemberService.getWorkspaceByUserId(id);
        return workspaceMemberResponseDto;
    }


    @GetMapping("/workspace/{id}")
    public List<WorkspaceMemberResponseDto> getWorkspaceMemberByWorkspaceId(@PathVariable String id){
        List<WorkspaceMemberResponseDto> workspaceMemberResponseDtos = workspaceMemberService.getWorkspaceMembersByWorkspacId(id);
        return workspaceMemberResponseDtos;
    }

    @PostMapping
    public WorkspaceMemberResponseDto addWorkspaceMember(@RequestBody WorkspaceMemberRequestDto workspaceMemberRequestDto){

        WorkspaceMemberResponseDto workspaceMemberResponseDto  = workspaceMemberService.addWorkspaceMember(workspaceMemberRequestDto);
        return workspaceMemberResponseDto;
    }

    @PostMapping("/invite")
    public WorkspaceMemberResponseDto inviteMemberByEmail(@RequestBody InviteMemberRequestDto inviteMemberRequestDto){
        return workspaceMemberService.inviteByEmail(inviteMemberRequestDto);
    }

    @PutMapping("/{id}")
    public WorkspaceMemberResponseDto updateWorkspaceMemberRole(@PathVariable String id,@RequestBody RoleRequest roleRequest) {
        WorkspaceMemberResponseDto workspaceMemberResponseDto = workspaceMemberService.updateWorkspaceMemberRole(id,roleRequest);
        return workspaceMemberResponseDto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkspaceMember(@PathVariable String id){
        workspaceMemberService.deleteWorkspaceMember(id);
    }


}
