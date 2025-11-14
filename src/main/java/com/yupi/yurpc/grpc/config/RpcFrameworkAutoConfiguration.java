package com.yupi.yurpc.grpc.config;

import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.config.RpcConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化 RPC 框架配置与 Telemetry。
 */
@Configuration
@EnableConfigurationProperties(RpcConfig.class)
public class RpcFrameworkAutoConfiguration {

    @Bean
    CommandLineRunner rpcInitializer(RpcConfig rpcConfig) {
        return args -> RpcApplication.init(rpcConfig);
    }
}


