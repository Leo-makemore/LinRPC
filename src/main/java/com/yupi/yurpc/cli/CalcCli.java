package com.yupi.yurpc.cli;

import com.yupi.yurpc.grpc.proto.CalcServiceGrpc;
import com.yupi.yurpc.grpc.proto.ComputeRequest;
import com.yupi.yurpc.grpc.proto.ComputeResponse;
import com.yupi.yurpc.grpc.proto.ResponseMetadata;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * 简易 Calc CLI，可快速验证 CalcService + 策略元数据
 */
public final class CalcCli {

    private CalcCli() {
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: CalcCli <operator> <left> <right> [host] [port]");
            System.exit(1);
        }
        String operator = args[0];
        double left = Double.parseDouble(args[1]);
        double right = Double.parseDouble(args[2]);
        String host = args.length > 3 ? args[3] : "localhost";
        int port = args.length > 4 ? Integer.parseInt(args[4]) : 9091;

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        try {
            CalcServiceGrpc.CalcServiceBlockingStub stub = CalcServiceGrpc.newBlockingStub(channel);
            ComputeResponse response = stub.compute(ComputeRequest.newBuilder()
                    .setOperator(operator)
                    .setLeft(left)
                    .setRight(right)
                    .build());
            System.out.printf("Result: %s%n", response.getResult());
            printMetadata(response.getMetadata());
        } finally {
            channel.shutdownNow();
        }
    }

    private static void printMetadata(ResponseMetadata metadata) {
        if (metadata == null || !metadata.hasStrategy()) {
            System.out.println("metadata unavailable");
            return;
        }
        System.out.printf("Strategy -> serializer=%s, loadBalancer=%s, retry=%s, tolerant=%s%n",
                metadata.getStrategy().getSerializer(),
                metadata.getStrategy().getLoadBalancer(),
                metadata.getStrategy().getRetryStrategy(),
                metadata.getStrategy().getTolerantStrategy());
        System.out.printf("Timestamp -> %s%n", metadata.getTimestamp());
    }
}


