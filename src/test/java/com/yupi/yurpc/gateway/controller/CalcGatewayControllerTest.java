package com.yupi.yurpc.gateway.controller;

import com.yupi.yurpc.grpc.proto.CalcServiceGrpc;
import com.yupi.yurpc.grpc.proto.ComputeResponse;
import com.yupi.yurpc.grpc.proto.ResponseMetadata;
import com.yupi.yurpc.grpc.proto.StrategySnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalcGatewayControllerTest {

    private final CalcServiceGrpc.CalcServiceBlockingStub calcStub = Mockito.mock(CalcServiceGrpc.CalcServiceBlockingStub.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CalcGatewayController(calcStub)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void computeReturnsResult() throws Exception {
        ComputeResponse response = ComputeResponse.newBuilder()
                .setResult(14.0)
                .setMetadata(ResponseMetadata.newBuilder()
                        .setStrategy(StrategySnapshot.newBuilder()
                                .setSerializer("json")
                                .setLoadBalancer("round-robin")
                                .setRetryStrategy("fixed")
                                .setTolerantStrategy("failFast")
                                .build())
                        .setTimestamp("2025-01-01T00:00:00Z")
                        .build())
                .build();
        Mockito.when(calcStub.compute(any())).thenReturn(response);

        CalcGatewayController.CalcRequest request = new CalcGatewayController.CalcRequest();
        request.setOperator("add");
        request.setLeft(5.0);
        request.setRight(9.0);

        mockMvc.perform(post("/api/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(14.0))
                .andExpect(jsonPath("$.serializer").value("json"));
    }
}


