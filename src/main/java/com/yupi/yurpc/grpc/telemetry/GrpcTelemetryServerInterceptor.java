package com.yupi.yurpc.grpc.telemetry;

import com.yupi.yurpc.telemetry.TelemetryContext;
import com.yupi.yurpc.telemetry.TelemetryManager;
import io.grpc.*;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * gRPC 服务端可观测性拦截器。
 */
@Component
@Profile("provider")
@GrpcGlobalServerInterceptor
public class GrpcTelemetryServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                 Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        String fullMethodName = call.getMethodDescriptor().getFullMethodName();
        String serviceName = extractServiceName(fullMethodName);
        String methodName = extractMethodName(fullMethodName);
        TelemetryContext telemetryContext = TelemetryManager.startServerTelemetry(serviceName, methodName);
        AtomicBoolean finished = new AtomicBoolean(false);

        java.util.function.BiConsumer<Status, Throwable> finish = (status, error) -> {
            if (finished.compareAndSet(false, true)) {
                boolean success = status.isOk();
                TelemetryManager.finishTelemetry(telemetryContext, success, error);
            }
        };

        ServerCall<ReqT, RespT> monitoringCall = new SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                finish.accept(status, status.isOk() ? null : status.asRuntimeException());
                super.close(status, trailers);
            }
        };

        ServerCall.Listener<ReqT> listener = next.startCall(monitoringCall, headers);
        return new SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onCancel() {
                finish.accept(Status.CANCELLED, Status.CANCELLED.asRuntimeException());
                super.onCancel();
            }
        };
    }

    private String extractServiceName(String fullMethodName) {
        int slash = fullMethodName.indexOf('/');
        return slash > 0 ? fullMethodName.substring(0, slash) : fullMethodName;
    }

    private String extractMethodName(String fullMethodName) {
        int slash = fullMethodName.indexOf('/');
        return slash > 0 ? fullMethodName.substring(slash + 1) : fullMethodName;
    }
}


