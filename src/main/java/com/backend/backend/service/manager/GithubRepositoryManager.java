package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.GithubRepository;
import com.backend.backend.dao.repositories.GithubRepositoryRepository;
import com.backend.backend.dto.githubrepository.GithubRepositoryRequestDto;
import com.backend.backend.dto.githubrepository.GithubRepositoryResponseDto;
import com.backend.backend.service.serviceInterface.IGithubRepositoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GithubRepositoryManager implements IGithubRepositoryService {

    private final GithubRepositoryRepository repoRepository;

    // ─── Upsert ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public GithubRepositoryResponseDto upsertRepository(GithubRepositoryRequestDto request) {
        Optional<GithubRepository> existing = repoRepository.findByUserIdAndRepoOwnerAndRepoName(
            request.getUserId(), request.getRepoOwner(), request.getRepoName()
        );

        GithubRepository entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setBranch(request.getBranch());
            entity.setPrivate(request.isPrivate());
            // Mise à jour du token uniquement si un nouveau est fourni
            if (request.getGithubTokenEncrypted() != null) {
                entity.setGithubTokenEncrypted(request.getGithubTokenEncrypted());
            }
            entity.setUpdatedAt(LocalDateTime.now());
        } else {
            entity = new GithubRepository();
            entity.setUserId(request.getUserId());
            entity.setRepoOwner(request.getRepoOwner());
            entity.setRepoName(request.getRepoName());
            entity.setBranch(request.getBranch());
            entity.setPrivate(request.isPrivate());
            entity.setGithubTokenEncrypted(request.getGithubTokenEncrypted());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
        }

        return toDto(repoRepository.save(entity));
    }

    // ─── Lecture ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<GithubRepositoryResponseDto> getRepositoriesByUser(String userId) {
        return repoRepository.findAllByUserId(userId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GithubRepositoryResponseDto> getRepository(
        String userId, String repoOwner, String repoName
    ) {
        return repoRepository
            .findByUserIdAndRepoOwnerAndRepoName(userId, repoOwner, repoName)
            .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getEncryptedToken(
        String userId, String repoOwner, String repoName
    ) {
        return repoRepository
            .findByUserIdAndRepoOwnerAndRepoName(userId, repoOwner, repoName)
            .map(GithubRepository::getGithubTokenEncrypted);
    }

    // ─── Suppression ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteRepository(String userId, String repoOwner, String repoName) {
        if (!repoRepository.existsByUserIdAndRepoOwnerAndRepoName(userId, repoOwner, repoName)) {
            throw new EntityNotFoundException(
                String.format("Dépôt '%s/%s' introuvable pour l'utilisateur '%s'.", repoOwner, repoName, userId)
            );
        }
        repoRepository.deleteByUserIdAndRepoOwnerAndRepoName(userId, repoOwner, repoName);
    }

    // ─── Mapper interne ─────────────────────────────────────────────────────

    private GithubRepositoryResponseDto toDto(GithubRepository entity) {
        return new GithubRepositoryResponseDto(
            entity.getId(),
            entity.getUserId(),
            entity.getRepoOwner(),
            entity.getRepoName(),
            entity.getBranch(),
            entity.isPrivate(),
            entity.getGithubTokenEncrypted() != null,  // tokenStored — jamais le token brut
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
