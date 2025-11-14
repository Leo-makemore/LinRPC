package com.yupi.yurpc.telemetry;

import com.yupi.yurpc.config.TelemetryConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 可观测性管理器，负责初始化指标与链路追踪
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Slf4j
@SuppressWarnings("NullAway")
public final class TelemetryManager {

    private static final String METRIC_TIMER = "rpc.call.duration";
    private static final String METRIC_COUNTER = "rpc.call.total";

    private static volatile boolean enabled = false;
    private static MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private static Tracer tracer = OpenTelemetry.noop().getTracer("yurpc");
    private static OpenTelemetrySdk openTelemetrySdk;
    private static String applicationName = "yu-rpc";
    private static Vertx metricsVertx;
    private static HttpServer metricsServer;

    private TelemetryManager() {
    }

    /**
     * 初始化可观测性
     *
     * @param telemetryConfig 可观测性配置
     * @param appName         应用名称
     */
    public static synchronized void init(TelemetryConfig telemetryConfig, String appName) {
        applicationName = appName;
        shutdown();
        if (telemetryConfig == null || !telemetryConfig.isEnabled()) {
            enabled = false;
            meterRegistry = new SimpleMeterRegistry();
            tracer = OpenTelemetry.noop().getTracer(applicationName);
            openTelemetrySdk = null;
            log.info("telemetry disabled");
            return;
        }
        enabled = true;
        meterRegistry = createMeterRegistry(telemetryConfig);
        tracer = createTracer(telemetryConfig, applicationName);
        log.info("telemetry enabled, metricsExporter={}, tracingExporter={}",
                telemetryConfig.getMetricsExporter(), telemetryConfig.getTracingExporter());
    }

    private static MeterRegistry createMeterRegistry(TelemetryConfig config) {
        if (!config.isMetricsEnabled()) {
            return new SimpleMeterRegistry();
        }
        String exporter = Objects.requireNonNullElse(config.getMetricsExporter(), "simple");
        if ("prometheus".equalsIgnoreCase(exporter)) {
            PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
            startPrometheusEndpoint(registry, config.getMetricsPort());
            return registry;
        }
        return new SimpleMeterRegistry();
    }

    private static Tracer createTracer(TelemetryConfig config, String appName) {
        if (!config.isTracingEnabled()) {
            return OpenTelemetry.noop().getTracer(appName);
        }
        SpanExporter spanExporter;
        String tracingExporter = Objects.requireNonNullElse(config.getTracingExporter(), "logging");
        if ("otlp".equalsIgnoreCase(tracingExporter)) {
            spanExporter = OtlpGrpcSpanExporter.builder()
                    .setEndpoint(Objects.requireNonNullElse(config.getOtlpEndpoint(), "http://localhost:4317"))
                    .build();
        } else {
            spanExporter = LoggingSpanExporter.create();
        }
        Attributes attributes = Attributes.builder()
                .put(AttributeKey.stringKey("service.name"), Objects.requireNonNullElse(appName, "yu-rpc"))
                .build();
        Resource resource = Resource.getDefault()
                .merge(Resource.create(attributes));

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .build();
        openTelemetrySdk = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
        return openTelemetrySdk.getTracer("com.yupi.yurpc");
    }

    public static TelemetryContext startClientTelemetry(String serviceName, String methodName) {
        return startTelemetry("client", SpanKind.CLIENT, serviceName, methodName);
    }

    public static TelemetryContext startServerTelemetry(String serviceName, String methodName) {
        return startTelemetry("server", SpanKind.SERVER, serviceName, methodName);
    }

    private static TelemetryContext startTelemetry(String side, SpanKind spanKind, String serviceName, String methodName) {
        if (!enabled) {
            return TelemetryContext.disabled();
        }
        String safeSide = Objects.requireNonNullElse(side, "unknown");
        String safeService = Objects.requireNonNullElse(serviceName, "unknown");
        String safeMethod = Objects.requireNonNullElse(methodName, "unknown");
        Timer.Sample sample = meterRegistry != null ? Timer.start(meterRegistry) : null;
        Span span = tracer.spanBuilder("rpc." + safeSide)
                .setSpanKind(spanKind)
                .setAttribute("rpc.system", "yurpc")
                .setAttribute("rpc.service", safeService)
                .setAttribute("rpc.method", safeMethod)
                .setAttribute("rpc.role", safeSide)
                .startSpan();
        return new TelemetryContext(true, sample, span, safeSide, safeService, safeMethod);
    }

    public static void finishTelemetry(TelemetryContext context, boolean success, Throwable error) {
        if (context == null || !context.isEnabled()) {
            return;
        }
        String safeService = Objects.requireNonNullElse(context.getServiceName(), "unknown");
        String safeMethod = Objects.requireNonNullElse(context.getMethodName(), "unknown");
        String safeRole = Objects.requireNonNullElse(context.getSide(), "unknown");
        Tags tags = Tags.of(
                "rpc.system", "yurpc",
                "rpc.service", safeService,
                "rpc.method", safeMethod,
                "rpc.role", safeRole,
                "rpc.status", success ? "success" : "error"
        );
        if (context.getTimerSample() != null && meterRegistry != null) {
            Timer timer = Timer.builder(METRIC_TIMER)
                    .description("RPC 调用耗时")
                    .tags(tags)
                    .register(meterRegistry);
            context.getTimerSample().stop(timer);
        }
        if (meterRegistry != null) {
            Counter counter = Counter.builder(METRIC_COUNTER)
                    .description("RPC 调用次数统计")
                    .tags(tags)
                    .register(meterRegistry);
            counter.increment();
        }
        Span span = context.getSpan();
        if (span != null) {
            if (success) {
                span.setStatus(StatusCode.OK);
            } else {
                span.setStatus(StatusCode.ERROR);
                if (error != null) {
                    span.recordException(error);
                }
            }
            span.end();
        }
    }

    /**
     * 释放资源
     */
    public static synchronized void shutdown() {
        if (openTelemetrySdk != null) {
            try {
                openTelemetrySdk.close();
            } catch (Exception e) {
                log.warn("failed to close telemetry sdk", e);
            } finally {
                openTelemetrySdk = null;
            }
        }
        if (metricsServer != null) {
            metricsServer.close(result -> {
                if (result.failed()) {
                    log.warn("failed to close metrics server", result.cause());
                }
            });
            metricsServer = null;
        }
        if (metricsVertx != null) {
            metricsVertx.close(result -> {
                if (result.failed()) {
                    log.warn("failed to close metrics vertx", result.cause());
                }
            });
            metricsVertx = null;
        }
    }

    public static MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    public static String getApplicationName() {
        return applicationName;
    }

    private static void startPrometheusEndpoint(PrometheusMeterRegistry registry, int port) {
        try {
            metricsVertx = Vertx.vertx();
            metricsServer = metricsVertx.createHttpServer()
                    .requestHandler(request -> {
                        if ("/metrics".equals(request.path())) {
                            request.response()
                                    .putHeader("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
                                    .end(registry.scrape());
                        } else {
                            request.response().setStatusCode(404).end();
                        }
                    });
            metricsServer.listen(port, result -> {
                if (result.succeeded()) {
                    log.info("prometheus metrics server started on port {}", port);
                } else {
                    log.error("failed to start prometheus metrics server", result.cause());
                }
            });
        } catch (Exception e) {
            log.error("failed to bootstrap prometheus endpoint", e);
        }
    }
}


