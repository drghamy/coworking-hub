package com.coworking.booking.service.impl;

import com.coworking.booking.dto.BookingRequest;
import com.coworking.booking.dto.BookingResponse;
import com.coworking.booking.kafka.BookingEventsPublisher;
import com.coworking.booking.model.Booking;
import com.coworking.booking.model.BookingStatus;
import com.coworking.booking.model.Workspace;
import com.coworking.booking.repository.BookingRepository;
import com.coworking.booking.repository.WorkspaceRepository;
import com.coworking.booking.service.BookingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final WorkspaceRepository workspaceRepository;
    private final BookingEventsPublisher bookingEventsPublisher;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${INVOICE_SERVICE_URL:http://localhost:8083}")
    private String invoiceServiceUrl;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Start time and end time are required.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.getStartTime().isBefore(now)) {
            throw new RuntimeException("Start time must be in the future.");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time.");
        }

        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Selected workspace was not found."));

        boolean isOverlapping = bookingRepository.existsOverlappingBooking(
                request.getWorkspaceId(), request.getStartTime(), request.getEndTime(), BookingStatus.CANCELLED);

        if (isOverlapping) {
            throw new RuntimeException("This workspace is already booked for the selected time range.");
        }

        long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        double hours = minutes / 60.0;
        double totalAmount = hours * workspace.getPricePerHour();

        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .workspace(workspace)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        bookingEventsPublisher.publishBookingCreated(savedBooking);

        return BookingResponse.builder()
                .bookingId(savedBooking.getId())
                .workspaceName(workspace.getName())
                .startTime(savedBooking.getStartTime())
                .endTime(savedBooking.getEndTime())
                .totalAmount(savedBooking.getTotalAmount())
                .status(savedBooking.getStatus())
                .invoiceNumber(null)
                .build();
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(booking -> BookingResponse.builder()
                        .bookingId(booking.getId())
                        .workspaceName(booking.getWorkspace().getName())
                        .startTime(booking.getStartTime())
                        .endTime(booking.getEndTime())
                        .totalAmount(booking.getTotalAmount())
                        .status(booking.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String authorizationHeader) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking was not found."));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled.");
        }

        if (isInvoicePaid(bookingId, authorizationHeader)) {
            throw new RuntimeException("Paid bookings cannot be cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);
        bookingEventsPublisher.publishBookingCancelled(savedBooking.getId());

        return BookingResponse.builder()
                .bookingId(savedBooking.getId())
                .workspaceName(savedBooking.getWorkspace().getName())
                .startTime(savedBooking.getStartTime())
                .endTime(savedBooking.getEndTime())
                .totalAmount(savedBooking.getTotalAmount())
                .status(savedBooking.getStatus())
                .invoiceNumber(null)
                .build();
    }

    private boolean isInvoicePaid(Long bookingId, String authorizationHeader) {
        String url = invoiceServiceUrl.replaceAll("/+$", "") + "/api/invoices/by-booking/" + bookingId;
        HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        try {
            InvoiceLookupResponse invoice = restTemplate
                    .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), InvoiceLookupResponse.class)
                    .getBody();
            return invoice != null && invoice.getPaymentStatus() == InvoicePaymentStatus.PAID;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 404 || ex.getResponseBodyAsString().contains("Invoice was not found")) {
                return false;
            }
            throw new RuntimeException("Unable to verify invoice payment status.");
        }
    }

    @Data
    private static class InvoiceLookupResponse {
        private InvoicePaymentStatus paymentStatus;
    }

    private enum InvoicePaymentStatus {
        PAID,
        UNPAID
    }
}
