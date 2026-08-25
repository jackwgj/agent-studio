## Purpose

为对话工作台建立可追踪的输入与正式输出产物链路，确保用户能下载沙箱生成文件，并在会话删除后可靠回收 MinIO 产物和 Docker Volume 工作目录。

## ADDED Requirements

### Requirement: 输入文件准备
系统 SHALL 将当前消息引用的有效输入文件按需准备到本会话沙箱 `input` 目录，并 SHALL 保留原始文件名与可信存储标识；过期临时 URL 不得作为长期持久化标识。

#### Scenario: 带附件启动对话执行
- **WHEN** 用户发送包含有效文件引用的消息且沙箱执行需要访问附件
- **THEN** Runtime 将文件准备到当前会话 `input` 目录并向 Agent 提供该沙箱路径

#### Scenario: 输入文件下载失败
- **WHEN** 输入对象不存在、校验失败或超过限制
- **THEN** 系统返回明确的输入准备错误且不把不完整文件交给 Agent

### Requirement: 正式输出上传 MinIO
系统 SHALL 仅从本轮可信 `output` 目录收集正式输出，并在上传 MinIO 成功后生成 Artifact 事件；正式输出 SHALL 使用由可信会话身份派生的专属对象前缀。

#### Scenario: Agent 生成正式文件
- **WHEN** 沙箱在本轮 `output` 目录生成符合限制的 Word、PDF、Excel、PPT、图片、压缩包或代码制品
- **THEN** Runtime 将文件上传 MinIO，并返回包含对象标识、文件名、大小、类型和校验值的 Artifact

#### Scenario: 输出上传失败
- **WHEN** 正式输出无法上传 MinIO
- **THEN** 系统不得向用户宣称该文件可下载，并返回可识别的 Artifact 上传错误

#### Scenario: 输出路径不可信
- **WHEN** 待收集文件位于本轮 `output` 之外、是符号链接或路径逃逸结果
- **THEN** 系统拒绝收集该文件

### Requirement: Artifact 持久化与下载
Manager SHALL 将 Assistant 正式 Artifact 的持久对象标识保存到对应对话消息，并 SHALL 在用户请求下载时生成可用下载地址，而不是把临时预签名 URL 作为唯一持久记录。

#### Scenario: 刷新历史会话
- **WHEN** 用户刷新页面并重新打开包含 Artifact 的历史会话
- **THEN** 前端仍能展示文件名、大小和可重新生成的下载入口

#### Scenario: 用户下载已保存产物
- **WHEN** 已授权用户点击 Artifact 下载
- **THEN** 系统根据持久对象标识生成当前有效的下载地址

### Requirement: 会话专属 Artifact 前缀
系统 SHALL 将对话正式产物写入 `conversation-artifacts` 下由项目、工作空间、用户和会话身份派生的专属前缀，并 SHALL 避免在该前缀中混入 Skill、共享组件或外部输入对象。

#### Scenario: 同一会话多轮生成产物
- **WHEN** 同一会话的多个 execution 生成输出
- **THEN** 所有正式产物位于该会话前缀下，并按 execution 区分

#### Scenario: 不同会话生成同名文件
- **WHEN** 两个会话均生成名为 `result.xlsx` 的文件
- **THEN** 两个对象位于不同会话前缀且不会相互覆盖

### Requirement: 删除会话触发可重试清理
删除会话时，系统 SHALL 在同一数据库事务内完成会话软删除与清理状态 `PENDING` 的记录，并 SHALL 异步或立即尝试删除该会话的 MinIO Artifact 前缀及 Docker Volume 工作目录；失败任务 SHALL 可被定时重试。

#### Scenario: 会话资源全部清理成功
- **WHEN** 用户删除其拥有的会话且 MinIO 与 Runtime 工作区均可访问
- **THEN** 会话从用户列表消失、专属 Artifact 前缀被删除、Volume 工作目录被删除且清理状态变为 `DONE`

#### Scenario: MinIO 清理暂时失败
- **WHEN** 会话已软删除但 MinIO 删除失败
- **THEN** 清理状态保持可重试，系统记录失败原因并由后续任务继续删除，而不是永久遗留无效产物

#### Scenario: 工作目录清理暂时失败
- **WHEN** 会话已软删除但 Runtime 或沙箱暂时不可用
- **THEN** 清理状态保持可重试，并在服务恢复后继续删除对应 Volume 目录

### Requirement: 清理目标必须由可信身份计算
资源清理 SHALL 依据已持久化会话身份计算 Artifact 前缀和工作目录，不得接受浏览器提供的任意对象前缀或文件系统路径，也不得删除不属于该会话的输入、Skill 或共享对象。

#### Scenario: 删除请求携带伪造路径
- **WHEN** 客户端尝试在删除请求中附带任意存储前缀或文件路径
- **THEN** 系统忽略或拒绝该路径，并只清理服务端根据会话身份计算出的目标

### Requirement: 临时执行目录清理
系统 SHALL 在 execution 正常完成、失败、取消或达到 TTL 后清理本轮 `tmp`；已经成功上传的本轮 `output` 工作副本 MAY 同时清理，未确认上传的输出不得被误报为正式 Artifact。

#### Scenario: execution 正常完成
- **WHEN** 本轮 Artifact 已完成上传且执行结束
- **THEN** 系统可清理本轮 `tmp` 和本地 `output` 副本而不影响 MinIO 中的正式产物

#### Scenario: execution 异常终止
- **WHEN** 本轮执行异常或客户端中断
- **THEN** 系统在 TTL 到期后清理遗留临时目录，且不会删除 conversation 级 `input`、`skills` 和 `work`
