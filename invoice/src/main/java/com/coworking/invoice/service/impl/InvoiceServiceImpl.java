package com.coworking.invoice.service.impl;

import com.coworking.invoice.dto.InvoiceDTO;
import com.coworking.invoice.kafka.BookingCancelledEvent;
import com.coworking.invoice.kafka.BookingCreatedEvent;
import com.coworking.invoice.model.Invoice;
import com.coworking.invoice.model.PaymentStatus;
import com.coworking.invoice.repository.InvoiceRepository;
import com.coworking.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public InvoiceDTO getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice was not found."));
        return mapToDTO(invoice);
    }

    @Override
    public InvoiceDTO getInvoiceByBookingId(Long bookingId) {
        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Invoice was not found."));
        return mapToDTO(invoice);
    }

    @Override
    public InvoiceDTO updatePaymentStatus(Long invoiceId, PaymentStatus newStatus) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice was not found."));

        invoice.setPaymentStatus(newStatus);
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return mapToDTO(updatedInvoice);
    }

    @Override
    public void createInvoiceForBooking(BookingCreatedEvent event) {
        if (event == null || event.bookingId() == null) {
            return;
        }

        boolean alreadyExists = invoiceRepository.findByBookingId(event.bookingId()).isPresent();
        if (alreadyExists) {
            return;
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .amount(event.totalAmount())
                .issuedAt(event.createdAt() != null ? event.createdAt() : LocalDateTime.now())
                .paymentStatus(PaymentStatus.UNPAID)
                .bookingId(event.bookingId())
                .build();

        invoiceRepository.save(invoice);
    }

    @Override
    public void cancelInvoiceForBooking(BookingCancelledEvent event) {
        if (event == null || event.bookingId() == null) {
            return;
        }
        invoiceRepository.findByBookingId(event.bookingId())
                .filter(invoice -> invoice.getPaymentStatus() != PaymentStatus.PAID)
                .ifPresent(invoiceRepository::delete);
    }

    private InvoiceDTO mapToDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .amount(invoice.getAmount())
                .issuedAt(invoice.getIssuedAt())
                .paymentStatus(invoice.getPaymentStatus())
                .bookingId(invoice.getBookingId())
                .build();
    }
}
