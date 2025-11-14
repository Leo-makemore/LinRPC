# Yu-RPC Core

A lightweight, high-performance Java RPC framework. It supports multiple serializers, load balancers, retry and tolerant strategies, pluggable registries, and a simple TCP/HTTP server implementation.

## Features

- **Multiple serializers**: JDK, JSON, Kryo, Hessian
- **Load balancing**: Random, Round-robin, Consistent Hash
- **Fault tolerance**: Fail-Fast, Fail-Safe, Fault-Tolerant
- **Retry strategies**: No retry, Fixed-interval retry
- **Service registry**: Etcd, ZooKeeper (SPI pluggable)
- **gRPC + Spring Boot 3 stack**: Provider/consumer powered by gRPC, Spring Boot Actuator, Micrometer, and OpenTelemetry
- **SPI-based extensibility**: Register custom implementations via SPI files
- **TCP/HTTP servers**: Vert.x based implementations
- **Observability ready**: Micrometer metrics, OpenTelemetry tracing, Prometheus endpoint
- **Cloud native tooling**: Dockerfile, Docker Compose, GitHub Actions CI, Testcontainers integration tests
- **REST gateway bridge**: Spring MVC gateway converts HTTP/JSON to gRPC calls with strategy metadata passthrough
- **Kubernetes-ready**: Manifests for Etcd/ZooKeeper registries, provider, gateway, and demo clients

## Modern Stack Highlights

- Java 17 + Vert.x networking core with pluggable registry, serializer, and load-balancer SPI
- Micrometer metrics, OpenTelemetry tracing (logging / OTLP exporters), optional Prometheus endpoint on `/metrics`
- Resilience toolkit: retry/tolerant strategies with telemetry hooks for latency & error rate
- Testcontainers-backed Etcd integration tests and JUnit 5 unit tests
- Docker-based demo packaging and GitHub Actions (`.github/workflows/ci.yml`) CI pipeline

## Requirements

- Java 17+
- Maven 3.8+
- Optional: Etcd or ZooKeeper for service discovery/registration

## Getting Started

### Quick Demo (gRPC Provider + Client)

1. **Build once**
   ```bash
   mvn clean package
   ```
2. **Start the gRPC provider (terminal 1)**
   ```bash
   mvn spring-boot:run \
     -Dspring-boot.run.main-class=com.yupi.yurpc.grpc.GrpcProviderApplication
   ```
   - gRPC listens on `0.0.0.0:9091` (configured via `application-provider.yaml`)
   - HTTP server is disabled (`spring.main.web-application-type=none`)
   - Provider / Client profiles live in `src/main/resources/application-provider.yaml` and `application-client.yaml`
3. **Run the gRPC client demo (terminal 2)**
   ```bash
   mvn spring-boot:run \
     -Dspring-boot.run.main-class=com.yupi.yurpc.grpc.GrpcClientApplication
   ```
   The client prints the remote results and then exits.

4. **Call with `grpcurl` (optional)**
   ```bash
   # 查询单个用户 / 用户数量
   grpcurl -plaintext -d '{"userId":123}' localhost:9091 yurpc.UserService/GetUserName
   grpcurl -plaintext -d '{}' localhost:9091 yurpc.UserService/GetUserCount

   # 新增 / 列表 / 删除 / 状态切换
   grpcurl -plaintext -d '{"name":"Charlie","age":28}' localhost:9091 yurpc.UserService/CreateUser
   grpcurl -plaintext -d '{}' localhost:9091 yurpc.UserService/ListUsers
   grpcurl -plaintext -d '{"userId":1003,"active":false}' localhost:9091 yurpc.UserService/ToggleUserActive
   grpcurl -plaintext -d '{"userId":1003}' localhost:9091 yurpc.UserService/DeleteUser

   # 获取推荐列表（携带策略快照）
   grpcurl -plaintext -d '{"userId":1003,"limit":3}' localhost:9091 yurpc.RecommendationService/GetRecommendations

   # 计算服务（支持 add/sub/mul/div/mod/pow）
   grpcurl -plaintext -d '{"left":42,"right":3,"operator":"div"}' localhost:9091 yurpc.CalcService/Compute
   ```

