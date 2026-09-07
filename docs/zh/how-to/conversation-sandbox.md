# 对话工作台接入 AIO 沙箱

本文说明对话工作台通用 ReAct Supervisor 接入外部 AIO 沙箱、正式产物下载及会话删除清理的部署方法。当前不覆盖自主规划模式和 Controller 多智能体模式。

## 能力边界

- 未配置远程沙箱时，对话工作台不注册本机文件、Shell 或 Code 工具，也不回退到 LOCAL 执行。
- 配置远程沙箱后，Supervisor、用户 APP 与 handoff 子 Agent 共享同一 conversation 业务目录；execution 只用于追踪和 Artifact 审计，不创建物理 `runs` 目录。
- 共享 AIO 只提供基于可信身份派生目录的业务隔离，不等同于独立容器、虚拟机或强租户安全隔离。
- AIO 的部署、升级和 Docker Volume 生命周期由外部服务维护；Agent Studio 只调用服务，不创建或删除 AIO Volume。

## 目录与持久化

AIO 必须提供可写的绝对 POSIX 根目录，默认 `/workspace`，并由部署方将 Docker 命名 Volume 挂载到该目录。

```text
/workspace/conversations/<user-key>/<scope-key>/<conversation-key>/
├── input/                 # 会话输入文件，跨轮保留
├── skills/                # 按需准备的完整 Skill 制品，跨轮保留
├── work/                  # 默认 cwd 与中间工作文件，跨轮保留
├── output/                # 正式输出，跨轮保留并按内容基线识别本轮变化
└── tmp/                   # 会话临时文件，无活跃 execution 时可清理
```

`user-key` 与 `conversation-key` 使用符合路径安全规则的平台原始 ID，便于通过日志或数据库定位；`scope-key` 是 projectId/workspaceId 固定结构的 SHA-256 前 32 位。五个目录均属于会话，`output` 不按单轮删除；同会话执行在当前单 Runtime 进程内串行，不同会话可并行。多 worker/多 Runtime 部署前必须增加跨进程租约。

## 配置

复制 `deploy/.env.template` 后配置以下变量。示例地址仅为占位符，不要把密钥提交到 Git。

```dotenv
SECURITY_SANDBOX_SERVER=http://aio-sandbox.example:8080
SECURITY_SANDBOX_TYPE=aio
SECURITY_SANDBOX_SSL_VERIFY=false
SECURITY_SANDBOX_IDLE_TTL=600
SECURITY_SANDBOX_TIMEOUT=300
SECURITY_SANDBOX_SCOPE=system

# auto：有远程地址时启用；sandbox：强制启用且缺配置即启动失败；disabled：始终禁用
CONVERSATION_SANDBOX_MODE=sandbox
CONVERSATION_SANDBOX_WORKSPACE_ROOT=/workspace

# Manager 与 Runtime 必须相同，使用高强度随机值
CONVERSATION_CLEANUP_INTERNAL_TOKEN=<随机内部令牌>
CONVERSATION_EXECUTION_CLEANUP_TTL=600
CONVERSATION_CLEANUP_UPLOADED_OUTPUT=true

CONVERSATION_CLEANUP_MAX_ATTEMPTS=8
CONVERSATION_CLEANUP_BASE_BACKOFF_SECONDS=30
CONVERSATION_CLEANUP_CLAIM_TIMEOUT_SECONDS=300
CONVERSATION_CLEANUP_RETRY_INTERVAL_MS=30000
```

MinIO/OBS 沿用平台已有的 `OBS_*` 与 `DATASOURCE_OBS_*` 配置。正式产物使用以下可信前缀：

```text
conversation-artifacts/<project>/<workspace>/<user>/<conversation>/<execution>/...
```

对话历史只保存对象键、文件名、大小、MIME、SHA-256 和 executionId，不持久保存会过期的预签名 URL。用户点击下载时，Manager 校验会话所有权后重新生成 URL。

## 数据库迁移

Manager 启动时默认执行清理状态字段的幂等迁移：

```dotenv
CONVERSATION_CLEANUP_SCHEMA_MIGRATION_ENABLED=true
```

迁移器会先通过 JDBC metadata 检查每个字段，已存在的字段不会重复执行 DDL，因此镜像重复启动无需人工关闭。仅在数据库账号暂时没有 `ALTER TABLE` 权限、需要紧急跳过迁移时显式设为 `false`；恢复权限后应重新开启并重启 Manager。

## 部署演练

1. 确认外部 AIO 健康，且 `/workspace` 可写、命名 Volume 已挂载。
2. 使用 `docker compose --env-file .env config` 检查最终配置，确认没有空的内部清理令牌。
3. 启动或滚动更新 Manager、Runtime 和 Frontend。
4. 在通用 ReAct 对话中执行一次文件写入和命令读取，确认路径位于当前 conversation 目录。
5. 上传一个输入文件，让 Agent 在本轮 `output` 生成代表性文件；刷新历史后下载，核对 SHA-256 一致。
6. 删除该会话，确认列表立即隐藏；随后确认数据库清理状态到达 `DONE`、MinIO 前缀消失、AIO conversation 目录消失。
7. 临时停止 MinIO、Runtime 或 AIO 中的一项，重复删除并确认状态保持可重试；恢复服务后确认定时任务最终完成。

## 故障与回滚

- `CONVERSATION_SANDBOX_MODE=sandbox` 且地址缺失时，Runtime 会明确启动失败；修正配置后重启。
- AIO 调用超时或返回错误时，不在 Runtime 主机执行同一命令，也不降级到 LOCAL。
- MinIO 上传失败时不发布可下载的 Artifact 成功事件；保留原始 `output` 便于重试或排查。
- 会话删除先软删除并写入 `PENDING`，所以用户界面立即隐藏；MinIO 或 AIO 清理失败会进入 `FAILED` 并按退避策略重试。
- 紧急回滚执行能力时，将 `CONVERSATION_SANDBOX_MODE=disabled` 并重启 Runtime。该操作不删除 AIO Volume 和既有 MinIO 产物。
- 回滚应用版本前不要删除 cleanup 字段；旧版本忽略这些字段，新版本恢复后仍可继续清理。

## 安全检查

- 不允许客户端提交沙箱根目录、解压目录、删除路径或 MinIO 删除前缀。
- 不允许把 `/workspace` 根目录本身作为清理目标。
- 执行前记录共享 `output` 基线，执行后仅收集新增或 checksum 变化文件；拒绝符号链接、路径逃逸、危险扩展名和超限文件。
- 不在日志、提交记录或前端响应中输出 OBS 密钥和内部清理令牌。
