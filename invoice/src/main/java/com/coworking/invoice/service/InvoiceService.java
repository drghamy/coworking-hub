package com.coworking.invoice.service;

import com.coworking.invoice.dto.InvoiceDTO;
import com.coworking.invoice.kafka.BookingCancelledEvent;
import com.coworking.invoice.kafka.BookingCreatedEvent;
import com.coworking.invoice.model.PaymentStatus;

import java.util.List;

public interface InvoiceService {
    List<InvoiceDTO> getAllInvoices();

    InvoiceDTO getInvoiceByNumber(String invoiceNumber);

    InvoiceDTO getInvoiceByBookingId(Long bookingId);

    InvoiceDTO updatePaymentStatus(Long invoiceId, PaymentStatus newStatus);

    void createInvoiceForBooking(BookingCreatedEvent event);

    void cancelInvoiceForBooking(BookingCancelledEvent event);
}
