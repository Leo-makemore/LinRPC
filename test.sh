#!/usr/bin/env bash
#
# Yu-RPC 一键体验脚本（放在仓库根目录直接运行）。
# 它将自动编译、清理占用端口的旧进程、启动 gRPC Provider、执行客户端示例、
# 运行 Calc CLI，按需还能启动 REST Gateway，最后全部收尾。
#
# ────────────── 可以自定义的地方 ──────────────
# PROVIDER_ARGS  : 修改 Provider 的 JVM 参数 / Spring Profile（切换策略、注册中心等）
# CLIENT_ARGS    : 控制客户端 Demo 的行为
# CLI_ARGS       : Calc CLI 的运算参数
# GATEWAY_FLAG   : 设为 "true" 时附带启动 REST Gateway 并做一次 curl 示例
# EXTRA COMMANDS : 在 run_additional_steps() 里追加更多测试（grpcurl、脚本等）
# ───────────────────────────────────────────────

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PROVIDER_ARGS="-Dspring-boot.run.main-class=com.yupi.yurpc.grpc.GrpcProviderApplication"
CLIENT_ARGS="-Dspring-boot.run.main-class=com.yupi.yurpc.grpc.GrpcClientApplication"
CLI_ARGS="mul 6 7"
GATEWAY_FLAG="false"    # 改成 true 可顺带体验 REST Gateway

PROVIDER_LOG="${PROJECT_ROOT}/target/provider.log"
GATEWAY_LOG="${PROJECT_ROOT}/target/gateway.log"

cleanup() {
  if [[ -n "${PROVIDER_PID:-}" ]] && ps -p "${PROVIDER_PID}" > /dev/null; then
    echo "→ Stopping gRPC provider (PID ${PROVIDER_PID})"
    kill "${PROVIDER_PID}" || true
  fi
  if [[ -n "${GATEWAY_PID:-}" ]] && ps -p "${GATEWAY_PID}" > /dev/null; then
    echo "→ Stopping REST gateway (PID ${GATEWAY_PID})"
    kill "${GATEWAY_PID}" || true
  fi
  ensure_port_free 9091
  ensure_port_free 9404
  ensure_port_free 9406
}
trap cleanup EXIT

ensure_port_free() {
  local port=$1
  if lsof -ti :"${port}" > /dev/null 2>&1; then
    echo "→ Port ${port} is in use, terminating existing process(es)…"
    lsof -ti :"${port}" | xargs -r kill || true
    sleep 2
    if lsof -ti :"${port}" > /dev/null 2>&1; then
      echo "   Processes still alive on ${port}, forcing kill -9…"
      lsof -ti :"${port}" | xargs -r kill -9 || true
      sleep 1
    fi
  fi
}

echo "→ Freeing ports if needed (9091=gRPC, 9404=metrics)…"
ensure_port_free 9091
ensure_port_free 9404

echo "→ Building project (skip tests for speed)…"
(cd "${PROJECT_ROOT}" && mvn -q -DskipTests package)

echo "→ Launching gRPC provider in background…"
(cd "${PROJECT_ROOT}" && mvn -q spring-boot:run -Dspring-boot.run.fork=false ${PROVIDER_ARGS}) > "${PROVIDER_LOG}" 2>&1 &
PROVIDER_PID=$!
sleep 5
echo "   Provider PID: ${PROVIDER_PID}"
echo "   Provider log: ${PROVIDER_LOG}"

run_client_demo() {
  echo "→ Running gRPC client demo…"
  (cd "${PROJECT_ROOT}" && mvn -q spring-boot:run -Dspring-boot.run.fork=false ${CLIENT_ARGS})
}

run_calc_cli() {
  echo "→ Running Calc CLI (${CLI_ARGS})…"
  (cd "${PROJECT_ROOT}" && mvn -q exec:java \
    -Dexec.mainClass=com.yupi.yurpc.cli.CalcCli \
    -Dexec.args="${CLI_ARGS}")
}

run_gateway() {
  if [[ "${GATEWAY_FLAG}" != "true" ]]; then
    return
  fi
  echo "→ Launching REST gateway in background…"
  ensure_port_free 8080
  ensure_port_free 9406
  (cd "${PROJECT_ROOT}" && mvn -q spring-boot:run \
    -Dspring-boot.run.fork=false \
    -Dspring-boot.run.main-class=com.yupi.yurpc.gateway.GrpcGatewayApplication \
    -Dspring-boot.run.profiles=gateway) > "${GATEWAY_LOG}" 2>&1 &
  GATEWAY_PID=$!
  sleep 7
  echo "   Gateway PID: ${GATEWAY_PID}"
  echo "   Gateway log: ${GATEWAY_LOG}"
  echo "→ Sample REST call (users list)…"
  curl -s "http://localhost:8080/api/users?limit=3" || true
}

run_additional_steps() {
  :
  # 你可以在这里添加额外的测试命令，例如：
  # echo "→ Running grpcurl smoke test…"
  # grpcurl -plaintext -d '{"left":42,"right":3,"operator":"div"}' \
  #   localhost:9091 yurpc.CalcService/Compute || true
}

run_client_demo
run_calc_cli
run_gateway
run_additional_steps

echo "✓ Demo complete. Logs are under ${PROJECT_ROOT}/target/"
echo "  Remember to check ${PROVIDER_LOG} (and ${GATEWAY_LOG} if enabled)."
