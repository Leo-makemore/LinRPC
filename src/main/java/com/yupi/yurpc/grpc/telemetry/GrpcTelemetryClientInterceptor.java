package com.yupi.yurpc.grpc.telemetry;

import com.yupi.yurpc.telemetry.TelemetryContext;
import com.yupi.yurpc.telemetry.TelemetryManager;
import io.grpc.*;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import javax.annotation.Nullable;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * gRPC 客户端可观测性拦截器。
 */
@Component
@Profile("client")
@GrpcGlobalClientInterceptor
public class GrpcTelemetryClientInterceptor implements ClientInterceptor {

    @Override
    @SuppressWarnings("NullableProblems")
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions,
                                                               Channel next) {
        String fullMethodName = method.getFullMethodName();
        String serviceName = extractServiceName(fullMethodName);
        String methodName = extractMethodName(fullMethodName);
        TelemetryContext telemetryContext = TelemetryManager.startClientTelemetry(serviceName, methodName);
        AtomicBoolean finished = new AtomicBoolean(false);

        ClientCall<ReqT, RespT> delegate = next.newCall(method, callOptions);
        return new SimpleForwardingClientCall<>(delegate) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new SimpleForwardingClientCallListener<>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        finish(status, status.isOk() ? null : status.asRuntimeException());
                        super.onClose(status, trailers);
                    }
                }, headers);
            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {
                finish(Status.CANCELLED, cause != null ? cause : Status.CANCELLED.asRuntimeException());
                super.cancel(message, cause);
            }

            private void finish(Status status, Throwable error) {
                if (finished.compareAndSet(false, true)) {
                    boolean success = status.isOk();
                    TelemetryManager.finishTelemetry(telemetryContext, success, error);
                }
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


