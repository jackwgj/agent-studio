## 1. 可信身份与请求契约

- [x] 1.1 （RED）补充 Manager 调用 Runtime 时透传 `projectId`、`workspaceId`、`userId` 的适配器测试，并验证缺失字段的旧实现会导致测试失败
- [x] 1.2 （GREEN）扩展 Manager 团队对话请求体与 Runtime 请求模型，要求项目、工作空间和用户身份非空，并验证 Manager 适配器测试和 Runtime 请求校验测试通过
- [x] 1.3 （RED/GREEN）补充并实现 Supervisor、用户 APP 的可信身份 `globalVariables` 兼容透传，并验证现有 Runtime 对话测试通过且不再生成 `anonymous` 身份

## 2. ConversationExecutionContext 与工作目录模型

- [x] 2.1 （RED）为不可变 `ConversationIdentity`、`ConversationWorkspace`、`ConversationExecutionContext` 编写单元测试，覆盖必填字段、不可变性和父子调用共享身份，并验证测试先失败
- [x] 2.2 （GREEN）实现三个执行上下文模型，并验证 2.1 的模型测试全部通过
- [x] 2.3 （RED）为工作目录派生编写测试，覆盖同会话复用、不同身份隔离、execution 目录隔离、危险字符、超长身份和目录逃逸，并验证测试先失败
- [x] 2.4 （GREEN）使用 SHA-256 路径键和 `PurePosixPath` 实现 conversation 级 `input/skills/work` 与 execution 级 `runs/<execution>/output/tmp`，并验证所有路径始终位于配置根目录内
- [x] 2.5 （RED）为专用 `ContextVar` 编写并发、正常结束、异常和 SSE 取消测试，并验证上下文泄漏测试在实现前失败
- [x] 2.6 （GREEN）在 `team_sse_stream` 确定 execution 后绑定上下文，并在 Runner 关闭后的 `finally` 中可靠 reset，验证并发与取消测试通过
- [x] 2.7 补充 APP 与 handoff 子 Agent 的上下文继承测试并实现共享业务身份和 Workspace、独立 `subExecutionId`，验证父子事件分组正确且路径相同

## 3. Docker Volume 与部署约束

- [ ] 3.1 增加 `CONVERSATION_SANDBOX_WORKSPACE_ROOT` 配置并以 `/workspace` 为默认值，完成启动配置校验，验证相对路径、父目录穿越或非 POSIX 根路径会给出明确错误
- [ ] 3.2 在 AIO 部署编排中把命名 Docker Volume 挂载到 `/workspace`，核对 Manager 不挂载该 Volume，并通过容器内挂载点检查验证配置生效
- [ ] 3.3 增加“更新不得执行 `down -v` 或删除命名 Volume”的部署说明和检查脚本，并验证危险命令能被检查流程识别
- [ ] 3.4 执行 AIO Volume 可写与容器重建冒烟测试，验证 `/workspace` 可写、已有会话的 `input/skills/work` 文件保留且新 execution 获得独立 `output/tmp`

## 4. 对话专用远程沙箱能力

- [ ] 4.1 （RED）为 `auto/sandbox/disabled` 三种对话执行模式编写配置测试，覆盖无远程地址、地址无效和禁止 LOCAL 回退，并验证测试先失败
- [ ] 4.2 （GREEN）实现 `ConversationSysOperationFactory` 和 `SandboxGatewayConfig`，验证 `auto` 无配置时不注册执行工具、`sandbox` 无配置时明确失败、`disabled` 始终禁用
- [ ] 4.3 （RED）为顶层 Supervisor 工具注入编写测试，验证远程沙箱配置存在时应提供文件、代码和命令能力，缺失时不应出现相关 ToolCard
- [ ] 4.4 （GREEN）将对话专用 SysOperation 注入 Supervisor，并验证工具默认 cwd 为当前会话工作区且远程超时不会在 Runtime 主机重试
- [ ] 4.5 为用户 APP 和 handoff 子 Agent 增加同一 SysOperation/Workspace 的回归测试与实现，验证三类 Runner 均访问同一 conversation 工作目录
- [ ] 4.6 覆盖 `ConversationReActRunner` 的官方 Skill LOCAL 工具注册旁路，并验证配置 Skill 但未配置远程沙箱时不会出现 `execute_code`、`execute_cmd` 或本地文件工具
- [ ] 4.7 为尚未适配的 Controller/PlanExecute 路径增加显式禁用或失败保护，并验证它们不会静默使用 LOCAL 执行

## 5. 输入文件与完整 Skill 制品准备

- [ ] 5.1 （RED）为 ArtifactBridge 输入准备编写测试，覆盖可信对象标识、原始文件名、校验值、大小限制、下载失败和不完整文件，并验证测试先失败
- [ ] 5.2 （GREEN）实现消息附件到当前会话 `input` 的受控传输，并验证 Agent 只获得沙箱内路径且持久记录不依赖过期预签名 URL
- [ ] 5.3 （RED）扩展 Skill 激活测试，覆盖完整 ZIP、脚本/模板/资源文件、版本复用、版本变化、路径穿越、符号链接、条目数和总大小限制，并验证不安全归档被拒绝
- [ ] 5.4 （GREEN）复用现有 Skill 校验规则，经 ArtifactBridge 将完整制品准备到 conversation `skills` 目录，并验证模型不能指定上传或解压目标
- [ ] 5.5 扩展 Skill 激活结果，返回完整指令、可信沙箱路径和资源准备状态，并验证纯文本 Skill 与包含资源的 Skill 均能按需激活

