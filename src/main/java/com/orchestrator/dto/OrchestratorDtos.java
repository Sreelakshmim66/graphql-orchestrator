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

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Trip {
        private String id;
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

    @Data public static class CreateBookingInput {
        private String tripId;
        private String userId;
        private String type;   // HOTEL | FLIGHT | ACTIVITY
        private String details;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Booking {
        private String id;
        private String tripId;
        private String userId;
        private String type;
        private String details;
        private String status;
        private String createdAt;
    }
}
