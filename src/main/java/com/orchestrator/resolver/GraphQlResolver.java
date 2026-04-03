package com.orchestrator.resolver;

import com.orchestrator.dto.OrchestratorDtos.*;
import com.orchestrator.service.*;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Single GraphQL controller handling all queries and mutations.
 *
 * Public operations  : register, login          (no JWT required)
 * Protected operations: everything else          (JWT required in Authorization header)
 *
 * Token is extracted from the GraphQL context (populated by JwtGraphQlInterceptor).
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class GraphQlResolver {

    private final UserService    userService;
    private final TripService    tripService;
    private final BookingService bookingService;

    // ════════════════════════════════════════════
    //  AUTH MUTATIONS (public — no JWT needed)
    // ════════════════════════════════════════════

    @MutationMapping
    public RegisterResponse register(@Argument RegisterInput input) {
        log.debug("GraphQL register → email={}", input.getEmail());
        return userService.register(input);
    }

    @MutationMapping
    public AuthResponse login(@Argument LoginInput input) {
        log.debug("GraphQL login → email={}", input.getEmail());
        return userService.login(input);
    }

    // ════════════════════════════════════════════
    //  USER QUERIES (protected)
    // ════════════════════════════════════════════

    @QueryMapping
    public UserProfile me(@Argument String userId, DataFetchingEnvironment env) {
        String token = extractToken(env);
        return userService.getProfile(userId, token);
    }

    // ════════════════════════════════════════════
    //  TRIP QUERIES + MUTATIONS (protected)
    // ════════════════════════════════════════════

    @QueryMapping
    public Trip trip(@Argument String tripId, DataFetchingEnvironment env) {
        String token = extractToken(env);
        return tripService.getTripById(tripId, token);
    }

    @QueryMapping
    public List<Trip> myTrips(@Argument String userId, DataFetchingEnvironment env) {
        String token = extractToken(env);
        return tripService.getTripsByUser(userId, token);
    }

    @MutationMapping
    public Trip createTrip(@Argument CreateTripInput input, DataFetchingEnvironment env) {
        String token = extractToken(env);
        return tripService.createTrip(input, token);
    }

    // ════════════════════════════════════════════
    //  BOOKING QUERIES + MUTATIONS (protected)
    // ════════════════════════════════════════════

    @QueryMapping
    public Booking booking(@Argument String bookingId, DataFetchingEnvironment env) {
        String token = extractToken(env);
        return bookingService.getBookingById(bookingId, token);
    }

    @QueryMapping
    public List<Booking> bookingsByTrip(@Argument String tripId, DataFetchingEnvironment env) {
        String token = extractToken(env);
        return bookingService.getBookingsByTrip(tripId, token);
    }

    @QueryMapping
    public List<Booking> bookingsByUser(@Argument String userId, DataFetchingEnvironment env) {
        String token = extractToken(env);
        return bookingService.getBookingsByUser(userId, token);
    }

    @MutationMapping
    public Booking createBooking(@Argument CreateBookingInput input, DataFetchingEnvironment env) {
        String token = extractToken(env);
        return bookingService.createBooking(input, token);
    }

    // ════════════════════════════════════════════
    //  DATA AGGREGATION — Trip.bookings field
    //  (auto-fetches bookings when client requests
    //   bookings inside a Trip query)
    // ════════════════════════════════════════════

    @SchemaMapping(typeName = "Trip", field = "bookings")
    public List<Booking> bookingsForTrip(Trip trip, DataFetchingEnvironment env) {
        String token = extractToken(env);
        return bookingService.getBookingsByTrip(trip.getId(), token);
    }

    // ════════════════════════════════════════════
    //  HEALTH
    // ════════════════════════════════════════════

    @QueryMapping
    public String health() {
        return "GraphQL Orchestrator is up";
    }

    // ════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════

    /**
     * Extract the raw JWT from the GraphQL context.
     * The token is placed into context by JwtGraphQlInterceptor.
     * Returns null for public operations (login/register handle that themselves).
     */
    private String extractToken(DataFetchingEnvironment env) {
        Object token = env.getGraphQlContext().get("jwtToken");
        return token != null ? token.toString() : null;
    }
}
