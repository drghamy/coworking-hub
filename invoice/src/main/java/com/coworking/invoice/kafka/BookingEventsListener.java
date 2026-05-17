package com.coworking.invoice.kafka;

import com.coworking.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class BookingEventsListener {

    private final InvoiceService invoiceService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.BOOKING_CREATED)
    public void onBookingCreated(String payload) throws Exception {
        BookingCreatedEvent event = objectMapper.readValue(payload, BookingCreatedEvent.class);
        invoiceService.createInvoiceForBooking(event);
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_CANCELLED)
    public void onBookingCancelled(String payload) throws Exception {
        BookingCancelledEvent event = objectMapper.readValue(payload, BookingCancelledEvent.class);
        invoiceService.cancelInvoiceForBooking(event);
    }

    @DltHandler
    public void dlt(String payload) {
        // Leave empty; just prevents container from crashing if DLT is configured later.
    }
}
