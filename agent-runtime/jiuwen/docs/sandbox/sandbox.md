# OpenJiuWen 沙箱操作接入机制解析

***

## 1. 沙箱操作概述

OpenJiuWen 的沙箱操作提供三种隔离执行能力：**文件系统（FS）**、**Shell 命令执行** 和 **代码执行（Code）**
。所有操作均在独立的沙箱环境中运行，与本地系统完全隔离。

```
┌──────────────────────────────────────────────────────────────┐
│                      使用方式                                 │
│                                                              │
│  ① 直接调用:  sys_op.shell().execute_cmd(...)                │
│  ② 工具调用:  tool = get_tool("id.shell.execute_cmd")        │
│ ③ 插件调用:  SysOperationPlugin → read_file / execute_code   │
│                  / execute_shell                              │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────▼───────────────────────────────────┐
│                三大操作能力                                    │
│                                                              │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │ FS 操作   │  │ Shell 操作    │  │ Code 操作     │           │
│  │ 文件读写   │  │ 命令执行      │  │ 代码执行      │           │
│  │ 目录列表   │  │ 流式输出      │  │ 流式输出      │           │
│  │ 文件搜索   │  │ 后台进程      │  │ Python/JS    │           │
│  │ 上传下载   │  │              │  │              │           │
│  └──────────┘  └──────────────┘  └──────────────┘           │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────▼───────────────────────────────────┐
│                沙箱路由层                                      │
│                                                              │
│  SandboxGateway (单例)                                       │
│    ├── isolation_key 定位沙箱实例                             │
│    ├── Launcher 创建/连接沙箱                                 │
│    ├── Provider 适配沙箱 API                                  │
│    └── SandboxStore 管理沙箱生命周期                           │
└──────────────────────────────────────────────────────────────┘
```

***

## 2. 三大操作能力

### 2.1 文件系统操作（FS） — `sys_op.fs()`

| 方法                     | 返回类型                                      | 说明                                    |
|------------------------|-------------------------------------------|---------------------------------------|
| `read_file`            | `ReadFileResult`                          | 读取文件，支持 head / tail / line_range 分段读取 |
| `read_file_stream`     | `AsyncIterator[ReadFileStreamResult]`     | 流式读取，适用于大文件                           |
| `write_file`           | `WriteFileResult`                         | 写入文件，支持追加/覆盖、权限设置                     |
| `upload_file`          | `UploadFileResult`                        | 上传本地文件到沙箱                             |
| `upload_file_stream`   | `AsyncIterator[UploadFileStreamResult]`   | 流式上传大文件                               |
| `download_file`        | `DownloadFileResult`                      | 从沙箱下载文件到本地                            |
| `download_file_stream` | `AsyncIterator[DownloadFileStreamResult]` | 流式下载大文件                               |
| `list_files`           | `ListFilesResult`                         | 列出文件，支持递归、排序、按扩展名过滤                   |
| `list_directories`     | `ListDirsResult`                          | 列出目录，支持递归、最大深度                        |
| `search_files`         | `SearchFilesResult`                       | 按模式搜索文件，支持排除规则                        |

### 2.2 Shell 命令执行 — `sys_op.shell()`

| 方法                       | 返回类型                                    | 说明                                    |
|--------------------------|-----------------------------------------|---------------------------------------|
| `execute_cmd`            | `ExecuteCmdResult`                      | 同步执行命令，返回 stdout / stderr / exit_code |
| `execute_cmd_stream`     | `AsyncIterator[ExecuteCmdStreamResult]` | 流式执行命令，实时获取输出                         |
| `execute_cmd_background` | `ExecuteCmdBackgroundResult`            | 后台执行命令，返回 PID                         |

### 2.3 代码执行 — `sys_op.code()`

| 方法                    | 返回类型                                     | 说明                             |
|-----------------------|------------------------------------------|--------------------------------|
| `execute_code`        | `ExecuteCodeResult`                      | 同步执行代码，支持 Python / JavaScript  |
| `execute_code_stream` | `AsyncIterator[ExecuteCodeStreamResult]` | 流式执行代码，实时获取 stdout / stderr 输出 |

