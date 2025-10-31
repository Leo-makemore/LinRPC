# Yu-RPC 框架

一个轻量级、高性能的 Java RPC 框架，支持多种序列化方式、负载均衡策略、容错机制和注册中心。

## 特性

- ✅ **多种序列化方式**：支持 JDK、JSON、Kryo、Hessian 序列化
- ✅ **负载均衡**：支持随机、轮询、一致性哈希等负载均衡策略
- ✅ **容错机制**：支持快速失败、安全失败、容错等策略
- ✅ **重试机制**：支持多种重试策略
- ✅ **注册中心**：支持 Etcd、ZooKeeper 注册中心
- ✅ **SPI 机制**：基于 SPI 的可扩展架构
- ✅ **TCP/HTTP 协议**：支持 TCP 和 HTTP 两种协议

## 快速开始

### 前置要求

- JDK 8 或更高版本
- Maven 3.6+
- （可选）Etcd 或 ZooKeeper 用于服务注册与发现

### 安装

```bash
git clone <repository-url>
cd yu-rpc-core
mvn clean install
```

### 配置文件

在 `src/main/resources/application.properties` 中配置 RPC 框架参数：

```properties
# RPC 框架配置
rpc.name=yu-rpc
rpc.version=1.0
rpc.serverHost=localhost
rpc.serverPort=8080

# 序列化器 (jdk, json, kryo, hessian)
rpc.serializer=jdk

# 负载均衡器 (random, roundRobin, consistentHash)
rpc.loadBalancer=roundRobin

# 重试策略 (no, fixedInterval)
rpc.retryStrategy=no

# 容错策略 (failFast, failSafe, faultTolerant)
rpc.tolerantStrategy=failFast

# 注册中心配置
rpc.registryConfig.registry=etcd
rpc.registryConfig.address=http://localhost:2379
rpc.registryConfig.timeout=10000
```

### 使用示例

#### 1. 定义服务接口

```java
public interface UserService {
    String getUserName(Long userId);
    Integer getUserCount();
}
```

#### 2. 实现服务

```java
public class UserServiceImpl implements UserService {
    @Override
    public String getUserName(Long userId) {
        return "User_" + userId;
    }
    
    @Override
    public Integer getUserCount() {
        return 100;
    }
}
```

#### 3. 启动服务提供者（Provider）

```java
public class ProviderExample {
    public static void main(String[] args) {
        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
        ServiceRegisterInfo<UserService> serviceRegisterInfo = new ServiceRegisterInfo<>(
                UserService.class.getName(),
                UserServiceImpl.class
        );
        serviceRegisterInfoList.add(serviceRegisterInfo);
        
        ProviderBootstrap.init(serviceRegisterInfoList);
        System.out.println("服务提供者启动成功！");
    }
}
```

#### 4. 启动服务消费者（Consumer）

```java
public class ConsumerExample {
    public static void main(String[] args) {
        // 初始化消费者
        ConsumerBootstrap.init();
        
        // 获取代理对象
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        
        // 调用远程方法
        String userName = userService.getUserName(123L);
        System.out.println("获取用户名: " + userName);
    }
}
```

## 项目结构

```
yu-rpc-core/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/yupi/yurpc/
│   │   │       ├── bootstrap/          # 启动类
│   │   │       ├── config/             # 配置类
│   │   │       ├── constant/           # 常量定义
│   │   │       ├── example/            # 使用示例
│   │   │       ├── exception/           # 异常定义
│   │   │       ├── fault/              # 容错和重试
│   │   │       ├── loadbalancer/       # 负载均衡
│   │   │       ├── model/              # 数据模型
│   │   │       ├── protocol/           # 协议定义
│   │   │       ├── proxy/              # 代理实现
│   │   │       ├── registry/           # 注册中心
│   │   │       ├── serializer/         # 序列化器
│   │   │       ├── server/             # 服务器
│   │   │       └── spi/                # SPI 加载器
│   │   └── resources/
│   │       ├── application.properties  # 配置文件
│   │       └── META-INF/               # SPI 配置
│   └── test/
│       └── java/                      # 测试代码
└── pom.xml
```

## 核心组件

### 序列化器

支持以下序列化方式：
- **JDK**：Java 原生序列化
- **JSON**：JSON 序列化（基于 Hutool）
- **Kryo**：高性能二进制序列化
- **Hessian**：跨语言序列化

### 负载均衡器

- **Random**：随机选择
- **RoundRobin**：轮询选择
- **ConsistentHash**：一致性哈希

### 容错策略

- **FailFast**：快速失败
- **FailSafe**：安全失败
- **FaultTolerant**：容错降级

### 注册中心

- **Etcd**：基于 Etcd 的注册中心
- **ZooKeeper**：基于 ZooKeeper 的注册中心

## 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=LoadBalancerTest
mvn test -Dtest=ProtocolMessageTest
mvn test -Dtest=RetryStrategyTest
```

## 开发

### 编译项目

```bash
mvn clean compile
```

### 打包项目

```bash
mvn clean package
```

### 安装到本地仓库

```bash
mvn clean install
```

## 配置说明

### 序列化器配置

```properties
rpc.serializer=jdk      # JDK 序列化
rpc.serializer=json     # JSON 序列化
rpc.serializer=kryo     # Kryo 序列化
rpc.serializer=hessian  # Hessian 序列化
```

### 负载均衡配置

```properties
rpc.loadBalancer=random          # 随机
rpc.loadBalancer=roundRobin    # 轮询
rpc.loadBalancer=consistentHash # 一致性哈希
```

### 注册中心配置

#### Etcd

```properties
rpc.registryConfig.registry=etcd
rpc.registryConfig.address=http://localhost:2379
rpc.registryConfig.timeout=10000
```

#### ZooKeeper

```properties
rpc.registryConfig.registry=zookeeper
rpc.registryConfig.address=localhost:2181
rpc.registryConfig.timeout=10000
```

## 扩展开发

框架基于 SPI 机制，支持自定义扩展：

1. 实现对应接口（如 `Serializer`、`LoadBalancer`、`Registry` 等）
2. 在 `META-INF/rpc/custom/` 目录下创建配置文件
3. 配置实现类的全限定名

## 注意事项

1. 使用 Etcd 注册中心时，需要确保 Etcd 服务已启动
2. 使用 ZooKeeper 注册中心时，需要确保 ZooKeeper 服务已启动
3. 测试时需要根据实际情况修改注册中心地址
4. 某些功能（如注册中心）需要外部依赖，测试时可能会失败

## 技术栈

- **Java 8+**
- **Vert.x**：异步网络框架
- **Netty**：高性能网络通信
- **Lombok**：简化代码
- **Hutool**：Java 工具类库
- **Etcd**：分布式键值存储
- **ZooKeeper**：分布式协调服务
- **Kryo**：快速序列化
- **Hessian**：二进制序列化

## 许可证

本项目采用 MIT 许可证。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 作者

- 原作者：<a href="https://github.com/liyupi">程序员鱼皮</a>
- 学习资源：<a href="https://codefather.cn">编程宝典</a>
- 学习社区：<a href="https://yupi.icu">编程导航知识星球</a>

