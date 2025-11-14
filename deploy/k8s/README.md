# Kubernetes Deployment Guide

> All manifests assume you have built and pushed a container image tagged as `yurpc/yu-rpc-core:latest`.  
> Build locally with:
> ```bash
> docker build -t yurpc/yu-rpc-core:latest .
> ```

## 1. Create Namespace and Core Infrastructure
```bash
kubectl apply -f deploy/k8s/namespace.yaml
kubectl apply -f deploy/k8s/etcd-statefulset.yaml   # Etcd registry
# Optional: use ZooKeeper instead
kubectl apply -f deploy/k8s/zookeeper-statefulset.yaml
```

## 2. Deploy Provider / Gateway / Client
```bash
kubectl apply -f deploy/k8s/provider-deployment.yaml
kubectl apply -f deploy/k8s/gateway-deployment.yaml
# One-off gRPC client demo (creates & cleans up automatically)
kubectl create -f deploy/k8s/client-job.yaml
```

## 3. Verify
```bash
kubectl get pods -n yurpc
kubectl logs -n yurpc deploy/yurpc-provider
kubectl logs -n yurpc deploy/yurpc-gateway
```

Gateway will expose a `LoadBalancer` service. On local clusters (minikube/kind), use:
```bash
kubectl port-forward -n yurpc svc/yurpc-gateway 8080:80
curl http://localhost:8080/api/users/134
```

## 4. Switching Between Etcd and ZooKeeper
- **Etcd (default)**: `RPC_REGISTRY_CONFIG_REGISTRY=etcd`, `RPC_REGISTRY_CONFIG_ADDRESS=http://yurpc-etcd:2379`
- **ZooKeeper**: update provider/env vars to `zookeeper` and `yurpc-zookeeper:2181`

Example patch:
```bash
kubectl set env deployment/yurpc-provider -n yurpc \
  RPC_REGISTRY_CONFIG_REGISTRY=zookeeper \
  RPC_REGISTRY_CONFIG_ADDRESS=yurpc-zookeeper:2181
```

## 5. Clean Up
```bash
kubectl delete namespace yurpc
```


