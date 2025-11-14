package com.yupi.yurpc.gateway.controller;

import com.yupi.yurpc.grpc.proto.RecommendationItem;
import com.yupi.yurpc.grpc.proto.RecommendationResponse;
import com.yupi.yurpc.grpc.proto.RecommendationServiceGrpc;
import com.yupi.yurpc.grpc.proto.ResponseMetadata;
import com.yupi.yurpc.grpc.proto.StrategySnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecommendationGatewayControllerTest {

    private final RecommendationServiceGrpc.RecommendationServiceBlockingStub recommendationStub =
            Mockito.mock(RecommendationServiceGrpc.RecommendationServiceBlockingStub.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RecommendationGatewayController(recommendationStub)).build();
    }

    @Test
    void returnsRecommendationList() throws Exception {
        RecommendationResponse response = RecommendationResponse.newBuilder()
                .addAllItems(List.of(
                        RecommendationItem.newBuilder()
                                .setTitle("Kubernetes Starter Lab")
                                .setCategory("cloud-native")
                                .setScore(0.9)
                                .build()))
                .setMetadata(ResponseMetadata.newBuilder()
                        .setStrategy(StrategySnapshot.newBuilder()
                                .setSerializer("json")
                                .setLoadBalancer("roundRobin")
                                .setRetryStrategy("fixed")
                                .setTolerantStrategy("failFast")
                                .build())
                        .build())
                .build();
        Mockito.when(recommendationStub.getRecommendations(any())).thenReturn(response);

        mockMvc.perform(get("/api/recommendations").param("userId", "1003").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("Kubernetes Starter Lab"))
                .andExpect(jsonPath("$.loadBalancer").value("roundRobin"));
    }
}


