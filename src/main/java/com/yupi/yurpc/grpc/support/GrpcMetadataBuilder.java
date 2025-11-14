package com.yupi.yurpc.grpc.support;

import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.config.RpcConfig;
import com.yupi.yurpc.grpc.proto.ResponseMetadata;
import com.yupi.yurpc.grpc.proto.StrategySnapshot;

import java.time.OffsetDateTime;

/**
 * 构建 gRPC 响应元数据
 */
public final class GrpcMetadataBuilder {

    private static final String UNKNOWN = "unknown";

    private GrpcMetadataBuilder() {
    }

    public static ResponseMetadata build() {
        RpcConfig config = RpcApplication.getRpcConfig();
        StrategySnapshot.Builder strategy = StrategySnapshot.newBuilder()
                .setSerializer(orDefault(config.getSerializer()))
                .setLoadBalancer(orDefault(config.getLoadBalancer()))
                .setRetryStrategy(orDefault(config.getRetryStrategy()))
                .setTolerantStrategy(orDefault(config.getTolerantStrategy()));
        return ResponseMetadata.newBuilder()
                .setStrategy(strategy)
                .setTimestamp(OffsetDateTime.now().toString())
                .build();
    }

    private static String orDefault(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
    }
}