5. **Run the Calc CLI (optional)**
   ```bash
   mvn exec:java \
     -Dexec.mainClass=com.yupi.yurpc.cli.CalcCli \
     -Dexec.args="mul 6 7"
   ```
   Pass `[host] [port]` as extra args when Provider 不在本机或端口非 9091，例如 `-Dexec.args="pow 2 10 grpc-provider 8090"`.

6. **Start the REST gateway (terminal 3, optional)**
   ```bash
   mvn spring-boot:run \
     -Dspring-boot.run.main-class=com.yupi.yurpc.gateway.GrpcGatewayApplication \
     -Dspring-boot.run.profiles=gateway
   ```
   - Gateway listens on `http://localhost:8080`
   - Sample REST calls bridging to gRPC：
     ```bash
     curl "http://localhost:8080/api/users/134"
     curl "http://localhost:8080/api/users?limit=5"
     curl -X POST "http://localhost:8080/api/users" \
       -H "Content-Type: application/json" \
       -d '{"name":"Dana","age":27}'
     curl -X POST "http://localhost:8080/api/calc" \
       -H "Content-Type: application/json" \
       -d '{"operator":"pow","left":2,"right":10}'
     curl "http://localhost:8080/api/recommendations?userId=134&limit=3"
     ```

### One-Click Script
位于仓库根目录的 `test.sh` 会自动完成编译、清理占用端口、启动 Provider、运行客户端示例与 Calc CLI，并在执行完毕后清理后台进程：
```bash
chmod +x test.sh    # 第一次运行前赋予权限
./test.sh
```
如需修改策略或演示内容，可编辑脚本顶部的 `PROVIDER_ARGS`、`CLIENT_ARGS`、`CLI_ARGS`、`GATEWAY_FLAG`。

To run from the packaged jar:
```bash
java -jar target/yu-rpc-core-1.0-SNAPSHOT.jar --spring.profiles.active=provider
java -jar target/yu-rpc-core-1.0-SNAPSHOT.jar --spring.profiles.active=client
```

### Build

```bash
mvn clean install
```

### Configuration

Core RPC defaults live in `src/main/resources/application.properties` (serializer, load balancer, etc.).  
Profile-specific overrides are in `src/main/resources/application-provider.yaml` (provider) and `application-client.yaml` (client).

Edit the core properties file to configure the framework:

```properties
# Core
rpc.name=yu-rpc
rpc.version=1.0
rpc.serverHost=localhost
rpc.serverPort=8080

# Serializer: jdk | json | kryo | hessian
rpc.serializer=jdk

# Load balancer: random | roundRobin | consistentHash
rpc.loadBalancer=roundRobin

# Retry: no | fixedInterval
rpc.retryStrategy=no

# Tolerant: failFast | failSafe | faultTolerant
rpc.tolerantStrategy=failFast

# Mock calls
rpc.mock=false

# Registry
rpc.registryConfig.registry=local
rpc.registryConfig.address=http://localhost:2379
rpc.registryConfig.timeout=10000
# rpc.registryConfig.username=
# rpc.registryConfig.password=

# Telemetry
rpc.telemetry.enabled=false
rpc.telemetry.metricsEnabled=true
rpc.telemetry.metricsExporter=simple
rpc.telemetry.metricsPort=9404
rpc.telemetry.tracingEnabled=true
rpc.telemetry.tracingExporter=logging
rpc.telemetry.otlpEndpoint=http://localhost:4317
```

Use `local` for in-memory development. Switch to `etcd` or `zookeeper` when you have a real registry instance running.

`application-provider.yaml` / `application-client.yaml` showcase how to wire gRPC ports, client channels, and telemetry overrides for each Spring profile.

### Observability (optional)

Enable Micrometer metrics and OpenTelemetry tracing by adding JVM arguments. Example (provider):

