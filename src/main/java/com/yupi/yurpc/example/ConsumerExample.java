package com.yupi.yurpc.example;

import com.yupi.yurpc.bootstrap.ConsumerBootstrap;
import com.yupi.yurpc.proxy.ServiceProxyFactory;

/**
 * 服务消费者示例
 * 初始化后可以通过代理调用远程服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class ConsumerExample {

    public static void main(String[] args) {
        // 服务消费者初始化
        ConsumerBootstrap.init();

        // 获取代理对象
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);

        // 调用远程方法
        String userName = userService.getUserName(123L);
        System.out.println("获取用户名: " + userName);

        Integer userCount = userService.getUserCount();
        System.out.println("获取用户数量: " + userCount);
    }
}