> 所有操作均支持 `timeout`（超时秒数）、`environment`（环境变量）、`cwd`（工作目录）等通用参数。返回结构统一为 `BaseResult[T]`，其中
`code=0` 表示成功。

***

## 3. 沙箱接入方式

### 3.1 方式一：直接调用

最基础的使用方式，通过 `SysOperationCard` 配置沙箱，获取 `SysOperation` 实例后直接调用操作方法。

```python
import asyncio
from openjiuwen.core.runner import Runner
from openjiuwen.core.sys_operation import (
    SysOperationCard, OperationMode, SandboxGatewayConfig,
    SandboxIsolationConfig, ContainerScope,
)
from openjiuwen.core.sys_operation.config import PreDeployLauncherConfig

async def main():
    await Runner.start()
    try:
        card = SysOperationCard(
            id="my_sandbox",
            mode=OperationMode.SANDBOX,
            gateway_config=SandboxGatewayConfig(
                isolation=SandboxIsolationConfig(
                    container_scope=ContainerScope.SYSTEM,
                ),
                launcher_config=PreDeployLauncherConfig(
                    base_url="http://localhost:8080",
                    sandbox_type="aio",
                ),
                timeout_seconds=30,
            ),
        )

        Runner.resource_mgr.add_sys_operation(card)
        sys_op = Runner.resource_mgr.get_sys_operation("my_sandbox")

        shell_res = await sys_op.shell().execute_cmd(command="echo hello world")
        print(shell_res.data.stdout.strip())

        fs_res = await sys_op.fs().read_file(path="/etc/hosts")
        print(fs_res.data.content)

        code_res = await sys_op.code().execute_code(
            code="print('Hello from Python')", language="python"
        )
        print(code_res.data.stdout.strip())
    finally:
        Runner.resource_mgr.remove_sys_operation(sys_operation_id="my_sandbox")
        await Runner.stop()

asyncio.run(main())
```

### 3.2 方式二：作为工具调用（推荐 Agent 集成）

注册沙箱后，操作方法自动包装为 `LocalFunction` 工具，可通过 Tool ID 获取：

```python
# 生成 Tool ID
tool_id = SysOperationCard.generate_tool_id("my_sandbox", "shell", "execute_cmd")
# tool_id = "my_sandbox.shell.execute_cmd"

# 获取并调用工具
tool = Runner.resource_mgr.get_tool(tool_id)
res = await tool.invoke({"command": "echo hello"})

# 添加到 Agent
agent.ability_manager.add(tool.card)
```

**Tool ID 格式**：`{card_id}.{op_type}.{method_name}`

| op\_type | 可用 method\_name                                                                                                                                                               |
|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `fs`     | `read_file`, `read_file_stream`, `write_file`, `upload_file`, `upload_file_stream`, `download_file`, `download_file_stream`, `list_files`, `list_directories`, `search_files` |
| `shell`  | `execute_cmd`, `execute_cmd_stream`, `execute_cmd_background`                                                                                                                 |
| `code`   | `execute_code`, `execute_code_stream`                                                                                                                                         |

### 3.3 方式三：通过插件调用

`SysOperationPlugin` 将沙箱操作封装为 `FunctionPlugin`，暴露三个精简工具：

| 插件工具名           | 底层操作                    | 功能描述                            |
|-----------------|-------------------------|---------------------------------|
| `read_file`     | `fs().read_file()`      | 异步文件读取，支持 head/tail/line\_range |
| `execute_code`  | `code().execute_code()` | 执行 Python/JavaScript 代码         |
| `execute_shell` | `shell().execute_cmd()` | 执行 Shell 命令                     |

插件方法返回统一字典格式：

```python
{
    "errCode": 0,           # 0=成功，非0=失败
    "errMessage": "success",
    "data": { ... }         # 具体结果数据
}
```

插件使用方式：

```python
from jiuwen.sys_operation.sys_operation_plugin import build_sysop_tools

tools = build_sysop_tools(sys_operation)
# 返回三个 Invokable 工具，可直接添加到 Agent
```

***

## 4. 沙箱环境准备

