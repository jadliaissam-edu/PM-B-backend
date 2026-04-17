package com.backend.backend.dao.repositories;

import com.backend.backend.dao.entities.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository  extends JpaRepository<Folder,String> {

    Optional<Folder> findByName(String s);
    List<Folder> findBySpace_Id(String spaceId);
}

