package com.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GraphqlOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(GraphqlOrchestratorApplication.class, args);
    }
}
