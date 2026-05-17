package com.coworking.booking.service.impl;

import com.coworking.booking.dto.WorkspaceDTO;
import com.coworking.booking.dto.WorkspaceRequest;
import com.coworking.booking.model.BookingStatus;
import com.coworking.booking.model.Workspace;
import com.coworking.booking.repository.BookingRepository;
import com.coworking.booking.repository.WorkspaceRepository;
import com.coworking.booking.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final BookingRepository bookingRepository;

    @Override
    public List<WorkspaceDTO> getAvailableWorkspaces(LocalDateTime start, LocalDateTime end) {
        List<Long> bookedIds = bookingRepository.findBookedWorkspaceIdsInPeriod(start, end, BookingStatus.CANCELLED);

        List<Workspace> available = bookedIds.isEmpty()
                ? workspaceRepository.findAll()
                : workspaceRepository.findByIdNotIn(bookedIds);

        return available.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public WorkspaceDTO saveWorkspace(WorkspaceRequest request) {
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .pricePerHour(request.getPricePerHour())
                .capacity(request.getCapacity())
                .available(request.getAvailable() == null || request.getAvailable())
                .build();

        return mapToDTO(workspaceRepository.save(workspace));
    }

    private WorkspaceDTO mapToDTO(Workspace workspace) {
        return WorkspaceDTO.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .type(workspace.getType())
                .pricePerHour(workspace.getPricePerHour())
                .capacity(workspace.getCapacity())
                .available(workspace.isAvailable())
                .description(workspace.getDescription())
                .build();
    }
}
