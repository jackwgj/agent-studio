# 对话工作台 Skill 推荐与按需激活验证记录

## 1. 验证范围与版本

- 验证日期：2026-08-18。
- 功能分支验证起点：`c0b5fb9689418de398457a0cfc03df559b487041`。
- 验证范围：Manager 对话工作台测试、Runtime supervisor 与 conversation team 测试、前端对话工作台测试及开发构建、并发请求隔离、远端基线差异、本地快速启动条件。
- 本轮没有执行 `merge`、`rebase`、`push`，也没有修改或新增 `+` / Agent 选择功能。

## 2. 并发隔离 TDD 记录

新增测试：

`agent-runtime/tests/unit_tests/serve/apis/test_conversation_team.py::test_concurrent_skill_context_and_event_channel_are_isolated`

### RED：确认原测试集缺少该并发覆盖

```powershell
$env:PYTHONPATH="$PWD\agent-runtime;$PWD\packages\storage;$PWD\packages\model_service;$PWD\packages\common_utils"
& '..\..\.venv\Scripts\python.exe' -m pytest agent-runtime/tests/unit_tests/serve/apis/test_conversation_team.py -q -k "concurrent_skill_context_and_event_channel_are_isolated"
```

结果：退出码 1，`15 deselected`，没有匹配测试。该红阶段证明原测试集缺少计划任务 8 指定的并发 Skill 上下文与事件通道联合隔离覆盖。

### GREEN：现有实现通过新增并发契约

新增用例使用 `asyncio.gather()` 并发执行两个独立 Agent：分别绑定只含 `s1`、`s2` 的 `SkillExecutionContext`，并分别绑定 `exec-1`、`exec-2` 的 `EventChannel`。断言结果和 `skill_activated` 事件均只包含各自 Skill，事件 execution ID 不串流，退出后两个 ContextVar 均恢复为空。

使用相同精确命令重跑，结果：`1 passed, 15 deselected`。现有 `ContextVar + 独立 EventChannel` 生产实现已经满足该契约，无需修改生产代码。

## 3. 三层自动化回归

### 3.1 Manager

先按计划执行完整对话工作台通配测试：

