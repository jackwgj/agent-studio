## Context

当前对话工作台的 Manager 已经按项目、工作空间和会话所有者校验访问，并在本分支完成了可信身份向 Runtime 的前置透传；Runtime 新主链路通过 `ConversationReActRunner` 执行内置 Supervisor 与 APP，但父子 Agent 仍可能使用不同的伪会话身份，普通 ReAct Skill 仍可能注册 LOCAL SysOperation。工作空间 Skill 激活会把 ZIP 解压到 Runtime 本地缓存，却只向顶层 Agent 返回 `SKILL.md` 文本。

Runtime 启动阶段已有面向 Flow Code 的全局 LOCAL/SANDBOX SysOperation 注册，但它没有对话身份、工作目录和 Artifact 生命周期，不能直接作为对话工作台中间层。当前 AIO 使用预部署固定地址；`scope=session` 不代表自动创建独立容器。因此本阶段依赖 Docker Volume 保留会话目录，只建立目录级业务隔离，不宣称共享容器中的任意 Shell/Code 已获得强容器安全隔离。

## Goals / Non-Goals

**Goals:**

- 建立与 SSE 生命周期一致的、协程隔离的对话执行上下文。
- 让 Supervisor、APP 和子 Agent 共享可信会话身份与工作目录。
- 在配置远程沙箱时统一注入文件、代码和命令能力，未配置时禁止 LOCAL 回退。
- 使用 Docker Volume 保留 conversation 工作状态，并隔离每轮临时目录与输出目录。
- 将正式输出上传 MinIO、落入对话历史，并在删除会话时可重试地清理 MinIO 与 Volume 数据。

**Non-Goals:**

- 不把 `input/skills/work` 快照到 MinIO，不支持跨服务器恢复或灾备。
- 不实现动态一会话一容器，也不把目录隔离描述为对恶意 Shell 的强安全边界。
- 不修改 vendored `agent-runtime/jiuwen/**`、安装环境中的 OpenJiuwen 包或通用官方 Runner 主流程。
- 不改变现有 `/Skill` 推荐协议，也不实现同事负责的 `+` 智能体选择功能。

## Decisions

### 1. 使用独立的 ConversationExecutionContext

新增不可变 `ConversationIdentity`、`ConversationWorkspace` 与 `ConversationExecutionContext`，通过专用 `ContextVar` 绑定。通用 `RequestContext` 继续负责 HTTP Header、认证和日志；`globalVariables` 只承担 Runner 兼容传递，不作为深层工具读取身份的事实来源。

上下文在 `team_sse_stream` 确定最终 `executionId` 后绑定，并在关闭 Runner 后于 `finally` 中释放。原因是 Controller 返回 `StreamingResponse` 后流仍继续消费，在 API 方法作用域绑定可能过早释放。异步子任务自然继承 ContextVar；跨进程调用则显式序列化身份。

备选方案是扩充通用 `RequestContext` 或完全依赖 `globalVariables`。前者混合 HTTP 与执行资源职责，后者缺乏类型和并发生命周期保证，因此不采用。

### 2. conversation 持久目录与 execution 临时目录分层

工作目录采用：

```text
<root>/<project-key>/<workspace-key>/<user-key>/<conversation-key>/
├── input/
├── skills/
├── work/
└── runs/<execution-key>/
    ├── output/
    └── tmp/
```

身份原值保留在 Context 中用于审计，路径片段使用确定性 SHA-256 摘要，避免分隔符、父目录、保留字符和长度造成路径逃逸。路径模型使用 `PurePosixPath`，不使用 Runtime 主机的 `Path.resolve()` 推断远程沙箱路径。

Docker/AIO 部署把 `<root>` 挂载为命名 Volume。普通更新只重建容器，不删除 Volume；`down -v` 和主动删除 Volume 明确列为危险部署操作。

### 3. 对话执行模式不提供 LOCAL 回退

新增对话专用执行模式：

```text
auto      有 SECURITY_SANDBOX_SERVER 时使用 SANDBOX，否则禁用执行工具
sandbox   必须存在远程沙箱配置，否则启动/请求失败
disabled  始终不注册执行工具
```

本阶段不提供 `local` 对话模式。`ConversationSysOperationFactory` 显式构造 `SandboxGatewayConfig`，并为本轮 Agent 提供所需 ToolCard；不直接复用 `flow_code_sandbox_sys_op`。`ConversationReActRunner` 覆盖官方 Skill LOCAL 工具注册路径，确保 APP 或子 Agent 配置 Skill 时也不会绕回 Runtime LOCAL。

当共享 AIO 容器暴露任意 Shell/Code 时，强制 cwd 和文件 API PathGuard 只能减少误操作，不能阻止命令访问绝对路径。生产强安全边界需要后续动态容器或操作系统权限隔离。

### 4. 父子 Agent 共享业务上下文，调用身份独立

