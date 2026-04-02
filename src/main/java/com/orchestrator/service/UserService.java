package com.orchestrator.service;

import com.orchestrator.dto.OrchestratorDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USER_SERVICE = "USER-SERVICE"; // Eureka registered name

    private final ServiceDiscoveryClient discovery;

    // ── Register ───────────────────────────────────────────────────────────

    public RegisterResponse register(RegisterInput input) {
        UserRegisterBody body = new UserRegisterBody(
                input.getFirstName(),
                input.getLastName(),
                input.getEmailId(),
                input.getPassword(),
                input.getMobileNumber()
        );
        try {
            String msg = discovery.postForString(USER_SERVICE, "/api/auth/register", body);
            return new RegisterResponse(true, msg != null ? msg : "Registered successfully");
        } catch (RuntimeException e) {
            log.warn("Register error: {}", e.getMessage());
            String msg = e.getMessage();
            // Strip service prefix for cleaner error message
            if (msg != null && msg.contains(":")) {
                msg = msg.substring(msg.lastIndexOf(":") + 1).trim();
            }
            return new RegisterResponse(false, msg);
        }
    }

    // ── Login ──────────────────────────────────────────────────────────────

    public AuthResponse login(LoginInput input) {
        UserLoginBody body = new UserLoginBody(input.getEmail(), input.getPassword());
        try {
            UserLoginResponse resp = discovery.post(USER_SERVICE, "/api/auth/login",
                    body, UserLoginResponse.class);
            if (resp != null && resp.getToken() != null) {
                return new AuthResponse(true, resp.getMessage(), resp.getToken(), resp.getUserId());
            }
            return new AuthResponse(false, resp != null ? resp.getMessage() : "Login failed", null, null);
        } catch (RuntimeException e) {
            log.warn("Login error: {}", e.getMessage());
            return new AuthResponse(false, "Invalid credentials", null, null);
        }
    }

    // ── Fetch Profile ──────────────────────────────────────────────────────

    public UserProfile getProfile(String userId, String jwtToken) {
        try {
            return discovery.getWithToken(USER_SERVICE,
                    "/api/users/" + userId, jwtToken, UserProfile.class);
        } catch (RuntimeException e) {
            log.warn("Get profile error for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }
}
