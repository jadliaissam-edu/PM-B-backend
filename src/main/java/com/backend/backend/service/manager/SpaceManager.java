package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.Space;
import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.entities.Workspace;
import com.backend.backend.dao.repositories.SpaceRepository;
import com.backend.backend.dao.repositories.UserRepository;
import com.backend.backend.dao.repositories.WorkspaceRepository;
import com.backend.backend.dto.space.SpaceRequestDto;
import com.backend.backend.dto.space.SpaceResponseDto;
import com.backend.backend.mapper.SpaceMapper;
import com.backend.backend.service.serviceInterface.ISpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceManager implements ISpaceService {

    private final SpaceRepository spaceRepository;
    private final SpaceMapper spaceMapper;
    private final WorkspaceRepository workspaceRepository;

    @Override
    public SpaceResponseDto addSpace(SpaceRequestDto spaceDto) {

        if (spaceDto.getWorkspaceId() == null) {
            throw new IllegalArgumentException("workspaceId is required");
        }

        Space space = spaceMapper.toEntity(spaceDto);

        Workspace workspace=workspaceRepository.findById(spaceDto.getWorkspaceId()).orElseThrow(()->new IllegalArgumentException("Workspace not found"));
        space.setWorkspace(workspace);


        Space savedSpace = spaceRepository.save(space);
        return spaceMapper.toDto(savedSpace);

    }

    @Override
    public SpaceResponseDto updateSpace(String id,SpaceRequestDto spaceDto) {
        Space space=spaceRepository.findById(id).orElseThrow(()->new IllegalArgumentException("Space not found"));
        space.setName(spaceDto.getName());
        space.setColor(spaceDto.getColor());
        space.setPrivate(spaceDto.isPrivate());
        if(spaceDto.getWorkspaceId()!=null){
            Workspace workspace=workspaceRepository.findById(spaceDto.getWorkspaceId()).orElseThrow(()->new IllegalArgumentException("Workspace not found"));
            space.setWorkspace(workspace);
        }
        Space savedSpace = spaceRepository.save(space);
        return spaceMapper.toDto(savedSpace);
    }

    @Override
    @Transactional
    public boolean deleteSpace(String id) {
        if(id==null){
            return false;
        }
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));
        spaceRepository.delete(space);
        return true;
    }

    @Override
    public SpaceResponseDto getSpaceByid(String spaceId) {
        Space space=spaceRepository.findById(spaceId).orElseThrow(()->new IllegalArgumentException("Space not found"));

        return spaceMapper.toDto(space);
    }

    @Override
    public Page<SpaceResponseDto> getSpaces(Pageable pageable) {
        return spaceRepository.findAll(pageable)
                .map(spaceMapper::toDto);
    }

    @Override
    public List<SpaceResponseDto> getSpaceByWorkspace(String workspaceid) {
        Workspace workspace=workspaceRepository.findById(workspaceid).orElseThrow(()->new IllegalArgumentException("Workspace not found"));
        return spaceRepository.findByWorkspace(workspace).stream().map(spaceMapper::toDto).collect(Collectors.toList());
    }
}