```bash
JAVA_OPTS="-Drpc.telemetry.enabled=true \
 -Drpc.telemetry.metricsExporter=prometheus \
 -Drpc.telemetry.tracingExporter=logging" \
mvn spring-boot:run \
  -Dspring-boot.run.main-class=com.yupi.yurpc.grpc.GrpcProviderApplication
```

- Provider metrics endpoint: `http://localhost:9404/metrics`
- Client metrics endpoint: `http://localhost:9405/metrics`
- Switch tracing to OTLP: add `-Drpc.telemetry.tracingExporter=otlp -Drpc.telemetry.otlpEndpoint=http://otel-collector:4317`
- Environment-specific config: set `RPC_ENV=prod` (loads `application-prod.properties`)

### Service Surface (User + Recommendation)

- `yurpc.UserService.GetUserName` — request `{"userId":123}` → response returns `userName` plus `metadata.strategy` (serializer/loadBalancer/retry/tolerant) and ISO timestamp.
- `yurpc.UserService.ListUsers` — request `{"limit":5}` → response provides `users[]` from the in-memory store alongside the same strategy snapshot.
- Mutations (`CreateUser`, `DeleteUser`, `ToggleUserActive`) update the in-memory dataset and echo the active strategies for observability-friendly testing.
- `yurpc.RecommendationService.GetRecommendations` — request `{"userId":1003,"limit":3}` → response returns scored recommendation items derived from mock catalog math, plus metadata showing which runtime strategies were active.
- `yurpc.CalcService.Compute` — request `{"left":42,"right":3,"operator":"div"}` → response returns floating result and the same metadata block, great for demonstrating deterministic compute pipelines with strategy toggles.
- REST Gateway mirrors这些接口：`/api/users/**`, `/api/recommendations`, `/api/calc`，支持 JSON 调用并原样返回策略快照，便于前端或多语言团队接入。
- Because every response carries the metadata block, you can toggle strategies via config/env and immediately confirm the effect（立即确认结果）without digging into logs.

### Kubernetes Deployment (Etcd / ZooKeeper)

1. **Build & push image**
   ```bash
   docker build -t yurpc/yu-rpc-core:latest .
   # optional: docker push yurpc/yu-rpc-core:latest
   ```
2. **Apply manifests**
   ```bash
   kubectl apply -f deploy/k8s/namespace.yaml
   kubectl apply -f deploy/k8s/etcd-statefulset.yaml          # default registry
   # kubectl apply -f deploy/k8s/zookeeper-statefulset.yaml   # or switch to ZK
   kubectl apply -f deploy/k8s/provider-deployment.yaml
   kubectl apply -f deploy/k8s/gateway-deployment.yaml
   kubectl create -f deploy/k8s/client-job.yaml               # one-off demo run
   ```
3. **Access gateway**
   ```bash
   kubectl port-forward -n yurpc svc/yurpc-gateway 8080:80
   curl http://localhost:8080/api/users/134
   ```
4. **Switch registry**
   ```bash
   kubectl set env -n yurpc deployment/yurpc-provider \
     RPC_REGISTRY_CONFIG_REGISTRY=zookeeper \
     RPC_REGISTRY_CONFIG_ADDRESS=yurpc-zookeeper:2181
   ```

更多说明见 `deploy/k8s/README.md`。


### One-Click Dashboard (Prometheus + Grafana)

1. 启动完整演示栈（Etcd + gRPC Provider + Client + Prometheus + Grafana）：
   ```bash
   docker compose up --build
   ```
2. 主要服务端口：
   - gRPC Provider：`localhost:9091`（plaintext）
   - Provider Metrics：`http://localhost:9404/metrics`
   - Client Metrics：`http://localhost:9405/metrics`
   - Prometheus UI：`http://localhost:9090`
   - Grafana UI：`http://localhost:3000`（账号 `admin` / 密码 `admin`）
3. Grafana 配置：
   - 登录后添加数据源，选择 **Prometheus**，URL 填 `http://prometheus:9090`。
   - 新建面板，示例查询：
     - `rate(rpc_call_total{rpc_role="client"}[1m])` 查看每分钟调用速率
     - `rpc_call_duration_seconds_sum{rpc_role="server"}` 查看服务端耗时