### 4.1 AIO Sandbox（推荐）

当前版本开箱即用的主路径是连接已启动的 AIO 沙箱。

**安装依赖**：

```bash
pip install -U "openjiuwen[sandbox]"
```

**启动 AIO 沙箱（Docker）**：

```bash
docker run --security-opt seccomp=unconfined --rm -it -p 8080:8080 ghcr.io/agent-infra/sandbox:latest
```

**配置连接**：

```python
PreDeployLauncherConfig(
    base_url="http://localhost:8080",
    sandbox_type="aio",
    idle_ttl_seconds=600,   # 空闲超时（秒）
)
```

### 4.2 自定义 Mock 沙箱（开发测试）

无需 Docker，通过自定义 Launcher + Provider 模拟沙箱行为：

```python
from openjiuwen.core.sys_operation.sandbox.sandbox_registry import SandboxRegistry

# 1. 定义 Launcher 配置
class MockLauncherConfig(SandboxLauncherConfig):
    launcher_type: Literal["mock"] = "mock"
    sandbox_type: str = "mock_sandbox"

# 2. 定义 Launcher
class MockLauncher(SandboxLauncher):
    async def launch(self, config, timeout_seconds, isolation_key=None):
        return LaunchedSandbox(base_url="mock://localhost:9999",
                               sandbox_id=f"mock_{isolation_key or 'default'}")

# 3. 定义 Shell Provider
class MockShellProvider(BaseShellProvider):
    async def execute_cmd(self, command, *, cwd=None, timeout=300,
                          shell_type: Literal["auto", "cmd", "powershell", "bash", "sh"] = "auto",
                          ...):
        return ExecuteCmdResult(
            code=0, message="ok",
            data=ExecuteCmdData(command=command, cwd=cwd or "/home/sandbox",
                                exit_code=0, stdout=f"executed: {command}\n", stderr=""),
        )

# 4. 注册组件
SandboxRegistry.register_launcher("mock", MockLauncher)
SandboxRegistry.register_provider("mock_sandbox", "shell", MockShellProvider)

# 5. 使用
card = SysOperationCard(
    id="mock_demo",
    mode=OperationMode.SANDBOX,
    gateway_config=SandboxGatewayConfig(
        isolation=SandboxIsolationConfig(container_scope=ContainerScope.CUSTOM,
                                         custom_id="test_001"),
        launcher_config=MockLauncherConfig(sandbox_type="mock_sandbox"),
        timeout_seconds=30,
    ),
)
```

***

## 5. 隔离策略

### 5.1 三种隔离粒度

| 隔离级别        | 适用场景       | 沙箱复用规则                                |
|-------------|------------|---------------------------------------|
| **SYSTEM**  | 单进程应用，全局共享 | 所有请求共享同一个沙箱                           |
| **SESSION** | 多会话场景      | 同一 session\_id 共享沙箱，不同 session 使用不同沙箱 |
| **CUSTOM**  | 精确控制       | 用户通过 custom\_id 完全控制复用边界              |

### 5.2 isolation\_key 路由机制

每次操作请求通过 `isolation_key` 定位沙箱实例：

```
格式: {container_scope}_{launcher_type}_{sandbox_type}_{prefix}{identity}
```

| 级别      | identity         | 示例                                      |
|---------|------------------|-----------------------------------------|
| SYSTEM  | `"system"`       | `system_pre_deploy_aio___system`        |
| SESSION | 运行时 `session_id` | `session_pre_deploy_aio_agent1__abc123` |
| CUSTOM  | 用户指定 `custom_id` | `custom_pre_deploy_aio__my_box_001`     |

**SESSION 级别的 session\_id 来源**：从协程上下文变量自动获取，支持运行时动态创建沙箱。

### 5.3 同一会话内的 Agent 隔离

通过 `prefix` 字段，在同一 SESSION 内进一步隔离不同 Agent：

```python
SandboxIsolationConfig(container_scope=ContainerScope.SESSION, prefix="agent1_")
# isolation_key: session_pre_deploy_aio_agent1__{session_id}

SandboxIsolationConfig(container_scope=ContainerScope.SESSION, prefix="agent2_")
# isolation_key: session_pre_deploy_aio_agent2__{session_id}
```

