package com.coworking.booking.controller;

import com.coworking.booking.dto.WorkspaceDTO;
import com.coworking.booking.dto.WorkspaceRequest;
import com.coworking.booking.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/available")
    public ResponseEntity<List<WorkspaceDTO>> getAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(workspaceService.getAvailableWorkspaces(start, end));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @PostMapping("/add")
    public ResponseEntity<WorkspaceDTO> addWorkspace(@Valid @RequestBody WorkspaceRequest request) {
        return ResponseEntity.ok(workspaceService.saveWorkspace(request));
    }
}
