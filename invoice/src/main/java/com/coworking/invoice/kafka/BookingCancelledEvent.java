package com.coworking.invoice.kafka;

import java.time.LocalDateTime;

public record BookingCancelledEvent(
        Long bookingId,
        LocalDateTime cancelledAt
) {
}

