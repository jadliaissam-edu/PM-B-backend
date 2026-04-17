package com.backend.backend.service.serviceInterface;

import com.backend.backend.dto.space.SpaceRequestDto;
import com.backend.backend.dto.space.SpaceResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface ISpaceService {
    SpaceResponseDto addSpace(SpaceRequestDto spaceDto);
    SpaceResponseDto updateSpace(String id,SpaceRequestDto spaceDto);
    boolean deleteSpace(String id);

    SpaceResponseDto getSpaceByid(String spaceId);
    Page<SpaceResponseDto> getSpaces(Pageable pageable);
    List<SpaceResponseDto> getSpaceByWorkspace(String workspaceid);

}
