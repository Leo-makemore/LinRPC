package com.yupi.yurpc.telemetry;

import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 可观测性上下文
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Getter
@AllArgsConstructor
public class TelemetryContext {

    private final boolean enabled;
    private final Timer.Sample timerSample;
    private final Span span;
    private final String side;
    private final String serviceName;
    private final String methodName;

    private static final TelemetryContext DISABLED = new TelemetryContext(false, null, Span.getInvalid(), "", "", "");

    public static TelemetryContext disabled() {
        return DISABLED;
    }
}


