package com.yupi.yurpc.registry;

import com.yupi.yurpc.config.RegistryConfig;
import com.yupi.yurpc.model.ServiceMetaInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.GenericContainer;

import java.time.Duration;
import java.util.List;

/**
 * 基于 Testcontainers 的 Etcd 集成测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@EnabledIfEnvironmentVariable(named = "RUN_ETCD_TESTS", matches = "true")
class EtcdRegistryIntegrationTest {

    private static GenericContainer<?> etcd;
    private static Registry registry;

    @BeforeAll
    static void init() {
        etcd = new GenericContainer<>("bitnami/etcd:3.5.12")
                .withEnv("ALLOW_NONE_AUTHENTICATION", "yes")
                .withEnv("ETCD_LISTEN_PEER_URLS", "http://0.0.0.0:2380")
                .withEnv("ETCD_LISTEN_CLIENT_URLS", "http://0.0.0.0:2379")
                .withEnv("ETCD_ADVERTISE_CLIENT_URLS", "http://0.0.0.0:2379")
                .withExposedPorts(2379)
                .withStartupTimeout(Duration.ofSeconds(60));
        etcd.start();

        registry = RegistryFactory.getInstance(RegistryKeys.ETCD);
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setRegistry(RegistryKeys.ETCD);
        registryConfig.setAddress("http://" + etcd.getHost() + ":" + etcd.getMappedPort(2379));
        registry.init(registryConfig);
    }

    @AfterAll
    static void destroy() {
        if (registry != null) {
            registry.destroy();
        }
        if (etcd != null) {
            etcd.stop();
        }
    }

    @Test
    void registerAndDiscover() throws Exception {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("demoService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("127.0.0.1");
        serviceMetaInfo.setServicePort(9090);
        registry.register(serviceMetaInfo);

        List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        Assertions.assertFalse(serviceMetaInfos.isEmpty());
    }
}


