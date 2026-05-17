package com.coworking.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "workspaces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Workspace name is required.")
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Workspace type is required.")
    @Column(nullable = false)
    private WorkspaceType type; 

    private String description;

    @NotNull(message = "Price per hour is required.")
    @Min(value = 0, message = "Price cannot be negative.")
    @Column(nullable = false)
    private Double pricePerHour;

    @NotNull(message = "Capacity is required.")
    @Min(value = 1, message = "Workspace must fit at least one person.")
    @Column(nullable = false)
    private Integer capacity;

    @Builder.Default
    private boolean available = true;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
}
