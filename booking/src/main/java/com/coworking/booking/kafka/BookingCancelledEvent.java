package com.coworking.booking.kafka;

import java.time.LocalDateTime;

public record BookingCancelledEvent(
        Long bookingId,
        LocalDateTime cancelledAt
) {
}

