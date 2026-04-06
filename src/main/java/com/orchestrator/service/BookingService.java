package com.orchestrator.service;

import com.orchestrator.dto.OrchestratorDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final String BOOKING_SERVICE = "BOOKING-SERVICE"; // Eureka name

    private final ServiceDiscoveryClient discovery;
    private final UserService userService;
    private final NotificationService notificationService;

    public Booking createBooking(CreateBookingInput input, String jwtToken) {
        Booking booking;
        try {
            BookingCreateBody body = new BookingCreateBody(input.getTripId(), input.getUserId(), null);
            booking = discovery.postWithToken(
                    BOOKING_SERVICE, "/api/bookings/completeBooking", body, jwtToken, Booking.class);
        } catch (RuntimeException e) {
            log.error("createBooking error: {}", e.getMessage());
            throw new RuntimeException("Failed to create booking: " + e.getMessage());
        }

        // Fire notification after successful booking
        try {
            UserProfile user = userService.getProfile(input.getUserId(), jwtToken);
            String userName  = user != null ? user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "") : input.getUserId();
            String userEmail = user != null ? user.getEmail() : null;
            String message   = "Your booking " + booking.getBookingId() + " for trip " + booking.getTripId() + " has been confirmed.";
            notificationService.sendNotification(booking.getBookingId(), userName.trim(), userEmail, message);
        } catch (RuntimeException e) {
            log.warn("Notification failed for bookingId={}: {}", booking.getBookingId(), e.getMessage());
        }

        return booking;
    }

    public Booking getBookingById(String bookingId, String jwtToken) {
        try {
            return discovery.getWithToken(BOOKING_SERVICE,
                    "/api/bookings/" + bookingId, jwtToken, Booking.class);
        } catch (RuntimeException e) {
            log.warn("getBookingById error bookingId={}: {}", bookingId, e.getMessage());
            return null;
        }
    }

    public List<Booking> getBookingsByTrip(String tripId, String jwtToken) {
        try {
            return discovery.clientFor(BOOKING_SERVICE)
                    .get()
                    .uri("/api/bookings/trip/" + tripId)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Booking>>() {})
                    .block();
        } catch (RuntimeException e) {
            log.warn("getBookingsByTrip error tripId={}: {}", tripId, e.getMessage());
            return List.of();
        }
    }

    public List<Booking> getBookingsByUser(String userId, String jwtToken) {
        try {
            return discovery.clientFor(BOOKING_SERVICE)
                    .get()
                    .uri("/api/bookings/user/" + userId)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Booking>>() {})
                    .block();
        } catch (RuntimeException e) {
            log.warn("getBookingsByUser error userId={}: {}", userId, e.getMessage());
            return List.of();
        }
    }
}
