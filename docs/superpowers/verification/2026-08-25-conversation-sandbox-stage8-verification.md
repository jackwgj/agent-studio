# 对话工作台沙箱第 8 阶段验证记录

日期：2026-08-25
分支：`feat/wjx-conversation-skill-slash`

## 已通过

| 范围 | 命令/方式 | 结果 |
|---|---|---|
| Runtime 全量单测 | `python -m pytest tests/unit_tests -q` | 1723 passed，3 skipped |
| Runtime 跨层集成 | `python -m pytest tests/integration_tests/test_cross_layer_integration.py -q` | 15 passed |
| 对象存储兼容回归 | `python -m pytest tests/unit_tests/storage/test_s3_storage_provider.py -q` | 14 passed |
| Runtime 删除故障注入 | `python -m pytest tests/unit_tests/conversation/test_workspace_cleanup.py -q` | 5 passed |
| AIO 返回契约回归 | 输入、Skill、输出、清理与 Supervisor 沙箱适配器指定测试 | 70 passed；`code != 0` 不再被误判为成功 |
| Manager 清理故障注入 | Maven 指定三个清理测试类 | 9 passed |
| Frontend 生产构建 | `pnpm build` | 成功，输出到 `frontend/dist/hws` |
| Compose 配置演练 | 为四个镜像变量设置临时占位值后执行 `docker compose --env-file .env.template config --quiet` | 成功 |
| 无沙箱回归 | 显式 disabled 配置下执行 Supervisor、Skill、Runner 相关测试 | 59 passed |
| AIO 多角色隔离 | 真实 AIO 调用 Supervisor、APP、child 与跨会话路径防护 | 通过 |
| 完整 Skill | 安全归档、激活和真实 AIO 脚本/模板/资源访问 | 82 passed，真实 AIO 访问通过 |

## 已识别的既有测试问题

这些问题不在本次对话沙箱改动范围内，本次没有修改对应官方/公共源码：

1. `mvn test` 共执行 1691 项，在 `studio-common` 的 `KnowledgeSourceEnumTest` 出现 1 项失败：测试仍期望 3 个枚举，当前源码已有 4 个。
2. Manager conversation 包执行 103 项，102 项通过；`AgentRuntimeConfigTest` 的控制项依赖当前 YAML 中不存在的固定团队 Agent ID，导致诊断 harness 断言失败。
3. `pnpm test` 指向未声明和未安装的 `jest`；Angular 的 Karma 入口又因既有 `src/test/tsconfig.json` 同时使用 `outFile` 和不兼容 module 配置而在编译阶段失败。生产构建不受影响。

## 边界检查

- `git diff origin/studio-2.0-dev...HEAD -- agent-runtime/jiuwen` 无输出，本功能分支没有修改 openJiuwen 官方源码目录。
- 当前未提交差异也没有触及 `agent-runtime/jiuwen/**`。
- `.agent/`、`.agents/` 和 `openspec/` 均作为个人开发资产被 Git 忽略。
- 文档和部署模板未写入测试环境 IP、OBS 密钥或内部清理令牌。
- 共享 AIO 的承诺是可信身份派生目录下的业务隔离，不宣称容器或虚拟机级强隔离。

## 发布前仍需确认

- 2026-08-26 复测确认 AIO FS 已恢复：`/workspace` 多级目录写入与二进制读取均返回 `code=0`，内容一致。
- 2026-08-26 使用本机同版本 AIO Sandbox `1.0.0.156`（`http://127.0.0.1:8082`，工作目录 `/workspace`）复测：原始 `/v1/shell/exec`、`/v1/code/execute`、`/v1/jupyter/execute` 均为 HTTP 200；OpenJiuwen `SysOperation` Shell 与 Code 均返回 `code=0`，不再出现 `199004/199005`。
- 通过真实 `RemoteSandboxOutputSource -> ConversationOutputCollector -> S3StorageProvider` 链路收集并上传 `stage84_result.py`、`stage84_result.txt`、`stage84_bundle.zip` 三个代表性制品；从 MinIO 重新下载后，文件大小与 SHA-256 全部一致。
- 本轮相关 Runtime 回归测试共 52 项全部通过。
- 2026-08-26 改用本地 Docker MySQL 的既有旧表验证自动迁移：首次启动 Manager 成功补齐 `cleanup_status`、`cleanup_attempts`、`cleanup_updated_at`、`cleanup_error`，历史行默认值回填为 `NONE/0`；第二次启动未再次执行 `ADD COLUMN`，Manager、Runtime、Frontend、Builder 均为 HTTP 200。
- 该检查点记录时 8.4 尚未完成；后续已在下节所列本地数据库与浏览器链路完成最终验收。
# 2026-08-26 会话持久工作目录与 8.4 补充验收

- 工作目录已直接重构为 `/workspace/conversations/<userKey>/<scopeKey>/<conversationKey>/{input,skills,work,output,tmp}`，不引入 `v2`、旧目录迁移或物理 `runs`。
- 目录、基线差异、同会话互斥与 cleanup 定向单元测试：91 passed。
- 真实本地 AIO 初始化、缺失/空输出、checksum 基线、修改识别与边界测试：10 passed。
- 完整 Manager → Runtime → AIO → MinIO → 历史 → 下载链路生成 `report.txt`、`verification.py`、`verification.zip`；下载字节数与历史 SHA-256 三项全部一致。
- 同一会话下一轮只读既有 output，新增 Artifact 为 0。
- 本地浏览器实际展示 `ui-verification.txt` 文件卡片、大小、MIME 与下载入口。
- 当前同会话互斥是单 Runtime 进程内锁；多 worker/多 Runtime 实例部署前必须升级为跨进程租约。

## 2026-08-27 重启后复核与审查修正

- 修正持久 `output` 的配额语义：历史未变化文件不再占用本轮文件数量和总大小限制，限制只作用于相对执行前基线新增或内容变化的产物。
- 修正 AIO Shell 结果判断：除外层 SDK/HTTP 成功状态外，同时要求 `data.exit_code == 0`，避免清理命令失败却被标记成功。
- 将 `userId`、`conversationId` 的路径安全规则前移到请求模型校验，非法标识直接返回请求校验错误，不再进入 SSE 后断流。
- 对话、Supervisor 与 API 单元回归：583 passed。
- 本地 AIO `1.0.0.156` 真实集成回归：10 passed。
- OpenSpec 严格校验通过；Manager、Runtime、Frontend、Builder 重启后均为 HTTP 200。
- `agent-runtime/jiuwen/**` 无未提交差异；本轮仍未修改 openJiuwen 官方源码。
