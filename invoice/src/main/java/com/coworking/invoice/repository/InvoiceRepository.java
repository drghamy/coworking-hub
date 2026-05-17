package com.coworking.invoice.repository;

import com.coworking.invoice.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByBookingId(Long bookingId);

    @Transactional
    @Modifying
    @Query("delete from Invoice invoice where invoice.bookingId = :bookingId")
    int deleteByBookingId(@Param("bookingId") Long bookingId);
}
