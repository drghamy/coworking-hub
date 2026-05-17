package com.coworking.booking.service;

import com.coworking.booking.dto.WorkspaceDTO;
import com.coworking.booking.dto.WorkspaceRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkspaceService {
    List<WorkspaceDTO> getAvailableWorkspaces(LocalDateTime start, LocalDateTime end);

    WorkspaceDTO saveWorkspace(WorkspaceRequest request);
}