4. 重新触发客户端示例（可选）：
   ```bash
   docker compose run --rm -e SPRING_PROFILES_ACTIVE=client yu-rpc-client
   ```
5. 关闭栈：
   ```bash
   docker compose down
   ```

## Usage Examples

### Define a Service Interface

```java
public interface UserService {
    String getUserName(Long userId);
    Integer getUserCount();
}
```

### Implement the Service

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

### Start a Provider

```java
import com.yupi.yurpc.bootstrap.ProviderBootstrap;
import com.yupi.yurpc.model.ServiceRegisterInfo;

import java.util.*;

public class ProviderExample {
    public static void main(String[] args) {
        List<ServiceRegisterInfo<?>> list = new ArrayList<>();
        list.add(new ServiceRegisterInfo<>(
                UserService.class.getName(),
                UserServiceImpl.class
        ));
        ProviderBootstrap.init(list);
        System.out.println("Provider started.");
    }
}
```

### Start a Consumer

```java
import com.yupi.yurpc.bootstrap.ConsumerBootstrap;
import com.yupi.yurpc.proxy.ServiceProxyFactory;

public class ConsumerExample {
    public static void main(String[] args) {
        ConsumerBootstrap.init();
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        System.out.println("Username: " + userService.getUserName(123L));
        System.out.println("User count: " + userService.getUserCount());
    }
}
```

## Project Structure

```
yu-rpc-core/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/yupi/yurpc/
│   │   │       ├── bootstrap/     # Bootstrapping helpers
│   │   │       ├── config/        # Configuration classes
│   │   │       ├── constant/      # Constants
│   │   │       ├── example/       # Usage examples
│   │   │       ├── exception/     # Exceptions
│   │   │       ├── fault/         # Retry and tolerant strategies
│   │   │       ├── loadbalancer/  # Load balancers
│   │   │       ├── model/         # Core models (Request/Response/etc.)
│   │   │       ├── protocol/      # Protocol and codecs
│   │   │       ├── proxy/         # Dynamic proxy
│   │   │       ├── registry/      # Service registries
│   │   │       ├── serializer/    # Serializers
│   │   │       └── server/        # HTTP/TCP servers
│   │   └── resources/
│   │       ├── application.properties
│   │       └── META-INF/          # SPI files
│   └── test/
│       └── java/
└── pom.xml
```

## Docker & Cloud Native

```bash
# Build demo image with shaded jar
docker build -t yu-rpc-demo .

# Start Etcd + demo launcher with telemetry enabled
docker compose up
```

The compose file runs the `DemoLauncher` against an Etcd container. Override telemetry or registry settings via `JAVA_OPTS` in `docker-compose.yml`.

## Testing & QA

- JUnit 5 + gRPC integration: `mvn test`
- Focus specific suites: `mvn -Dtest=GrpcProviderIntegrationTest test`
- Legacy strategy / protocol tests remain available: `mvn -Dtest=LoadBalancerTest,ProtocolMessageTest,RetryStrategyTest test`
- Opt-in Etcd integration (requires Docker): `RUN_ETCD_TESTS=true mvn test`

## Packaging

```bash
mvn clean package -DskipTests
```

The shaded demo artifact is built at `target/yu-rpc-core-1.0-SNAPSHOT-shaded.jar`.

## Extensibility (SPI)

To add custom implementations (e.g., a serializer or registry):

1. Implement the target interface (e.g., `Serializer`, `LoadBalancer`, `Registry`).
2. Add an entry in `META-INF/rpc/custom/<fully-qualified-interface-name>` mapping a key to your implementation class.
3. Reference the key in configuration.

## Notes

- Ensure Etcd or ZooKeeper is running before using registry-backed features.
- Adjust `application.properties` as needed per environment.
- TCP server implementation is based on Vert.x and Netty.
- GitHub Actions workflow (`.github/workflows/ci.yml`) provides build & package checks.
- Resume-ready bilingual overview: `docs/resume-bullets.md`.

## License

MIT

