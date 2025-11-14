package com.yupi.yurpc.fault.retry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 重试策略测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">程序员鱼皮的编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class RetryStrategyTest {

    RetryStrategy retryStrategy = new NoRetryStrategy();

    @Test
    public void doRetry() {
        Assertions.assertThrows(RuntimeException.class, () -> retryStrategy.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟重试失败");
        }));
    }
}