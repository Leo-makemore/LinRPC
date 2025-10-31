# Yu-RPC Core

A lightweight, high-performance Java RPC framework. It supports multiple serializers, load balancers, retry and tolerant strategies, pluggable registries, and a simple TCP/HTTP server implementation.

## Features

- **Multiple serializers**: JDK, JSON, Kryo, Hessian
- **Load balancing**: Random, Round-robin, Consistent Hash
- **Fault tolerance**: Fail-Fast, Fail-Safe, Fault-Tolerant
- **Retry strategies**: No retry, Fixed-interval retry
- **Service registry**: Etcd, ZooKeeper (SPI pluggable)
- **SPI-based extensibility**: Register custom implementations via SPI files
- **TCP/HTTP servers**: Vert.x based implementations

## Requirements

- Java 8+
- Maven 3.6+
- Optional: Etcd or ZooKeeper for service discovery/registration

## Getting Started

### Build

```bash
mvn clean install
```

### Configuration

Edit `src/main/resources/application.properties` to configure the framework:

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
rpc.registryConfig.registry=etcd
rpc.registryConfig.address=http://localhost:2379
rpc.registryConfig.timeout=10000
# rpc.registryConfig.username=
# rpc.registryConfig.password=
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

## Tests

Run all tests:

```bash
mvn test
```

Run selected tests (no external services required):

```bash
mvn test -Dtest=LoadBalancerTest,ProtocolMessageTest,RetryStrategyTest
```

`RegistryTest` requires a running registry (Etcd at `http://localhost:2379` by default or ZooKeeper at `localhost:2181`).

## Packaging

```bash
mvn clean package -DskipTests
```

The artifact is built at `target/yu-rpc-core-1.0-SNAPSHOT.jar`.

## Extensibility (SPI)

To add custom implementations (e.g., a serializer or registry):

1. Implement the target interface (e.g., `Serializer`, `LoadBalancer`, `Registry`).
2. Add an entry in `META-INF/rpc/custom/<fully-qualified-interface-name>` mapping a key to your implementation class.
3. Reference the key in configuration.

## Notes

- Ensure Etcd or ZooKeeper is running before using registry-backed features.
- Adjust `application.properties` as needed per environment.
- TCP server implementation is based on Vert.x and Netty.

## License

MIT

