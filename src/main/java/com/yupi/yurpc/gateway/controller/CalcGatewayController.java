package com.yupi.yurpc.gateway.controller;

import com.yupi.yurpc.grpc.proto.CalcServiceGrpc;
import com.yupi.yurpc.grpc.proto.ComputeRequest;
import com.yupi.yurpc.grpc.proto.ComputeResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalcGatewayController {

    private final CalcServiceGrpc.CalcServiceBlockingStub calcStub;

    public CalcGatewayController(CalcServiceGrpc.CalcServiceBlockingStub calcStub) {
        this.calcStub = calcStub;
    }

    @PostMapping("/api/calc")
    public CalcResult compute(@Valid @RequestBody CalcRequest request) {
        ComputeResponse response = calcStub.compute(ComputeRequest.newBuilder()
                .setLeft(request.left)
                .setRight(request.right)
                .setOperator(request.operator)
                .build());
        return new CalcResult(response.getResult(),
                response.getMetadata().getStrategy().getSerializer(),
                response.getMetadata().getTimestamp());
    }

    @Data
    public static class CalcRequest {
        @NotBlank
        private String operator;

        @NotNull
        private Double left;

        @NotNull
        private Double right;
    }

    public record CalcResult(double result, String serializer, String timestamp) {
    }
}