### 5.4 沙箱生命周期管理

| 操作   | 方法                                                    | 说明                                |
|------|-------------------------------------------------------|-----------------------------------|
| 释放沙箱 | `SandboxGatewayClient.release(key, on_stop="delete")` | 释放指定沙箱资源                          |
| 停止策略 | `on_stop` 参数                                          | `"delete"` / `"pause"` / `"keep"` |
| 空闲淘汰 | `idle_ttl_seconds` 配置                                 | 超时自动删除释放资源                        |

***

## 6. 结果数据模型

所有操作返回统一的结果结构 `BaseResult[T]`：

```python
class BaseResult(BaseModel, Generic[T]):
    code: int          # 0=成功，非0=失败
    message: str       # 状态信息
    data: Optional[T]  # 业务数据（成功时返回）
```

### 6.1 Shell 结果数据

| 类                          | 核心字段                                                          |
|----------------------------|---------------------------------------------------------------|
| `ExecuteCmdData`           | `command`, `cwd`, `exit_code`, `stdout`, `stderr`             |
| `ExecuteCmdChunkData`      | `text`, `type`("stdout"/"stderr"), `chunk_index`, `exit_code` |
| `ExecuteCmdBackgroundData` | `command`, `cwd`, `pid`                                       |

### 6.2 FS 结果数据

| 类                  | 核心字段                                                               |
|--------------------|--------------------------------------------------------------------|
| `ReadFileData`     | `path`, `content`(str/bytes), `mode`                               |
| `WriteFileData`    | `path`, `size`, `mode`                                             |
| `FileSystemItem`   | `name`, `path`, `size`, `modified_time`, `is_directory`, `type`    |
| `FileSystemData`   | `total_count`, `list_items`(List\[FileSystemItem]), `root_path`    |
| `SearchFilesData`  | `total_matches`, `matching_files`, `search_path`, `search_pattern` |
| `UploadFileData`   | `local_path`, `target_path`, `size`                                |
| `DownloadFileData` | `source_path`, `local_path`, `size`                                |

### 6.3 Code 结果数据

| 类                      | 核心字段                                                          |
|------------------------|---------------------------------------------------------------|
| `ExecuteCodeData`      | `code_content`, `language`, `exit_code`, `stdout`, `stderr`   |
| `ExecuteCodeChunkData` | `text`, `type`("stdout"/"stderr"), `chunk_index`, `exit_code` |

***

## 7. 沙箱操作内部路由流程

一次 `sys_op.shell().execute_cmd(command="echo hello")` 的完整路由：

```
用户调用 sys_op.shell().execute_cmd(command="echo hello")
  │
  ├─ ① SysOperation._get_operation("shell")
  │     → OperationRegistry 按 mode=SANDBOX 查找
  │     → 返回 ShellOperation 实例（懒加载 + 缓存）
  │
  ├─ ② ShellOperation.execute_cmd(...)
  │     → self.invoke("execute_cmd", command="echo hello")
  │     → BaseSandboxMixin 解析 isolation_key_template
  │       → SESSION 级别：替换 {session_id} 为当前会话 ID
  │
  ├─ ③ SandboxGatewayClient.invoke(op_type="shell", method="execute_cmd")
  │     → 构造 GatewayInvokeRequest
  │
  └─ ④ SandboxGateway.handle_request(config, request)
        → 查找/创建 Provider（按 isolation_key + op_type 缓存）
          → 若沙箱不存在：Launcher.launch() 创建 → 记录到 SandboxStore
          → 若沙箱已暂停：Launcher.resume() 恢复
        → 反射调用 Provider.execute_cmd(**params)
        → Provider 调用沙箱 HTTP API → 返回 ExecuteCmdResult
```

***

## 8. 扩展新的沙箱类型

### 8.1 扩展点

