## Purpose

为对话工作台提供可信、请求隔离且可跨多轮复用的沙箱执行环境，使顶层 Supervisor、用户 APP 和子 Agent 能在同一会话工作区内安全地使用文件、命令、代码与完整 Skill 制品。

## ADDED Requirements

### Requirement: 可信执行身份
Manager 调用对话 Runtime 时 SHALL 传递非空的项目、工作空间、用户、会话和本轮执行身份；Runtime SHALL 拒绝缺少必需身份的团队对话请求，且不得以 `anonymous` 替代缺失用户身份。

#### Scenario: Manager 发送完整身份
- **WHEN** 已认证用户在其拥有的工作空间会话中发送消息
- **THEN** Runtime 收到与该会话一致的 `projectId`、`workspaceId`、`userId`、`conversationId` 和 `executionId`

#### Scenario: Runtime 收到缺失身份的请求
- **WHEN** 团队对话请求缺少项目、工作空间或用户身份，或者字段仅包含空白字符
- **THEN** Runtime 拒绝该请求且不启动任何 Agent 或执行工具

### Requirement: 请求级对话执行上下文
Runtime SHALL 为每轮团队对话创建唯一的 `ConversationExecutionContext`，并在整个 SSE 执行、APP 调用和子 Agent 调用期间提供同一个上下文；流结束、取消或异常后 SHALL 释放该上下文。

#### Scenario: 并发会话隔离
- **WHEN** 两个不同会话并发执行
- **THEN** 每个协程只能读取本会话的身份、执行 ID 和工作目录

#### Scenario: 客户端中断流式连接
- **WHEN** 客户端在执行期间取消 SSE 连接
- **THEN** Runtime 关闭相关 Runner 并释放本轮执行上下文，不影响其他并发会话

### Requirement: 确定性会话工作目录
系统 SHALL 根据可信身份生成不包含原始用户输入路径片段的确定性 POSIX 工作目录，并将 conversation 级 `input`、`skills`、`work` 与 execution 级 `output`、`tmp` 分离。

#### Scenario: 同一会话继续下一轮
- **WHEN** 同一用户在同一会话启动新的 execution
- **THEN** 新 execution 复用该会话的 `input`、`skills` 和 `work`，同时获得独立的 `output` 和 `tmp`

#### Scenario: 不同会话生成路径
- **WHEN** 任意项目、工作空间、用户或会话身份不同
- **THEN** 系统生成不同的 conversation 工作目录

#### Scenario: 身份包含危险路径字符
- **WHEN** 身份字段包含路径分隔符、父目录片段或超长文本
- **THEN** 生成的实际目录仍限定在配置的工作区根目录下且不包含可逃逸片段

### Requirement: Docker Volume 工作区持久化
部署 SHALL 将对话工作区根目录挂载到命名 Docker Volume，并在普通镜像更新和容器重建过程中保留该 Volume。

#### Scenario: 更新沙箱镜像
- **WHEN** 使用保留 Volume 的部署流程重建沙箱容器
- **THEN** 已存在会话的 `input`、`skills` 和 `work` 内容在新容器中仍可访问

### Requirement: 远程沙箱能力注入
当配置远程安全沙箱时，系统 SHALL 为对话工作台 Agent 注入沙箱文件、代码和命令能力，且 SHALL 将默认工作目录限定为当前会话工作区；当未配置远程沙箱时，系统 SHALL 不注册这些能力并且不得回退到 Runtime LOCAL 执行。

#### Scenario: 已配置远程沙箱
- **WHEN** 对话执行配置了可用的远程沙箱服务
- **THEN** 顶层 Supervisor 能通过沙箱工具处理文件、执行代码和执行命令

#### Scenario: 未配置远程沙箱
- **WHEN** 对话执行未配置远程沙箱服务
- **THEN** Agent 仍可生成文本和激活 Skill 指令，但不能调用本地或远程文件、命令和代码工具

#### Scenario: 沙箱调用失败
- **WHEN** 远程沙箱不可达或执行超时
- **THEN** 本轮返回明确的沙箱错误且不在 Runtime 主机上重试执行

### Requirement: 父子 Agent 与 APP 共享执行边界
内置 Supervisor、用户选择的 APP 和由 Supervisor 调用的子 Agent SHALL 使用同一个业务会话身份和会话工作目录；`subExecutionId` SHALL 仅用于标识子调用，不得替代业务会话身份。

#### Scenario: Supervisor 调用子 Agent
- **WHEN** Supervisor 通过 handoff 工具调用子 Agent
- **THEN** 子 Agent 读取与父 Agent 相同的会话工作目录，同时事件仍携带独立 `subExecutionId`

#### Scenario: 用户选择 APP
- **WHEN** 用户在对话工作台选择已发布 APP 执行任务
- **THEN** APP 使用当前对话的可信身份和会话工作目录，而不是 `anonymous` 或新的伪会话目录

### Requirement: Skill 按需完整准备
系统 SHALL 始终向顶层 Agent 提供当前工作空间 Skill 描述，并仅在 Agent 激活 Skill 时下载、校验和准备完整 Skill 制品到当前会话沙箱目录；激活不得仅提供一个无法访问其附属文件的 `SKILL.md` 副本。

#### Scenario: 激活纯文本 Skill
- **WHEN** Agent 激活只依赖 `SKILL.md` 的 Skill
- **THEN** Agent 获得完整指令并按该指令继续生成结果

#### Scenario: 激活包含脚本或模板的 Skill
- **WHEN** Agent 激活包含脚本、模板或资源文件的 Skill 且远程沙箱可用
- **THEN** 完整制品被安全准备到本会话 `skills` 目录，Agent 可在沙箱中按 Skill 指令访问这些文件

#### Scenario: Skill 归档不安全
- **WHEN** Skill ZIP 包含路径穿越、符号链接、超限内容或不合法的 `SKILL.md`
- **THEN** 系统拒绝激活且不会把归档内容写入会话工作区
