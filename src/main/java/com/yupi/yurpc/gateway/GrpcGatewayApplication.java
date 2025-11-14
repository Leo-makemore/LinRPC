package com.yupi.yurpc.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

/**
 * REST Gateway bridging HTTP to gRPC
 */
@SpringBootApplication(scanBasePackages = "com.yupi.yurpc")
@Profile("gateway")
public class GrpcGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcGatewayApplication.class, args);
    }
}


