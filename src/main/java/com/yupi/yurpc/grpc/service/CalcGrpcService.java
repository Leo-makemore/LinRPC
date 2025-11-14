package com.yupi.yurpc.grpc.service;

import com.yupi.yurpc.example.CalcService;
import com.yupi.yurpc.grpc.proto.CalcServiceGrpc;
import com.yupi.yurpc.grpc.proto.ComputeRequest;
import com.yupi.yurpc.grpc.proto.ComputeResponse;
import com.yupi.yurpc.grpc.proto.ResponseMetadata;
import com.yupi.yurpc.grpc.support.GrpcMetadataBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Profile;

/**
 * Calc gRPC 服务
 */
@Slf4j
@GrpcService
@Profile("provider")
@RequiredArgsConstructor
public class CalcGrpcService extends CalcServiceGrpc.CalcServiceImplBase {

    private final CalcService calcService;

    @Override
    public void compute(ComputeRequest request, StreamObserver<ComputeResponse> responseObserver) {
        double left = request.getLeft();
        double right = request.getRight();
        String operator = request.getOperator();
        try {
            double result = calcService.compute(left, right, operator);
            ResponseMetadata metadata = GrpcMetadataBuilder.build();
            ComputeResponse response = ComputeResponse.newBuilder()
                    .setResult(result)
                    .setMetadata(metadata)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.error("Calc compute error: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("Calc compute unexpected error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}


