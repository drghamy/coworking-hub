package com.coworking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.coworking.booking.model.BookingStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long bookingId;
    private String workspaceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalAmount;
    private BookingStatus status;
    private String invoiceNumber;
}
