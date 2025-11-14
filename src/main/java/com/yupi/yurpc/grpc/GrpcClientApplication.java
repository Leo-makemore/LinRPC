package com.yupi.yurpc.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

/**
 * Spring Boot 应用入口（Client）。
 */
@SpringBootApplication
public class GrpcClientApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(GrpcClientApplication.class);
        application.setDefaultProperties(Map.of("spring.profiles.active", "client"));
        application.run(args);
    }
}


