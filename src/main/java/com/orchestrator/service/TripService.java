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
public class TripService {

    private static final String TRIP_SERVICE = "TRIP-SERVICE"; // Eureka name

    private final ServiceDiscoveryClient discovery;

    public Trip createTrip(CreateTripInput input, String jwtToken) {
        try {
            return discovery.postWithToken(TRIP_SERVICE, "/api/trips", input, jwtToken, Trip.class);
        } catch (RuntimeException e) {
            log.error("createTrip error: {}", e.getMessage());
            throw new RuntimeException("Failed to create trip: " + e.getMessage());
        }
    }

    public Trip getTripById(String tripId, String jwtToken) {
        try {
            return discovery.getWithToken(TRIP_SERVICE, "/api/trips/" + tripId, jwtToken, Trip.class);
        } catch (RuntimeException e) {
            log.warn("getTripById error tripId={}: {}", tripId, e.getMessage());
            return null;
        }
    }

    public List<Trip> getTripsByUser(String userId, String jwtToken) {
        try {
            // Use raw WebClient for typed List response
            return discovery.clientFor(TRIP_SERVICE)
                    .get()
                    .uri("/api/trips/user/" + userId)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Trip>>() {})
                    .block();
        } catch (RuntimeException e) {
            log.warn("getTripsByUser error userId={}: {}", userId, e.getMessage());
            return List.of();
        }
    }
}
