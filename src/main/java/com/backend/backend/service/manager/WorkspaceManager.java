package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.entities.Workspace;
import com.backend.backend.dao.repositories.UserRepository;
import com.backend.backend.dao.repositories.WorkspaceRepository;
import com.backend.backend.dto.space.SpaceResponseDto;
import com.backend.backend.dto.workspace.WorkspaceDto;
import com.backend.backend.dto.workspace.WorkspaceRequestDto;
import com.backend.backend.dto.workspace.WorkspaceResponseDto;
import com.backend.backend.dto.workspaceMember.WorkspaceMemberRequestDto;
import com.backend.backend.mapper.WorkspaceMapper;
import com.backend.backend.service.serviceInterface.IAuthService;
import com.backend.backend.service.serviceInterface.IWorkspaceService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceManager implements IWorkspaceService{


    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMapper workspaceMapper;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final IAuthService authService;

    @Override
    public List<WorkspaceResponseDto> getWorkspaceByCurrentUser() {
        User user = authService.getCurrentUser();

        List<Workspace> workspaces = workspaceRepository.findAllByUser(user);

        return workspaces.stream()
                .map(workspaceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public WorkspaceResponseDto addWorkspace(WorkspaceRequestDto workspaceRequestDTO) {
        Workspace workspaceEntity = workspaceMapper.requesttoEntity(workspaceRequestDTO);
        User user = authService.getCurrentUser();
        System.out.println(user);
        workspaceEntity.setUser(user);

        workspaceEntity.setCreatedAt(LocalDateTime.now());

        workspaceRepository.save(workspaceEntity);
        WorkspaceResponseDto workspaceResponseDto = workspaceMapper.toResponseDto(workspaceEntity);
        return workspaceResponseDto;
    }

    @Override
    public WorkspaceResponseDto updateWorkspace(String id, WorkspaceRequestDto workspaceRequestDTO) {

        Workspace existingWorkspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace introuvable"));

        existingWorkspace.setName(workspaceRequestDTO.getName());
        existingWorkspace.setSlug(workspaceRequestDTO.getSlug());
        Workspace updatedWorkspace = workspaceRepository.save(existingWorkspace);
        WorkspaceResponseDto workspaceResponseDTO = workspaceMapper.toResponseDto(updatedWorkspace);

        return workspaceResponseDTO;
    }

    @Override
    @Transactional
    public void deleteWorkspace(String workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace introuvable"));
        workspaceRepository.delete(workspace);
    }

    @Override
    public WorkspaceResponseDto getWorkspaceById(String id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace introuvable"));

        return workspaceMapper.toResponseDto(workspace);
    }

    @Override
    public List<WorkspaceResponseDto> getAllWorkspace() {
        return workspaceRepository.findAll().stream()
                .map(workspaceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

//    @Override
//    public Page<WorkspaceDto> getAllWorkspaceSummaries(int page, int size) {
//        Page<Workspace> workspaces = workspaceRepository.findAllByOrderByIdDesc(PageRequest.of(page, size));
//        return workspaces.map(w -> {
//            WorkspaceDto dto = modelMapper.map(w, WorkspaceDto.class);
//
//            if (w.getUser() != null) {
//                dto.setOwnerName(w.getUser().getName());
//            }
//
//            if (w.getWorkspaceMembers() != null) {
//                dto.setWorkspaceMembers(w.getWorkspaceMembers().stream()
//                        .map(m -> {
//                            WorkspaceMemberRequestDto wmDto = new WorkspaceMemberRequestDto();
//                            wmDto.setRole(m.getRole());
//                            wmDto.setJoinedAt(m.getJoinedAt());
//                            return wmDto;
//                        }).toList());
//            }
//
//            // 5. Mapping des Spaces
//            if (w.getSpaces() != null) {
//                dto.setSpaces(w.getSpaces().stream()
//                        .map(s -> {
//                            SpaceResponseDto sDto = new SpaceResponseDto();
//                            sDto.setId(s.getId());
//                            sDto.setSpaceName(s.getName());
//                            sDto.setColor(s.getColor());
//                            sDto.setPrivate(s.isPrivate());
//                            return sDto;
//                        }).toList());
//            }
//
//            return dto;
//        });

//    }
}
