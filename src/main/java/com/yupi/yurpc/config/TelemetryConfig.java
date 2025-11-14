package com.yupi.yurpc.config;

import lombok.Data;

/**
 * 可观测性配置
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class TelemetryConfig {

    /**
     * 总开关
     */
    private boolean enabled = false;

    /**
     * 是否开启指标采集
     */
    private boolean metricsEnabled = true;

    /**
     * 指标导出器: simple | prometheus
     */
    private String metricsExporter = "simple";

    /**
     * Prometheus 指标 HTTP 端口
     */
    private int metricsPort = 9404;

    /**
     * 是否开启链路追踪
     */
    private boolean tracingEnabled = true;

    /**
     * 链路追踪导出器: logging | otlp
     */
    private String tracingExporter = "logging";

    /**
     * OTLP 导出端点，仅在 tracingExporter=otlp 时生效
     */
    private String otlpEndpoint = "http://localhost:4317";
}


