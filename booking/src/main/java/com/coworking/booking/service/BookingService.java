package com.coworking.booking.service;

import com.coworking.booking.dto.BookingRequest;
import com.coworking.booking.dto.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);

    List<BookingResponse> getAllBookings();

    BookingResponse cancelBooking(Long bookingId, String authorizationHeader);
}
