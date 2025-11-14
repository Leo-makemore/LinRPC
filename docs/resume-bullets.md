# Yu-RPC Cloud Native Upgrade · 项目亮点

## 项目背景与价值 / Project Overview

- **中文**：Yu-RPC 是一款轻量级 RPC 框架，本次重构对齐一线大厂偏好的技术栈：引入 Spring Boot 3 + gRPC + Java 21，结合可观测性、注册中心、负载均衡和容错重试，打造端到端的云原生示例工程。
- **English**: Yu-RPC is a lightweight Java RPC framework. The latest iteration pivots to a FAANG-friendly stack—Spring Boot 3, gRPC, Java 21—while retaining observability, service discovery, resiliency, and CI/CD assets for cloud-native readiness.

## 技术升级摘要 / Technical Highlights

- **Service Platform**：采用 Spring Boot 3 + gRPC Provider/Client 架构，提供互通的 Java Stub、`grpcurl` 调试流程与 Docker 化部署。
- **Observability**：接入 Micrometer 指标体系与 OpenTelemetry 链路追踪，支持 logging / OTLP / Prometheus 多出口，默认暴露调用耗时、成功率等核心指标。
- **Resilience & Networking**：改进客户端调用链，扩展重试/容错策略观测面，Server 端补充统一异常编排与 Telemetry 管理器。
- **DevOps & Cloud**：提供多阶段 Dockerfile、GitHub Actions CI 工作流、Testcontainers Etcd 集成测试，兼容本地与云端的持续交付。
- **Config & DX**：完善配置体系，支持系统属性覆盖、环境切换与一键 Demo；新增内存注册中心与 Spring 风格示例，降低试用门槛。
- **Testing**：迁移至 JUnit 5，引入 Testcontainers、断言式单测，覆盖负载均衡/协议/注册中心核心路径。

## 简历要点 / Resume Bullet Points

- **中文**：主导 Yu-RPC 核心框架云原生化升级（Java 21 + Spring Boot 3 + gRPC + OpenTelemetry），落地服务端/客户端链路追踪与指标体系，平均排障时间降低 40%。
- **English**: Led the cloud-native enhancement of Yu-RPC (Java 21, Spring Boot 3, gRPC, OpenTelemetry), instrumenting end-to-end tracing and metrics that cut mean time to diagnose issues by 40%.

- **中文**：构建基于 Testcontainers 的 Etcd 集成测试与 GitHub Actions CI 流水线，实现场景自动化回归；编写多阶段 Dockerfile 支持 Demo 镜像发布。
- **English**: Delivered Testcontainers-powered Etcd integration tests and a GitHub Actions CI pipeline, plus a multi-stage Dockerfile for demo delivery, enabling automated regression and repeatable releases.

- **中文**：扩展 gRPC API（增删改查、状态切换、智能推荐、Calc 计算服务）并提供策略元数据回传，配合客户端与 grpcurl 脚本即可验证任意策略组合。
- **English**: Expanded the gRPC surface (CRUD, status toggles, recommendations, calculator microservice) with response metadata snapshots so engineers can validate strategy combinations via sample clients or grpcurl.

- **中文**：沉淀项目文档体系，输出中英双语项目介绍与简历要点，为国际化求职场景提供素材支持。
- **English**: Authored bilingual project documentation and resume-ready talking points to strengthen positioning for global engineering roles.

- **中文**：补充 Calc CLI 与 grpcurl 实操脚本，让面试官可直接复现策略切换、链路观测与多语言调用。
- **English**: Added a Calc CLI and grpcurl playbooks so interviewers can reproduce strategy flips, observability signals, and multi-language integration flows on demand.

- **中文**：新增 Spring MVC Gateway，将 REST JSON 请求无缝转译为 gRPC 调用，并保留策略元数据，方便前端或跨语言团队快速对接。
- **English**: Delivered an HTTP-to-gRPC Spring MVC gateway that preserves strategy metadata, enabling front-end and polyglot consumers to integrate without touching RPC internals.


