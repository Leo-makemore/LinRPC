package com.yupi.yurpc.registry;

import com.yupi.yurpc.config.RegistryConfig;
import com.yupi.yurpc.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于内存的注册中心实现，适合本地演示或单机开发环境。
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Slf4j
public class InMemoryRegistry implements Registry {

    /**
     * 服务信息存储，key 为 serviceKey
     */
    private final Map<String, List<ServiceMetaInfo>> registryData = new ConcurrentHashMap<>();

    @Override
    public void init(RegistryConfig registryConfig) {
        log.info("in-memory registry init, config = {}", registryConfig);
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) {
        String serviceKey = serviceMetaInfo.getServiceKey();
        registryData.computeIfAbsent(serviceKey, ignored -> new CopyOnWriteArrayList<>())
                .add(serviceMetaInfo);
        log.info("service registered in memory: {}", serviceMetaInfo);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String serviceKey = serviceMetaInfo.getServiceKey();
        registryData.computeIfPresent(serviceKey, (key, serviceMetaInfos) -> {
            serviceMetaInfos.removeIf(info -> info.getServiceAddress().equals(serviceMetaInfo.getServiceAddress()));
            return serviceMetaInfos.isEmpty() ? null : serviceMetaInfos;
        });
        log.info("service unregistered from memory: {}", serviceMetaInfo);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        return registryData.getOrDefault(serviceKey, Collections.emptyList());
    }

    @Override
    public void heartBeat() {
        // 内存注册中心无需心跳检测
    }

    @Override
    public void watch(String serviceNodeKey) {
        // 内存注册中心无需监听机制
    }

    @Override
    public void destroy() {
        registryData.clear();
        log.info("in-memory registry destroyed");
    }
}


