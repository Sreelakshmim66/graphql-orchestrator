package com.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * All DTOs used by the GraphQL resolvers and the downstream REST clients.
 * Named to match exactly what the GraphQL schema exposes.
 */
public class OrchestratorDtos {

    // ═══════════════════════════════════════════════
    // USER  (mirrors user-service REST contracts)
    // ═══════════════════════════════════════════════

    @Data public static class RegisterInput {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String mobileNumber;
    }

    @Data public static class LoginInput {
        private String email;
        private String password;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class AuthResponse {
        private boolean success;
        private String  message;
        private String  token;
        private String  userId;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class RegisterResponse {
        private boolean success;
        private String  message;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class UserProfile {
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private String mobileNumber;
    }

    // Internal REST bodies sent to user-service
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class UserLoginBody {
        private String email;
        private String password;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class UserRegisterBody {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String mobileNumber;
    }

    @Data @NoArgsConstructor
    public static class UserLoginResponse {
        private String message;
        private String token;
        private String userId;
    }

    // ═══════════════════════════════════════════════
    // TRIP  (mirrors trip-service REST contracts)
    // ═══════════════════════════════════════════════

    @Data public static class CreateTripInput {
        private String name;
        private String destination;
        private String userId;
        private String startDate;
        private String endDate;
    }

    // Internal body sent to trip-service POST /api/trips/createTrip
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class TripCreateBody {
        private String hotelName;   // mapped from CreateTripInput.name
        private String hotelId;     // mapped from CreateTripInput.destination
        private String userId;
        private String startDate;
        private String endDate;
    }

    // trip-service createTrip only returns the new trip's ID
    @Data @NoArgsConstructor
    public static class TripCreateResponse {
        private String tripId;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Trip {
        private String tripId;
        private String name;
        private String destination;
        private String userId;
        private String startDate;
        private String endDate;
        private String createdAt;
        private java.util.List<Booking> bookings;
    }

    @Data public static class SearchTripsInput {
        private String destination;
        private String startDate;
        private String endDate;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class InventoryItem {
        private String hotelId;
        private String hotelName;
        private double price;
        private String photoUrl;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class SearchTripsResponse {
        private java.util.List<InventoryItem> inventories;
    }

    // ═══════════════════════════════════════════════
    // BOOKING  (mirrors booking-service REST contracts)
    // ═══════════════════════════════════════════════

    // Internal body sent to booking-service POST /api/bookings/completeBooking
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class BookingCreateBody {
        private String tripId;
        private String userId;
        private String clientIp;
    }

    @Data public static class CreateBookingInput {
        private String tripId;
        private String userId;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Booking {
        private String bookingId;
        private String tripId;
        private String userId;
        private String status;
        private String createdAt;
    }

    // ═══════════════════════════════════════════════
    // NOTIFICATION  (mirrors notification-service REST contracts)
    // ═══════════════════════════════════════════════

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class NotificationRequestBody {
        private String itinNumber;
        private String userName;
        private String userEmail;
        private String message;
    }

    @Data @NoArgsConstructor
    public static class NotificationResponse {
        private String itinNumber;
        private String userName;
        private String userEmail;
        private String message;
        private String createdAt;
    }
}
