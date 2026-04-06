# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Build without tests
./mvnw clean package -DskipTests
```

The app starts on port **8080**. GraphiQL UI is available at `http://localhost:8080/graphiql`.

## Architecture

This is a **Spring Boot 3 / Java 17** GraphQL API gateway (orchestrator) for a travel booking app. It sits between a React frontend and four downstream microservices, all discovered via **Eureka** (no hard-coded service URLs).

### Request flow

```
React FE (localhost:3000)
    → POST /graphql
        → JwtGraphQlInterceptor  (validates JWT, puts token + userId into GraphQL context)
            → GraphQlResolver    (single @Controller for all queries/mutations)
                → *Service       (UserService, TripService, BookingService, NotificationService)
                    → ServiceDiscoveryClient  (resolves Eureka name → URL, calls REST via WebClient)
                        → downstream microservice
```

### Key design decisions

- **JWT validation happens at the GraphQL layer**, not in Spring Security. `OrchestratorConfig` permits all `/graphql` requests; `JwtGraphQlInterceptor` validates the token and places it in the GraphQL context. Resolvers extract it via `env.getGraphQlContext().get("jwtToken")` and forward it to downstream services.
- **`ServiceDiscoveryClient`** is the single WebClient wrapper used by all service classes. It resolves Eureka service names (e.g. `"USER-SERVICE"`, `"TRIP-SERVICE"`) to base URLs and provides typed convenience methods: `get`, `post`, `getWithToken`, `postWithToken`, `postForString`.
- **All DTOs** live in a single file: `OrchestratorDtos.java`. The inner classes mirror both the GraphQL schema types and the REST contracts of downstream services (which sometimes differ — e.g. `CreateTripInput.name` maps to `TripCreateBody.hotelName`).
- **`BookingService.createBooking`** orchestrates a two-step flow: call booking-service to create the booking, then call notification-service to send a confirmation email. Notification failure is non-fatal (logged as WARN, booking still returned).
- **`Trip.bookings`** is a federated field resolved by `@SchemaMapping` — when a client includes `bookings` inside a `Trip` query, `GraphQlResolver.bookingsForTrip` automatically fetches them from booking-service.

### Downstream Eureka service names

| Eureka name            | Used by             |
|------------------------|---------------------|
| `USER-SERVICE`         | UserService         |
| `TRIP-SERVICE`         | TripService         |
| `BOOKING-SERVICE`      | BookingService      |
| `NOTIFICATION-SERVICE` | NotificationService |

### Prerequisites to run locally

- Eureka server running at `http://localhost:8761`
- All four downstream services registered in Eureka
- The `jwt.secret` in `application.properties` must match the secret used by `user-service` to issue tokens
- CORS is configured to allow only `http://localhost:3000`