| 扩展点            | 基类                  | 职责                   |
|----------------|---------------------|----------------------|
| Launcher       | `SandboxLauncher`   | 沙箱实例的创建/暂停/恢复/删除     |
| FS Provider    | `BaseFSProvider`    | 文件系统操作的沙箱 API 适配     |
| Shell Provider | `BaseShellProvider` | Shell 命令执行的沙箱 API 适配 |
| Code Provider  | `BaseCodeProvider`  | 代码执行的沙箱 API 适配       |

Launcher 和 Provider **正交组合**——可以只扩展 Provider（沙箱获取方式不变），也可以同时扩展两者。

### 8.2 最小扩展示例（仅 Shell）

```python
from openjiuwen.core.sys_operation.sandbox.sandbox_registry import SandboxRegistry
from openjiuwen.core.sys_operation.sandbox.launchers.base import SandboxLauncher, LaunchedSandbox
from openjiuwen.core.sys_operation.sandbox.providers.base_provider import BaseShellProvider

class MyLauncher(SandboxLauncher):
    async def launch(self, config, timeout_seconds, isolation_key=None):
        return LaunchedSandbox(base_url="http://my-sandbox:9000", sandbox_id=isolation_key)

class MyShellProvider(BaseShellProvider):
    async def execute_cmd(self, command, *, cwd=None, timeout=300,
                          environment=None, options=None,
                          shell_type: Literal["auto", "cmd", "powershell", "bash", "sh"] = "auto"):
        resp = await http_post(f"{self.endpoint.base_url}/exec", json={"cmd": command, "cwd": cwd})
        return ExecuteCmdResult(code=0, message="ok",
                                data=ExecuteCmdData(command=command, cwd=cwd or ".",
                                                     exit_code=resp["exit_code"],
                                                     stdout=resp["stdout"], stderr=resp["stderr"]))

SandboxRegistry.register_launcher("my_launcher", MyLauncher)
SandboxRegistry.register_provider("my_sandbox", "shell", MyShellProvider)
```

### 8.3 注册后使用

```python
card = SysOperationCard(
    id="custom_sandbox",
    mode=OperationMode.SANDBOX,
    gateway_config=SandboxGatewayConfig(
        isolation=SandboxIsolationConfig(container_scope=ContainerScope.CUSTOM,
                                         custom_id="demo"),
        launcher_config=SandboxLauncherConfig(
            launcher_type="my_launcher",
            sandbox_type="my_sandbox",
        ),
        timeout_seconds=30,
    ),
)
```

***

## 9. 配置参考

### 9.1 完整配置层级

```
SysOperationCard
├── id: str                              # 沙箱唯一标识
├── mode: OperationMode                  # 必须为 SANDBOX
└── gateway_config: SandboxGatewayConfig
    ├── isolation: SandboxIsolationConfig
    │   ├── container_scope: ContainerScope     # SYSTEM / SESSION / CUSTOM
    │   ├── prefix: Optional[str]               # 同 scope 内的命名空间前缀
    │   └── custom_id: Optional[str]            # CUSTOM 模式的固定 ID
    ├── launcher_config: SandboxLauncherConfig
    │   ├── launcher_type: str                  # 启动器类型，如 "pre_deploy"
    │   ├── sandbox_type: str                   # 沙箱类型，如 "aio"
    │   ├── on_stop: "delete"|"pause"|"keep"    # 停止策略
    │   ├── idle_ttl_seconds: Optional[int]     # 空闲超时（秒）
    │   └── extra_params: Dict                  # 自定义参数
    ├── timeout_seconds: int                    # 超时（秒）
    ├── auth_headers: Dict[str, str]            # 认证 HTTP 头
    └── auth_query_params: Dict[str, str]       # 认证查询参数
```

### 9.2 常见配置模板

**SYSTEM 级别 + AIO 直连**：

```python
SysOperationCard(
    id="sandbox",
    mode=OperationMode.SANDBOX,
    gateway_config=SandboxGatewayConfig(
        isolation=SandboxIsolationConfig(container_scope=ContainerScope.SYSTEM),
        launcher_config=PreDeployLauncherConfig(
            base_url="http://localhost:8080", sandbox_type="aio",
        ),
        timeout_seconds=30,
    ),
)
```

**SESSION 级别 + Agent 前缀隔离**：

