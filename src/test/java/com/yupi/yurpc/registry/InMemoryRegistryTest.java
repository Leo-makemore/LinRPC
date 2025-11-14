package com.yupi.yurpc.registry;

import com.yupi.yurpc.config.RegistryConfig;
import com.yupi.yurpc.model.ServiceMetaInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 本地注册中心单元测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
class InMemoryRegistryTest {

    private Registry registry;

    @BeforeEach
    void setUp() {
        registry = RegistryFactory.getInstance(RegistryKeys.LOCAL);
        registry.init(new RegistryConfig());
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
        Assertions.assertEquals(1, serviceMetaInfos.size());
        Assertions.assertEquals("127.0.0.1", serviceMetaInfos.get(0).getServiceHost());
    }

    @Test
    void unregister() throws Exception {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("demoService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("127.0.0.1");
        serviceMetaInfo.setServicePort(9090);
        registry.register(serviceMetaInfo);

        registry.unRegister(serviceMetaInfo);
        List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        Assertions.assertTrue(serviceMetaInfos.isEmpty());
    }
}


