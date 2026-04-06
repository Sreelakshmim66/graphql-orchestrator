package com.orchestrator.service;

import com.orchestrator.dto.OrchestratorDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String NOTIFICATION_SERVICE = "NOTIFICATION-SERVICE"; // Eureka name

    private final ServiceDiscoveryClient discovery;

    public NotificationResponse sendNotification(String itinNumber, String userName,
                                                  String userEmail, String message) {
        try {
            NotificationRequestBody body = new NotificationRequestBody(itinNumber, userName, userEmail, message);
            return discovery.post(NOTIFICATION_SERVICE, "/api/notifications", body, NotificationResponse.class);
        } catch (RuntimeException e) {
            log.warn("sendNotification error for itinNumber={}: {}", itinNumber, e.getMessage());
            return null;
        }
    }
}
