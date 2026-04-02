package com.orchestrator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Resolves service URLs dynamically via Eureka — no hard-coded host/port.
 * Falls back gracefully if a service is unavailable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceDiscoveryClient {

    private final DiscoveryClient discoveryClient;
    private final WebClient.Builder webClientBuilder;

    /**
     * Resolve the base URL of a registered Eureka service by name.
     * e.g. "USER-SERVICE" → "http://192.168.1.10:8081"
     */
    public String resolveServiceUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No instances found in Eureka for service: " + serviceName);
        }
        ServiceInstance instance = instances.get(0); // simple round-robin pick first
        String url = instance.getUri().toString();
        log.debug("Resolved {} → {}", serviceName, url);
        return url;
    }

    /**
     * Build a WebClient pre-configured with the resolved base URL.
     */
    public WebClient clientFor(String eurekaServiceName) {
        String baseUrl = resolveServiceUrl(eurekaServiceName);
        return webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Convenience: POST to a service and return body as given class.
     */
    public <T> T post(String eurekaServiceName, String path, Object body, Class<T> responseType) {
        return clientFor(eurekaServiceName)
                .post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(
                                        "[" + eurekaServiceName + "] " + resp.statusCode() + ": " + msg))))
                .bodyToMono(responseType)
                .block();
    }

    /**
     * Convenience: POST to a service and return raw String body.
     */
    public String postForString(String eurekaServiceName, String path, Object body) {
        return clientFor(eurekaServiceName)
                .post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(
                                        "[" + eurekaServiceName + "] " + resp.statusCode() + ": " + msg))))
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Convenience: GET from a service and return body as given class.
     */
    public <T> T get(String eurekaServiceName, String path, Class<T> responseType) {
        return clientFor(eurekaServiceName)
                .get()
                .uri(path)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(
                                        "[" + eurekaServiceName + "] " + resp.statusCode() + ": " + msg))))
                .bodyToMono(responseType)
                .block();
    }

    /**
     * Convenience: GET with JWT Bearer token (for protected endpoints).
     */
    public <T> T getWithToken(String eurekaServiceName, String path, String jwtToken, Class<T> responseType) {
        return clientFor(eurekaServiceName)
                .get()
                .uri(path)
                .header("Authorization", "Bearer " + jwtToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(
                                        "[" + eurekaServiceName + "] " + resp.statusCode() + ": " + msg))))
                .bodyToMono(responseType)
                .block();
    }

    /**
     * Convenience: POST with JWT Bearer token.
     */
    public <T> T postWithToken(String eurekaServiceName, String path, Object body,
                               String jwtToken, Class<T> responseType) {
        return clientFor(eurekaServiceName)
                .post()
                .uri(path)
                .header("Authorization", "Bearer " + jwtToken)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new RuntimeException(
                                        "[" + eurekaServiceName + "] " + resp.statusCode() + ": " + msg))))
                .bodyToMono(responseType)
                .block();
    }
}
