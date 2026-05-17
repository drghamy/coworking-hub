package com.coworking.booking.dto;

import com.coworking.booking.model.WorkspaceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkspaceRequest {
    @NotBlank(message = "Workspace name is required.")
    private String name;

    @NotNull(message = "Workspace type is required.")
    private WorkspaceType type;

    private String description;

    @NotNull(message = "Price per hour is required.")
    @DecimalMin(value = "0.0", message = "Price cannot be negative.")
    private Double pricePerHour;

    @NotNull(message = "Capacity is required.")
    @Min(value = 1, message = "Workspace must fit at least one person.")
    private Integer capacity;

    private Boolean available;
}
