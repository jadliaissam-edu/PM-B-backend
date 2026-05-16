package com.backend.backend.dao.repositories;

import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.entities.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, String> {
    Page<Workspace> findAllByOrderByIdDesc(PageRequest of);

    Optional<Workspace> findByName(String name);

    List<Workspace> findAllByUser(User user);
}