## 6. 正式输出 Artifact 与下载链路

- [ ] 6.1 （RED）为输出收集器编写测试，覆盖仅允许本轮 `output`、拒绝父目录逃逸和符号链接、限制文件数量/单文件大小/总大小/类型，并验证测试先失败
- [ ] 6.2 （GREEN）实现受控输出扫描与校验，并验证 `tmp`、`work` 或其他 execution 的文件不会被当作正式产物
- [ ] 6.3 实现 `conversation-artifacts/<project>/<workspace>/<user>/<conversation>/<execution>/...` 可信对象键生成器，并通过同名文件、跨会话和危险文件名测试验证不覆盖且不逃逸
- [ ] 6.4 实现 Runtime 上传 MinIO 后再发送 `artifact` TeamEvent 的流程，并验证上传失败时不产生可下载成功事件
- [ ] 6.5 扩展 Artifact 事件和 Manager `FileRef` 持久模型，保存 `objectKey`、文件名、大小、MIME、checksum、executionId，并通过消息落库与历史加载测试验证元数据完整
- [ ] 6.6 实现带会话所有权校验的 Artifact 下载接口，按持久对象标识生成新的预签名 URL，并验证越权访问被拒绝且过期后可重新生成
- [ ] 6.7 在对话工作台展示正式 Artifact 的文件信息和下载入口，并通过前端组件测试及刷新历史会话的手工验收验证下载仍可用

## 7. 会话删除与可重试资源清理

- [ ] 7.1 新增 `cleanup_status/cleanup_attempts/cleanup_updated_at/cleanup_error` 数据库迁移及实体映射，并验证历史会话默认 `NONE`、迁移可重复执行
- [ ] 7.2 （RED）为删除会话事务编写测试，验证软删除与 `PENDING` 同事务提交，任一数据库操作失败时整体回滚
- [ ] 7.3 （GREEN）改造会话删除服务以写入可信清理任务，并验证客户端提供的对象前缀或文件路径被忽略或拒绝
- [ ] 7.4 实现基于持久会话身份计算 Artifact 前缀的 MinIO 幂等删除，复用存储抽象的按前缀删除能力，并验证只删除本会话正式产物
- [ ] 7.5 为 Runtime 内部工作目录清理接口编写认证、身份校验、路径逃逸和幂等测试，并验证任意文件系统路径不能作为接口参数
- [ ] 7.6 实现 Runtime 根据可信身份计算并删除 conversation Volume 根目录的内部接口，并验证不会删除其他会话或 Volume 根目录
- [ ] 7.7 实现 Manager 清理编排与状态流转，只有 MinIO 和 Volume 均成功才写 `DONE`，并通过单侧失败测试验证任务保持可重试
- [ ] 7.8 实现带次数上限和退避策略的定时重试任务，记录受限长度错误信息，并验证重复执行、服务恢复和并发抢占场景不会误删或重复失败
- [ ] 7.9 实现 execution `tmp` 的完成/失败/取消/TTL 清理，以及已成功上传 `output` 本地副本的可选清理，并验证 conversation 级目录始终保留到会话删除

## 8. 集成、安全与发布验收

- [ ] 8.1 在无沙箱配置下执行回归测试，验证纯文本对话和 Skill 指令仍可用，同时 Runtime 主机上不存在 LOCAL 文件、Shell 或 Code 执行
- [ ] 8.2 在配置 AIO 沙箱的测试环境执行端到端测试，验证 Supervisor、APP、子 Agent 能在同一会话目录进行文件、命令和代码操作，并验证跨会话隔离
- [ ] 8.3 执行完整 Skill 端到端测试，验证包含脚本、模板和资源的 Skill 可在沙箱中访问，恶意或超限 Skill 被拒绝
- [ ] 8.4 执行输入到输出端到端测试，生成 Word/PDF/Excel/PPT/图片/压缩包或代码制品中的代表性文件，验证 MinIO 上传、历史展示和下载校验值一致
- [ ] 8.5 执行删除会话故障注入测试，分别模拟 MinIO、Runtime 和沙箱不可用，验证会话立即隐藏、任务最终重试成功且 Artifact 与 Volume 数据均被清理
- [ ] 8.6 运行 Runtime 单元/集成测试、Manager Maven 测试和前端测试/构建，记录命令与结果，并确认没有修改 vendored `agent-runtime/jiuwen/**` 或官方通用 Runner 主流程
- [ ] 8.7 更新配置、Docker Volume、AIO 沙箱接入、Artifact 下载、清理重试和回滚边界文档，并通过一次按文档执行的部署演练验证可操作性
- [ ] 8.8 完成代码审查与安全检查，确认共享 AIO 只承诺目录级业务隔离、不宣称强容器隔离，并确保后续提交说明全部使用中文