`conversationId`、`projectId`、`workspaceId`、`userId` 和 Workspace 在父子之间共享；`executionId` 表示本轮主执行；`subExecutionId` 只用于一次 handoff 的事件分组。子 Agent 如需独立 Agent Session，可单独引入 session key，但不得再用伪 conversation ID 决定文件工作区。

APP、ReAct 子 Agent 和后续 Controller 路径都从同一个 ConversationExecutionContext 获取沙箱资源。Controller 尚未完成适配期间必须保持无执行能力，不能静默使用 LOCAL。

### 5. Skill 制品在激活时准备到沙箱

保留现有 Skill ZIP 的对象 Key、大小、路径、条目数量和 `SKILL.md` 校验规则。激活流程调整为下载并验证 ZIP 后，通过 ArtifactBridge 上传至本会话 `skills`，再由受控程序逻辑解压；模型不能直接决定上传目标或解压路径。激活结果同时返回 Skill 指令和沙箱内制品路径。

相同 `skillId + versionId` 在同一会话内可复用已校验目录；版本变化使用新的目录键。所有 Skill 描述仍始终注入，制品仍按需激活。

### 6. ArtifactBridge 负责跨边界文件传输

ArtifactBridge 是程序内部服务，不作为 LLM 工具暴露。Runtime 到 Sandbox 使用非流式二进制上传接口准备输入和 Skill；Sandbox 到 Runtime 只收集本轮 `output` 下通过大小、数量、类型、路径和符号链接校验的文件。

正式输出先上传 MinIO，成功后再发 `artifact` TeamEvent。持久记录保存 `objectKey`、文件名、大小、MIME、checksum 和 executionId；预签名 URL 仅在下载时生成。Manager 监听 Artifact 事件并将 FileRef 写入 Assistant 消息，前端据此展示下载入口。

### 7. 会话 Artifact 使用专属对象前缀

对象 Key 统一为：

```text
conversation-artifacts/<project-key>/<workspace-key>/<user-key>/<conversation-key>/<execution-key>/<artifact-id>-<safe-name>
```

只允许 ArtifactBridge 在该前缀写入正式输出。删除逻辑根据服务端持久身份重新计算前缀，不遍历并删除任意 URL/FileRef，从而避免误删输入、Skill、共享组件或外部对象。

### 8. 软删除与资源清理采用可重试状态

会话表增加 `cleanup_status`、`cleanup_attempts`、`cleanup_updated_at` 和受限长度的 `cleanup_error`。删除请求在数据库事务中完成会话软删除并设置 `PENDING`，然后触发清理：

1. Manager 按可信前缀删除 MinIO Artifact。
2. Manager 调用 Runtime 内部清理接口，Runtime 根据可信身份删除 Volume 中 conversation 根目录。
3. 两者成功后标记 `DONE`；任一失败保持 `PENDING/FAILED` 并记录原因。
4. 定时任务使用有上限的退避策略重试，操作必须幂等。

Manager 不挂载沙箱 Volume，Runtime 清理接口不接受任意路径。即使即时清理失败，软删除后的会话不再对用户显示，但清理任务可继续运行。

## Risks / Trade-offs

- [Risk] Docker Volume 被部署脚本显式删除后 conversation 工作状态不可恢复 → 部署检查禁止 `down -v`，并在文档中明确 Volume 名称和恢复边界；本阶段接受无跨机灾备。
- [Risk] 共享预部署 AIO 容器中的 Shell 可以访问绝对路径 → 当前只承诺业务目录隔离和默认 cwd；高安全环境必须在后续引入每会话容器或 OS 权限隔离。
- [Risk] MinIO 删除与数据库事务无法原子提交 → 使用持久清理状态、幂等前缀删除和定时重试实现最终一致性。
- [Risk] 大型 Skill、输入或输出消耗磁盘与传输资源 → 对归档、单文件、总大小、条目数和执行超时设置硬限制，并为临时目录设置 TTL。
- [Risk] ContextVar 在 SSE 取消时泄漏 → 在异步生成器内部绑定，针对正常完成、异常和取消分别测试 reset 顺序。
- [Risk] 现有 APP/子 Agent 的 LOCAL Skill 工具旁路 → Conversation Runner 在注册阶段统一覆盖并增加“无沙箱不得出现 execute 工具”的回归测试。

## Migration Plan

1. 先部署兼容新身份字段的 Manager 与 Runtime；两者须作为同一发布批次，旧调用方在升级前不能调用新 Runtime 团队接口。
2. 执行会话清理字段数据库迁移，默认历史会话 `cleanup_status=NONE`。
3. 创建并挂载对话工作区 Docker Volume，验证容器重建后保留测试文件。
4. 以 `disabled/auto 且无沙箱` 上线 Context 和 Workspace，确认现有纯文本对话与 Skill 激活不回归。
5. 配置测试 AIO 沙箱，逐步启用沙箱文件、代码和命令能力。
6. 启用 Artifact 上传、历史下载和清理重试任务。
7. 回滚代码时保留数据库新增列、MinIO 前缀和 Docker Volume；禁用对话沙箱与清理调度即可恢复旧执行路径，避免回滚过程中删除用户文件。
