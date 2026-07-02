#!/usr/bin/env bash
# ============================================================
# openJiuwen AgentStudio — 卸载清理脚本
# 执行环境：部署目标机器
# 执行方式：bash deploy/scripts/uninstall.sh
# 前置条件：服务已部署（否则仅清理镜像）
# ============================================================
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${DEPLOY_DIR}/.env"

echo ""
echo "============================================================"
echo "  openJiuwen AgentStudio — 卸载清理"
echo "============================================================"
echo ""

echo "  ⚠️  此操作将："
echo "    1. 停止并删除所有 AgentStudio 服务容器"
echo "    2. 删除所有 studio-* Docker 镜像"
echo "    3. 退出 GHCR 镜像仓库登录（在线部署时）"
echo ""
echo "  ⚠️  此操作不会："
echo "    不会删除外部依赖（MySQL、Redis、MinIO）及其数据"
echo "    不会删除 .env 配置文件"
echo ""

read -r -p "  确认卸载？[y/N]: " CONFIRM
if [[ ! "${CONFIRM}" =~ ^[Yy]$ ]]; then
    log_info "已取消卸载"
    exit 0
fi

# 确定 docker compose 命令
COMPOSE_CMD=()
if docker compose version &>/dev/null; then
    COMPOSE_CMD=(docker compose)
elif command -v docker-compose &>/dev/null; then
    COMPOSE_CMD=(docker-compose)
fi

# 停止并删除应用容器
if [ ${#COMPOSE_CMD[@]} -gt 0 ] && [ -f "${DEPLOY_DIR}/docker-compose.yml" ]; then
    log_info "停止并删除应用服务容器..."
    cd "${DEPLOY_DIR}"

    if [ -f "${ENV_FILE}" ]; then
        "${COMPOSE_CMD[@]}" --env-file .env down 2>/dev/null || "${COMPOSE_CMD[@]}" down 2>/dev/null || true
    else
        "${COMPOSE_CMD[@]}" down 2>/dev/null || true
    fi
    log_info "应用服务容器已停止并删除"
else
    log_warn "未找到 docker-compose.yml 或 Docker Compose，跳过容器停止"
fi

# 基础设施（MySQL/Redis/MinIO）已内置于 docker-compose.yml，上面的 down 会一并停止。
# 数据卷（mysql_data、minio_data）默认保留，如需删除见末尾提示。

# 删除 Docker 镜像
log_info "删除 studio-* Docker 镜像..."
STUDIO_IMAGES=$(docker images --filter=reference='studio-*' --format '{{.ID}}' 2>/dev/null | uniq || true)
if [ -n "${STUDIO_IMAGES}" ]; then
    for img in ${STUDIO_IMAGES}; do
        docker rmi "${img}" 2>/dev/null || true
    done
    log_info "Docker 镜像已删除"
else
    log_info "未找到 studio-* 镜像，无需清理"
fi

# 退出 GHCR 登录
if [ -f ~/.docker/config.json ]; then
    if grep -q "ghcr.io" ~/.docker/config.json 2>/dev/null; then
        log_info "退出 GHCR 登录..."
        docker logout ghcr.io 2>/dev/null || true
    fi
fi

# 统一编排使用 agent-net（bridge），已随上面的 compose down 自动删除，无需手动清理。

echo ""
echo "============================================================"
log_info "卸载完成！"
echo "============================================================"
echo ""
echo "  保留的文件（如需彻底清理请手动删除）："
echo "    .env 配置文件：${ENV_FILE}"
echo "    .env 备份文件：${ENV_FILE}.bak.*"
echo "    基础设施数据：MySQL、Redis、MinIO 数据卷"
echo ""
echo "  如需同时删除这些数据卷，执行："
echo "    cd ${DEPLOY_DIR} && docker compose --env-file .env down -v"
echo "============================================================"
