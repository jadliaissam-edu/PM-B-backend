package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.Folder;
import com.backend.backend.dao.entities.Liste;
import com.backend.backend.dao.entities.Sprint;
import com.backend.backend.dao.repositories.FolderRepository;
import com.backend.backend.dao.repositories.ListeRepository;
import com.backend.backend.dao.repositories.SprintRepository;
import com.backend.backend.dto.liste.ListeRequestDto;
import com.backend.backend.dto.liste.ListeResponseDto;
import com.backend.backend.mapper.ListeMapper;
import com.backend.backend.service.serviceInterface.IListeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListeManager implements IListeService {

    private final ListeRepository listeRepository;
    private final ListeMapper listeMapper;
    private final FolderRepository folderRepository;
    private final SprintRepository sprintRepository;

    @Override
    public ListeResponseDto addListe(ListeRequestDto request) {
        Liste liste=listeMapper.toEntity(request);
        Folder folder=folderRepository.findById(request.getFolderId()).orElseThrow(()->new IllegalArgumentException("Folder not found"));
        liste.setFolder(folder);

        if(request.getSprintId()!=null){
            Sprint sprint=sprintRepository.findById(request.getSprintId()).orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
            liste.setSprint(sprint);
        }
        liste.setCreatedAt(LocalDateTime.now());
        Liste savedliste=listeRepository.save(liste);
        return listeMapper.toDto(savedliste);
    }

    @Override
    public ListeResponseDto updateListe(String id, ListeRequestDto request) {
        Liste liste=listeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Liste not found"));
        liste.setName(request.getName());
        liste.setType(request.getType());
        liste.setOrder(request.getOrder());

        if(request.getSprintId()!=null){
            Sprint sprint=sprintRepository.findById(request.getSprintId()).orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
            liste.setSprint(sprint);
        }

        if(request.getFolderId()!=null){
            Folder folder=folderRepository.findById(request.getFolderId()).orElseThrow(() -> new IllegalArgumentException("Folder not found"));
            liste.setFolder(folder);
        }
        Liste savedliste=listeRepository.save(liste);
        return listeMapper.toDto(savedliste);
    }

    @Override
    public boolean deleteListe(String id) {
        if(id==null){
            return false;
        }
        listeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Liste not found"));
        listeRepository.deleteById(id);
        return true;
    }

    @Override
    public ListeResponseDto getListeById(String id) {
        Liste liste=listeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Liste not found"));

        return listeMapper.toDto(liste);
    }

    @Override
    public Page<ListeResponseDto> getListes(Pageable pageable) {
        return listeRepository.findAll(pageable).map(listeMapper::toDto);
    }

    @Override
    public List<ListeResponseDto> getListeByFolder(String folderId) {
        Folder folder=folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        return listeRepository.findByFolder(folder).stream().map(listeMapper ::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ListeResponseDto> getListeBySprint(String sprintId) {
        Sprint sprint=sprintRepository.findById(sprintId).orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
        return listeRepository.findBySprint(sprint).stream().map(listeMapper::toDto).collect(Collectors.toList());
    }
}
