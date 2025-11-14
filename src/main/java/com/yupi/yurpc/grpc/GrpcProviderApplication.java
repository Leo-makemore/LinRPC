package com.yupi.yurpc.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

/**
 * Spring Boot 应用入口（Provider）。
 */
@SpringBootApplication
public class GrpcProviderApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(GrpcProviderApplication.class);
        application.setDefaultProperties(Map.of("spring.profiles.active", "provider"));
        application.run(args);
    }
}


