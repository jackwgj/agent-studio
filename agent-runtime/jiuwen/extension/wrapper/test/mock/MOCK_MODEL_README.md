# Mock Model for Openjiuwen

这是一个适配于开源版本openjiuwen的mock_model，基于现有的商用版本mock_data开发，可用于测试和开发环境。

## 功能特性

- ✅ 完全兼容openjiuwen的Model接口
- ✅ 支持同步/异步调用
- ✅ 支持流式输出
- ✅ 支持工具调用
- ✅ 基于已有的mock_data.yaml和mock_deepseek.jsonl文件
- ✅ 动态注册机制，无需修改model.py
- ✅ 支持多种场景：普通问答、控制器、提问器、deepseek prompt自优化等

## 文件说明

- `openjiuwen_customer_chat_llm.py`: 主要的MockModelClient实现，采用动态注册机制
- `mock_data.yaml`: 预设的mock响应数据
- `mock_deepseek.jsonl`: deepseek prompt自优化的mock数据
- `test_open_customer_chat_llm.py`: 测试脚本

## 使用方法

### 1. 直接使用

```python
from openjiuwen.core.foundation.llm.model import Model
from openjiuwen.core.foundation.llm.schema.message import UserMessage
from openjiuwen.core.foundation.llm.schema.config import ModelRequestConfig, ModelClientConfig

# 导入MockModelClient（自动注册）
from jiuwen.extension.wrapper.test.mock.openjiuwen_customer_chat_llm import MockModelClient

async def test():
    # 创建配置
    model_config = ModelRequestConfig(model="mock_model")
    client_config = ModelClientConfig(
        client_id="mock_client_id",
        client_provider="Mock",  # 指定使用Mock提供者
        api_key="mock_api_key",  # mock模型不需要真实的API key
        api_base="mock_api_base" # mock模型不需要真实的API base
    )
    
    # 创建Model实例
    model = Model(model_client_config=client_config, model_config=model_config)
    
    # 调用模型
    result = await model.invoke([UserMessage(content="世界上最高的山")])
    print(result.content)  # 输出: 珠穆朗玛峰
    
    # 流式调用
    async for chunk in model.stream([UserMessage(content="测试流式输出")]):
        print(chunk.content, end="", flush=True)

# 运行测试
import asyncio
asyncio.run(test())
```

### 2. 在model_wrapper中使用

```python
from openjiuwen_studio.extensions.model_wrapper import ModelWrapper
from jiuwen.extension.wrapper.test.mock.openjiuwen_customer_chat_llm import MockModelClient

# 导入MockModelClient后，它会自动注册到客户端注册表

async def test():
    wrapper = ModelWrapper()
    
    # 使用Mock模型
    result = await wrapper.invoke(
        input=[{"role": "user", "content": "杭州今天的天气"}],
        model_id="mock_model",
        session_id="test_session",
        model="mock_model",
        model_provider="Mock"  # 指定使用Mock提供者
    )
    
    print(result.content)  # 输出: 皓月当空，万里无云

import asyncio
asyncio.run(test())
```

## 动态注册机制

MockModelClient采用了openjiuwen的动态注册机制，通过以下方式自动注册到客户端注册表：

```python
from openjiuwen.core.common.clients.client_registry import get_client_registry

# 动态注册MockModelClient到客户端注册表
def register_mock_model():
    registry = get_client_registry()
    registry.register_class(MockModelClient)

# 确保模块被导入时注册客户端
register_mock_model()
```

这种方式不需要修改openjiuwen的核心代码，便于维护和升级。

## 测试

使用提供的测试脚本验证MockModelClient的功能：

```bash
E:\backend\.venv\Scripts\python.exe E:\backend\jiuwen\extension\wrapper\test\mock\test_open_customer_chat_llm.py
```

测试包括：

- 简单用户消息测试
- 使用mock_data.yaml中的预设响应测试
- 流式调用测试

## 自定义扩展

### 添加新的mock响应

在`mock_data.yaml`文件中添加新的键值对：

```yaml
新的查询内容:
  - "对应的mock响应"
  - "如果有多个响应，会轮询返回"
```

### 扩展功能

可以在`MockModelClient`类中重写或扩展方法来满足特定需求。

## 注意事项

1. openjiuwen_customer_chat_llm.py需要在openjiuwen的项目路径下运行，确保能正确导入openjiuwen的模块
2. mock模型不需要真实的API key和API base，只需要提供任意值即可
3. 如果需要在其他环境使用，可能需要调整sys.path

## 版本兼容性

- 兼容openjiuwen的最新版本
- 采用动态注册机制，不依赖于特定版本的model.py实现

## 许可证

MIT
