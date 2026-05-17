package com.coworking.booking.dto;

import com.coworking.booking.model.WorkspaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceDTO {
    private Long id;
    private String name;
    private WorkspaceType type;
    private String description;
    private Double pricePerHour;
    private Integer capacity;
    private boolean available;
}