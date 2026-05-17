package com.coworking.invoice.kafka;

import java.time.LocalDateTime;

public record BookingCreatedEvent(
        Long bookingId,
        Long userId,
        Long workspaceId,
        Double totalAmount,
        LocalDateTime createdAt
) {
}

