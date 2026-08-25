## Why

对话工作台目前只能激活并读取 Skill 指令，缺少贯穿顶层 Supervisor、用户 APP 与子 Agent 的统一执行上下文、持久工作目录和远程沙箱能力；正式输出文件也没有可靠的交付与回收生命周期。当前开发阶段需要先基于 Docker Volume 建立可持续迭代的会话工作区，并将用户可下载的正式产物保存到 MinIO，避免继续依赖 Runtime 容器本地目录或产生无法回收的对象。

## What Changes

- **BREAKING**：对话 Runtime 内部接口 `/v1/conversation/team` 要求 Manager 传入非空 `projectId`、`workspaceId` 与 `userId`，并在执行入口保留可信身份。
- 新增请求级 `ConversationExecutionContext`，统一承载项目、工作空间、用户、会话和本轮执行身份，并在 SSE 生命周期内安全绑定和释放。
- 新增确定性的会话工作目录模型，以 Docker Volume 保存 conversation 级 `input/skills/work`，并以 execution 级目录隔离 `output/tmp`。
- 配置远程沙箱时，为对话工作台顶层 Supervisor、用户 APP 和子 Agent 共享同一会话执行上下文与工作区；未配置远程沙箱时不回退 Runtime LOCAL 执行。
- Skill 继续按需激活，完整制品准备到会话沙箱工作区，而不只返回 `SKILL.md` 文本。
- 用户上传文件按需准备到沙箱 `input` 目录；正式输出从本轮 `output` 目录收集并上传 MinIO，再作为对话 Artifact 返回。
- 正式 Artifact 使用可信的会话专属对象前缀，删除会话时同时触发 MinIO 产物和 Docker Volume 工作目录的可重试级联清理。
- 不在本阶段引入 MinIO 工作区检查点、跨服务器恢复、动态一会话一容器或本地执行回退。

## Capabilities

### New Capabilities

- `conversation-sandbox-execution`: 对话执行身份、请求级执行上下文、Docker Volume 工作区、沙箱工具注入以及父子 Agent/APP 的上下文共享。
- `conversation-artifact-lifecycle`: 输入与 Skill 制品准备、正式输出 Artifact 上传 MinIO、对话文件引用和删除会话时的可重试资源清理。

### Modified Capabilities

无。

## Impact

- Manager 对话模块：Runtime 请求契约、Artifact 事件持久化、会话删除与清理状态。
- Runtime 对话模块：团队请求模型、Supervisor/APP/子 Agent Runner、Skill 激活、执行上下文、工作目录与 Sandbox SysOperation 接入。
- 对象存储：新增对话 Artifact 专属前缀及按前缀删除流程，复用现有 MinIO/OBS 抽象。
- 部署：AIO 沙箱的工作区根目录需要挂载命名 Docker Volume；更新部署不得删除该 Volume。
- 前端：后续展示正式 Artifact 下载入口，不改变现有 `/Skill` 推荐协议。
