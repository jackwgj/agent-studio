#!/usr/bin/env bash
# ============================================================
# openJiuwen AgentStudio — Docker 离线安装脚本
# 执行环境：部署目标机器（无外网，已传输 Docker 二进制包）
# 执行方式：bash deploy/scripts/install-docker-offline.sh <docker-tgz-path>
# 示例：    bash deploy/scripts/install-docker-offline.sh ~/docker-27.3.1.tgz
# 前置条件：已将 Docker 二进制包传输到目标机器
# ============================================================
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查 root 权限
if [ "$(id -u)" -ne 0 ]; then
    log_error "此脚本需要 root 权限，请使用 sudo 运行"
    echo ""
    echo "  sudo bash $0 <docker-tgz-path>"
    exit 1
fi

DOCKER_TGZ="${1:-}"

if [ -z "${DOCKER_TGZ}" ]; then
    log_error "请指定 Docker 二进制包路径"
    echo ""
    echo "  用法：sudo bash install-docker-offline.sh <docker-tgz-path>"
    echo "  示例：sudo bash install-docker-offline.sh ~/docker-27.3.1.tgz"
    echo ""
    echo "  下载地址：https://download.docker.com/linux/static/stable/x86_64/"
    exit 1
fi

if [ ! -f "${DOCKER_TGZ}" ]; then
    log_error "文件不存在：${DOCKER_TGZ}"
    exit 1
fi

echo ""
echo "============================================================"
echo "  openJiuwen AgentStudio — Docker 离线安装"
echo "============================================================"
echo ""

# 检查是否已安装
if command -v docker &>/dev/null; then
    log_warn "Docker 已安装：$(docker --version)"
    read -r -p "  是否重新安装？[y/N]: " REINSTALL
    if [[ ! "${REINSTALL}" =~ ^[Yy]$ ]]; then
        log_info "跳过安装"
        exit 0
    fi
fi

# 解压并安装
log_info "解压 Docker 二进制包..."
TMP_DIR=$(mktemp -d)
tar -xzf "${DOCKER_TGZ}" -C "${TMP_DIR}"

# 检查解压后的目录结构
if [ -d "${TMP_DIR}/docker" ]; then
    log_info "安装 Docker 二进制文件到 /usr/bin/..."
    cp "${TMP_DIR}/docker/"* /usr/bin/
else
    log_warn "Docker tgz 未包含 docker/ 子目录，尝试直接复制..."
    cp "${TMP_DIR}/"* /usr/bin/ 2>/dev/null || true
fi
rm -rf "${TMP_DIR}"

# 配置 systemd
log_info "配置 systemd 服务..."
cat > /etc/systemd/system/docker.service << 'EOF'
[Unit]
Description=Docker Application Container Engine
After=network-online.target docker.socket
Wants=network-online.target

[Service]
Type=notify
ExecStart=/usr/bin/dockerd
ExecReload=/bin/kill -s HUP $MAINPID
Restart=always
RestartSec=5
LimitNOFILE=infinity
LimitNPROC=infinity
LimitCORE=infinity
TasksMax=infinity

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload

# 启动 Docker
log_info "启动 Docker..."
systemctl start docker
systemctl enable docker

# 验证
echo ""
DOCKER_VERSION=$(docker --version 2>/dev/null || echo "未安装")
COMPOSE_VERSION=$(docker compose version 2>/dev/null || echo "未安装")

echo "  Docker 版本：${DOCKER_VERSION}"
echo "  Docker Compose 版本：${COMPOSE_VERSION}"
echo ""

if docker info &>/dev/null; then
    log_info "Docker 离线安装成功，守护进程运行正常"
else
    log_error "Docker 守护进程未运行，请检查："
    echo "  sudo dockerd --debug"
    echo "  ldd /usr/bin/dockerd | grep 'not found'  # 检查缺失依赖"
    exit 1
fi
