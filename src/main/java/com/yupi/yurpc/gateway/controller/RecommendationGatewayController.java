package com.yupi.yurpc.gateway.controller;

import com.yupi.yurpc.grpc.proto.RecommendationRequest;
import com.yupi.yurpc.grpc.proto.RecommendationResponse;
import com.yupi.yurpc.grpc.proto.RecommendationServiceGrpc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RecommendationGatewayController {

    private final RecommendationServiceGrpc.RecommendationServiceBlockingStub recommendationStub;

    public RecommendationGatewayController(RecommendationServiceGrpc.RecommendationServiceBlockingStub recommendationStub) {
        this.recommendationStub = recommendationStub;
    }

    @GetMapping("/api/recommendations")
    public RecommendationDto recommendations(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        RecommendationResponse response = recommendationStub.getRecommendations(
                RecommendationRequest.newBuilder()
                        .setUserId(userId != null ? userId : 0L)
                        .setLimit(limit)
                        .build());
        return new RecommendationDto(
                response.getItemsList().stream()
                        .map(item -> new RecommendationItemDto(item.getTitle(), item.getCategory(), item.getScore()))
                        .collect(Collectors.toList()),
                response.getMetadata().getStrategy().getLoadBalancer());
    }

    public record RecommendationDto(List<RecommendationItemDto> items, String loadBalancer) {
    }

    public record RecommendationItemDto(String title, String category, double score) {
    }
}


