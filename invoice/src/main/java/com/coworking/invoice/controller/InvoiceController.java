package com.coworking.invoice.controller;

import com.coworking.invoice.dto.InvoiceDTO;
import com.coworking.invoice.model.PaymentStatus;
import com.coworking.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/all")
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/by-booking/{bookingId}")
    public ResponseEntity<InvoiceDTO> getByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(invoiceService.getInvoiceByBookingId(bookingId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<InvoiceDTO> updateStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(invoiceService.updatePaymentStatus(id, status));
    }
}
