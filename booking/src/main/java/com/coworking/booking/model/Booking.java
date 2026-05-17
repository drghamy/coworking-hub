package com.coworking.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required.")
    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "workspace_id", nullable = false)
    @NotNull(message = "Workspace is required.")
    private Workspace workspace;

    @NotNull(message = "Start time is required.")
    @FutureOrPresent(message = "Start time cannot be in the past.")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required.")
    @Future(message = "End time must be in the future.")
    @Column(nullable = false)
    private LocalDateTime endTime;

    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
}
