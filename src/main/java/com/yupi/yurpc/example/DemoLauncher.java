package com.yupi.yurpc.example;

/**
 * 一键启动的本地演示程序。
 * 先启动服务提供者，再启动消费者进行一次 RPC 调用。
 */
public class DemoLauncher {

    public static void main(String[] args) throws InterruptedException {
        // 启动服务提供者
        Thread providerThread = new Thread(() -> ProviderExample.main(args), "yu-rpc-demo-provider");
        providerThread.setDaemon(true);
        providerThread.start();

        // 等待服务端启动完成
        Thread.sleep(1500L);

        // 发起一次远程调用
        ConsumerExample.main(args);

        // 预留时间打印日志
        Thread.sleep(500L);
        System.out.println("Demo finished. 按 Ctrl+C 结束程序。");
    }
}


