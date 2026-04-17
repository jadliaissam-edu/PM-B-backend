package com.backend.backend.web;

import com.backend.backend.dto.sprint.SprintRequestDto;
import com.backend.backend.dto.sprint.SprintResponseDto;
import com.backend.backend.service.serviceInterface.ISprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
public class SprintController {
    private final ISprintService sprintService;

    @PostMapping
    public ResponseEntity<SprintResponseDto> addSprint(@RequestBody SprintRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sprintService.addSprint(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SprintResponseDto> updateSprint(
            @PathVariable String id,
            @RequestBody SprintRequestDto request) {
        return ResponseEntity.ok(sprintService.updateSprint(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable String id) {
        boolean deleted = sprintService.deleteSprint(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SprintResponseDto> getSprintById(@PathVariable String id) {
        return ResponseEntity.ok(sprintService.getSprintByid(id));
    }

    @GetMapping
    public ResponseEntity<Page<SprintResponseDto>> getSprints(Pageable pageable) {
        return ResponseEntity.ok(sprintService.getSprints(pageable));
    }
}
