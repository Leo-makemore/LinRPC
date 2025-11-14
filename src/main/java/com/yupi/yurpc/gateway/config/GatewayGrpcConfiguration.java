package com.yupi.yurpc.gateway.config;

import com.yupi.yurpc.grpc.proto.CalcServiceGrpc;
import com.yupi.yurpc.grpc.proto.RecommendationServiceGrpc;
import com.yupi.yurpc.grpc.proto.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("gateway")
public class GatewayGrpcConfiguration {

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(
            @GrpcClient("userService") UserServiceGrpc.UserServiceBlockingStub stub) {
        return stub;
    }

    @Bean
    public RecommendationServiceGrpc.RecommendationServiceBlockingStub recommendationServiceBlockingStub(
            @GrpcClient("recommendationService") RecommendationServiceGrpc.RecommendationServiceBlockingStub stub) {
        return stub;
    }

    @Bean
    public CalcServiceGrpc.CalcServiceBlockingStub calcServiceBlockingStub(
            @GrpcClient("calcService") CalcServiceGrpc.CalcServiceBlockingStub stub) {
        return stub;
    }
}