```powershell
cd backend
mvn -pl studio-manager-service -am "-Dtest=**/conversation/**/*Test" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

真实结果：`Tests run: 73, Failures: 1, Errors: 0, Skipped: 0`。唯一失败为已确认基线项：

`AgentRuntimeConfigTest.testYml_ConfigKeyMatching`

该诊断测试的 harness 没有模拟 Spring 属性默认值，与 Skill 功能无关；本轮未修改或尝试修复。

随后枚举 `conversation` 包下全部 `*Test.java`，只排除 `AgentRuntimeConfigTest`，以 15 个测试类的精确 `-Dtest` 选择器重跑。结果：`Tests run: 72, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`。

### 3.2 Runtime

```powershell
$env:PYTHONPATH="$PWD\agent-runtime;$PWD\packages\storage;$PWD\packages\model_service;$PWD\packages\common_utils"
& '..\..\.venv\Scripts\python.exe' -m pytest agent-runtime/tests/unit_tests/supervisor agent-runtime/tests/unit_tests/serve/apis/test_conversation_team.py -q
```

结果：`132 passed, 4 warnings`。警告均为依赖包的 Pydantic/DashScope 弃用提示，没有测试失败；结果包含本轮新增并发隔离用例。

### 3.3 前端

仓库默认 Karma 配置存在既有 TS6082/空壳问题，本轮继续使用可删除的 scoped Karma harness，只加载 `conversation-workspace/**/*.spec.ts`。准备 harness 时先后发现新版 `zone.js` 导出路径及初始化顺序不兼容，修正临时 harness 为 `zone.js` 后再加载 `zone.js/testing`，并仅在 harness 中提供测试所需的 `window.AppWebPath`；这些失败属于临时测试入口，不涉及产品代码。

最终命令：

```powershell
cd frontend
$env:CI='true'
pnpm exec ng test --watch=false --browsers=ChromeHeadless --karma-config='.cache/task-7-scratch/karma.conf.cjs' --ts-config='.cache/task-7-scratch/tsconfig.json' --main='.cache/task-7-scratch/test.ts' --include='src/routes/conversation-workspace/**/*.spec.ts'
pnpm exec tsc --noEmit -p tsconfig.app.json
pnpm exec ng build --configuration development
```

结果：

- Chrome Headless：`TOTAL: 66 SUCCESS`。
- TypeScript：退出码 0。
- Angular development build：退出码 0，耗时 207.866 秒。
- 构建只报告既有 Browserslist、`isolatedModules` 和 Sass `@import` 弃用警告。
- scoped Karma harness 已删除且未提交。

## 4. 远端 0804 基线差异

执行：

```powershell
git fetch origin
git log --oneline HEAD..origin/studio-2.0-dev-0804
git show --name-status fbde38df
git merge-tree (git merge-base HEAD origin/studio-2.0-dev-0804) HEAD origin/studio-2.0-dev-0804
```

远端从已同步的 `c2d5f62861d694ff2049f1e7a758c63f86cbed5c` 前进 1 个提交：

- `fbde38df0d41eabc095a784c169657d95e78a2c1`：持久化对话轮次事件并恢复 turns。

该提交实际修改 12 个文件，其中与本功能双方同时修改的热点包括：

- `agent-runtime/agent_runtime/supervisor/event/adapt.py`
- `backend/studio-manager-service/src/main/java/com/openjiuwen/studio/conversation/application/ConversationWorkspaceAppService.java`
- `backend/studio-manager-service/src/test/java/com/openjiuwen/studio/conversation/infrastructure/adapter/ConversationRunEventSourceListenerTest.java`
- `frontend/src/routes/conversation-workspace/conversation-workspace.component.html`
- `frontend/src/routes/conversation-workspace/conversation-workspace.component.less`
- `frontend/src/routes/conversation-workspace/conversation-workspace.component.ts`

三方只读合并检查显示以下文件存在明确文本冲突：

- `backend/studio-manager-service/src/test/java/com/openjiuwen/studio/conversation/infrastructure/adapter/ConversationRunEventSourceListenerTest.java`
- `frontend/src/routes/conversation-workspace/conversation-workspace.component.ts`

其余双方同时修改文件虽可自动合并，仍需在获得用户确认后的同步阶段复核对话轮次持久化、`skill_activated` 非持久化边界和 SkillSelector 接线。当前分支相对远端为本地 33 个提交、远端 1 个提交；本轮按约束暂停同步，不做变基或合并。

## 5. 本地快速启动与真实联调状态

工具链实测：JDK 17.0.20、Maven 3.9.16、Node 22.23.2、pnpm 10.34.5；主工作区共享虚拟环境为 Python 3.11.9，FastAPI/Pydantic 可导入。Docker Engine 与 Compose 可用。

当前隔离工作树没有个人 `.vscode/local-dev.env`、`.vscode/local-dev.ide.env`、`deploy/.env` 或自身 `.venv`；主工作区存在 IDE dotenv、日志配置、部署配置和 Python 3.11 虚拟环境，但数据库、Redis、对象存储与 Builder 均配置为外部依赖。检查时本地 `31111`、`31014`、`31015`、`4200` 均未监听，标准本地 Redis/MinIO 端口也未监听；已有 Docker 容器不是本任务四层本地开发组合。

由于启动 Manager 可能连接并写入外部数据库，且当前尚未确认可用于本轮七项验收的测试账号、三个 Skill 的真实 ID/版本、模型服务和对象存储制品，本轮没有擅自启动服务，也没有伪造浏览器联调结果。自动化测试和构建已经完成，真实服务级联调仍为阻塞项。

待具备安全联调条件后，需要由主代理或用户在确认的测试环境中执行：

1. 不使用 `/` 时验证完整目录注入及自主激活。
2. 菜单推荐 `professional-rewriter-cn` 并核对推荐顺序。
3. 同时推荐 `meeting-minutes-cn` 与 `professional-rewriter-cn` 并逐项激活。
4. 手写 `/meeting-minutes-cn` 且不点菜单，确认推荐 ID 为空。
5. 篡改为其他工作空间 Skill ID，确认 Manager 在落 user 消息前拒绝。
6. 确认激活标签实时出现且刷新历史不恢复。
7. 激活依赖未注册命令/沙箱工具的 Skill，确认明确报告能力缺失而不伪造结果。

三个验收 Skill 名称已知为 `meeting-minutes-cn`、`requirement-clarifier-cn`、`professional-rewriter-cn`；本轮没有读取到可验证的真实 Skill ID/版本，因此不在文档中编造版本信息，也不记录对象存储密钥或临时 URL。

## 6. 结论

- Manager、Runtime、前端的 Skill 定向自动化回归全部通过；唯一 Manager 通配失败为已确认并显式排除的既有基线诊断测试。
- 新增的并发回归证明 Skill ContextVar 与 `skill_activated` EventChannel 在并发请求间互不串流，现有生产实现无需修复。
- 前端构建与类型检查通过，临时测试 harness 未进入提交。
- 真实浏览器联调尚未执行，不能据此宣称七个端到端场景通过。
- 远端 0804 已前进且存在两个明确冲突文件；合并准备状态为“等待用户确认后同步并解冲突”。