```python
SysOperationCard(
    id="agent_sandbox",
    mode=OperationMode.SANDBOX,
    gateway_config=SandboxGatewayConfig(
        isolation=SandboxIsolationConfig(
            container_scope=ContainerScope.SESSION, prefix="agent1_",
        ),
        launcher_config=PreDeployLauncherConfig(
            base_url="http://localhost:8080", sandbox_type="aio",
        ),
        timeout_seconds=30,
    ),
)
```

***

## 10. 关键源码索引

| 模块             | 路径                                                           | 说明                                                      |
|----------------|--------------------------------------------------------------|---------------------------------------------------------|
| 操作入口           | `sys_operation/sys_operation.py`                             | `SysOperation`、`SysOperationCard`                       |
| 配置定义           | `sys_operation/config.py`                                    | 所有配置类                                                   |
| FS 操作基类        | `sys_operation/fs.py`                                        | `BaseFsOperation`，定义 10 个方法签名                           |
| Shell 操作基类     | `sys_operation/shell.py`                                     | `BaseShellOperation`，定义 3 个方法签名                         |
| Code 操作基类      | `sys_operation/code.py`                                      | `BaseCodeOperation`，定义 2 个方法签名                          |
| 操作注册表          | `sys_operation/registry.py`                                  | `OperationRegistry`，包扫描自动发现                             |
| 工具适配器          | `sys_operation/tool_adapter.py`                              | `SysOperationToolAdapter`，操作→工具转换                       |
| 沙箱 FS 操作       | `sys_operation/sandbox/fs_operation.py`                      | 通过 Gateway 路由的 FS 实现                                    |
| 沙箱 Shell 操作    | `sys_operation/sandbox/shell_operation.py`                   | 通过 Gateway 路由的 Shell 实现                                 |
| 沙箱 Code 操作     | `sys_operation/sandbox/code_operation.py`                    | 通过 Gateway 路由的 Code 实现                                  |
| 路由 Mixin       | `sys_operation/sandbox/sandbox_mixin.py`                     | `invoke()`/`invoke_stream()` 委托 Gateway                 |
| 沙箱注册表          | `sys_operation/sandbox/sandbox_registry.py`                  | Launcher/Provider 注册管理                                  |
| 网关核心           | `sys_operation/sandbox/gateway/gateway.py`                   | `SandboxGateway`，沙箱路由调度                                 |
| 网关客户端          | `sys_operation/sandbox/gateway/gateway_client.py`            | `SandboxGatewayClient`                                  |
| 沙箱存储           | `sys_operation/sandbox/gateway/sandbox_store.py`             | `InMemorySandboxStore`                                  |
| 启动器基类          | `sys_operation/sandbox/launchers/base.py`                    | `SandboxLauncher`                                       |
| 预部署启动器         | `sys_operation/sandbox/launchers/pre_deployment_launcher.py` | `PreDeploymentLauncher`                                 |
| Provider 基类    | `sys_operation/sandbox/providers/base_provider.py`           | `BaseFSProvider`/`BaseShellProvider`/`BaseCodeProvider` |
| FS Protocol    | `sys_operation/protocal/fs_protocal.py`                      | FS 最小接口集                                                |
| Shell Protocol | `sys_operation/protocal/shell_protocal.py`                   | Shell 最小接口集                                             |
| Code Protocol  | `sys_operation/protocal/code_protocal.py`                    | Code 最小接口集                                              |
| 结果基类           | `sys_operation/result/base_result.py`                        | `BaseResult[T]`                                         |
| Shell 结果       | `sys_operation/result/shell_operation_result.py`             | `ExecuteCmdData`/`ExecuteCmdResult` 等                   |
| FS 结果          | `sys_operation/result/fs_operation_result.py`                | `ReadFileData`/`FileSystemItem` 等                       |
| Code 结果        | `sys_operation/result/code_operation_result.py`              | `ExecuteCodeData`/`ExecuteCodeResult` 等                 |
| 插件封装           | `jiuwen/sys_operation/sys_operation_plugin.py`               | `SysOperationPlugin`                                    |

