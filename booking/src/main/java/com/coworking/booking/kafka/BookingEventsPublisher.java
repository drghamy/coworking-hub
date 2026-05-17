package com.coworking.booking.kafka;

import com.coworking.booking.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BookingEventsPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBookingCreated(Booking booking) {
        if (booking == null || booking.getId() == null) {
            return;
        }

        BookingCreatedEvent event = new BookingCreatedEvent(
                booking.getId(),
                booking.getUserId(),
                booking.getWorkspace() != null ? booking.getWorkspace().getId() : null,
                booking.getTotalAmount(),
                LocalDateTime.now()
        );

        kafkaTemplate.send(KafkaTopics.BOOKING_CREATED, booking.getId().toString(), event);
    }

    public void publishBookingCancelled(Long bookingId) {
        if (bookingId == null) {
            return;
        }

        BookingCancelledEvent event = new BookingCancelledEvent(bookingId, LocalDateTime.now());
        kafkaTemplate.send(KafkaTopics.BOOKING_CANCELLED, bookingId.toString(), event);
    }
}

