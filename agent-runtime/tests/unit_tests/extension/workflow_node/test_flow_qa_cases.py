# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2026. All rights reserved.

"""
FlowQA 组件单元测试。

覆盖：
- 问答策略（random / index）
- 中断机制（USER_INTERACT 状态）
- 会话历史管理
- 结构化信息输出
- 状态持久化与恢复
- 多轮对话处理
- 错误处理
"""

from unittest.mock import Mock, AsyncMock

import pytest
from agent_runtime.extension.workflow_node.flow_qa import (
    FlowQA,
    FlowQAConfig,
    QAState,
    ExecutionStatus,
    QAStrategy,
)
from openjiuwen.core.session.internal.workflow import NodeSession, WorkflowSession
from openjiuwen.core.session.node import Session
from openjiuwen.core.workflow import (
    Workflow,
    End,
    Start,
    create_workflow_session,
    WorkflowExecutionState,
)


def _make_qa_session(comp_id: str = "qa1") -> Session:
    wf = WorkflowSession(workflow_id="qa_unit_wf", session_id="qa_unit_sess")
    node = NodeSession(wf, comp_id, "FlowQA")
    session = Session(node, False)
    session.get_global_state = Mock(return_value=None)
    session.update_global_state = Mock()
    session.interact = AsyncMock(return_value="用户回答")
    session.write_custom_stream = AsyncMock()
    return session


class TestQAState:
    """测试 QAState 状态管理"""

    def test_state_initialization(self):
        """测试状态初始化"""
        state = QAState()

        assert state.status == ExecutionStatus.START
        assert state.inputs == {}

    def test_state_with_values(self):
        """测试带值的初始化"""
        state = QAState(
            status=ExecutionStatus.USER_INTERACT,
            inputs={"name": "张三", "age": 25},
        )

        assert state.status == ExecutionStatus.USER_INTERACT
        assert state.inputs["name"] == "张三"
        assert state.inputs["age"] == 25


class TestQAStrategy:
    """测试问答策略枚举"""

    def test_strategy_values(self):
        """测试策略值"""
        assert QAStrategy.random.value == "random"
        assert QAStrategy.index.value == "index"


class TestFlowQAConfig:
    """测试 FlowQA 配置"""

    def test_config_from_dict(self):
        """测试从字典创建配置"""
        config = FlowQAConfig(
            name="test_qa",
            need_reply=True,
            qa_strategy="random",
            options=["选项1", "选项2", "选项3"],
            enable_struct_message=True,
            struct_output_template="结果：{{_NODE_OUTPUT}}",
            enable_history=True,
        )

        assert config.name == "test_qa"
        assert config.need_reply is True
        assert config.qa_strategy == "random"
        assert len(config.options) == 3
        assert config.enable_struct_message is True

    def test_config_default_values(self):
        """测试默认配置值"""
        config = FlowQAConfig()

        assert config.name is None
        assert config.need_reply is False
        assert config.qa_strategy == "random"
        assert config.options == []
        assert config.enable_history is True


class TestFlowQAInitialization:
    """测试 FlowQA 组件初始化"""

    def test_component_initialization_with_dict(self):
        """测试使用字典配置初始化"""
        config = {
            "name": "test_qa",
            "needReply": True,
            "qaStrategy": "random",
            "options": ["选项A", "选项B"],
        }
        qa = FlowQA(config)

        assert qa._conf.name == "test_qa"
        assert qa._conf.need_reply is True
        assert qa._conf.qa_strategy == "random"
        assert len(qa._conf.options) == 2

    def test_component_initialization_with_config_object(self):
        """测试使用配置对象初始化"""
        config = FlowQAConfig(
            name="my_qa",
            need_reply=False,
            qa_strategy="index",
            options=["第一项", "第二项"],
            index_key="select_index",
        )
        qa = FlowQA(config)

        assert qa._conf.name == "my_qa"
        assert qa._conf.need_reply is False
        assert qa._conf.qa_strategy == "index"
        assert qa._conf.index_key == "select_index"

    def test_component_requires_config(self):
        """测试缺少配置时抛出异常"""
        from openjiuwen.core.common.exception.errors import BaseError

        with pytest.raises(BaseError):
            FlowQA(None)

    def test_component_node_type(self):
        """测试节点类型"""
        qa = FlowQA({"options": ["选项"]})
        assert qa.component_type() == "EI.qa"

    def test_component_properties(self):
        """测试组件属性"""
        config = FlowQAConfig(name="test", options=["a", "b", "c"])
        qa = FlowQA(config)

        assert qa.node_type == "EI.qa"
        assert qa.config == config


class TestFlowQAShouldInterrupt:
    """测试 FlowQA 中断判断"""

    def test_should_interrupt_at_start(self):
        """测试起始状态不应中断"""
        qa = FlowQA({"options": ["选项"]})
        assert qa.should_interrupt() is False

    def test_should_interrupt_at_user_interact(self):
        """测试用户交互状态应中断"""
        qa = FlowQA({"options": ["选项"]})
        qa._node_state.status = ExecutionStatus.USER_INTERACT
        assert qa.should_interrupt() is True

    def test_should_interrupt_at_end(self):
        """测试结束状态不应中断"""
        qa = FlowQA({"options": ["选项"]})
        qa._node_state.status = ExecutionStatus.END
        assert qa.should_interrupt() is False


class TestFlowQAStateManagement:
    """测试 FlowQA 状态管理"""

    def test_get_state(self):
        """测试获取状态"""
        qa = FlowQA({"options": ["选项"]})
        state = qa.get_state()

        assert isinstance(state, QAState)
        assert state.status == ExecutionStatus.START

    def test_load_state(self):
        """测试加载状态"""
        qa = FlowQA({"options": ["选项"]})
        new_state = QAState(
            status=ExecutionStatus.USER_INTERACT,
            inputs={"name": "测试"},
        )
        qa.load_state(new_state)

        loaded_state = qa.get_state()
        assert loaded_state.status == ExecutionStatus.USER_INTERACT
        assert loaded_state.inputs["name"] == "测试"

    def test_get_node_status(self):
        """测试获取节点状态"""
        qa = FlowQA({"options": ["选项"]})
        assert qa.get_node_status() == ExecutionStatus.START

        qa._node_state.status = ExecutionStatus.END
        assert qa.get_node_status() == ExecutionStatus.END

    def test_error_to_output(self):
        """测试错误处理"""
        qa = FlowQA({"options": ["选项"]})
        qa._node_state.status = ExecutionStatus.USER_INTERACT

        qa.error_to_output()

        assert qa.get_node_status() == ExecutionStatus.END


class TestFlowQAStatePersistence:
    """测试 FlowQA 状态持久化"""

    def test_load_state_from_session(self):
        """测试从会话加载状态"""
        session = _make_qa_session()
        session.get_global_state = Mock(return_value=None)

        state = FlowQA._load_state_from_session(session)
        assert state.status == ExecutionStatus.START

    def test_load_state_from_session_with_saved_state(self):
        """测试从会话加载已保存状态"""
        session = _make_qa_session()
        session.get_global_state = Mock(
            return_value={"status": "user_interact", "inputs": {"key": "value"}}
        )

        state = FlowQA._load_state_from_session(session)
        assert state.status == ExecutionStatus.USER_INTERACT
        assert state.inputs["key"] == "value"

    def test_store_state_to_session(self):
        """测试保存状态到会话"""
        session = _make_qa_session()
        state = QAState(status=ExecutionStatus.END, inputs={"result": "ok"})

        FlowQA._store_state_to_session(state, session)

        session.update_global_state.assert_called_once()
        call_args = session.update_global_state.call_args[0][0]
        assert "flow_qa_state" in call_args


class TestFlowQAQuestionStrategy:
    """测试问答策略"""

    @pytest.mark.asyncio
    async def test_random_strategy(self):
        """测试随机策略"""
        qa = FlowQA(
            {
                "qaStrategy": "random",
                "options": ["选项A", "选项B", "选项C"],
            }
        )

        results = set()
        for _ in range(50):
            qa_result = qa._get_qa_by_strategy({})
            results.add(qa_result)

        assert len(results) > 1

    @pytest.mark.asyncio
    async def test_index_strategy(self):
        """测试索引策略"""
        qa = FlowQA(
            {
                "qaStrategy": "index",
                "options": ["选项A", "选项B", "选项C"],
                "index_key": "select_index",
            }
        )

        result = qa._get_qa_by_strategy({"select_index": 0})
        assert result == "选项A"

        result = qa._get_qa_by_strategy({"select_index": 2})
        assert result == "选项C"

    @pytest.mark.asyncio
    async def test_index_strategy_out_of_range(self):
        """测试索引越界"""
        qa = FlowQA(
            {
                "qaStrategy": "index",
                "options": ["选项A", "选项B"],
                "index_key": "index",
            }
        )

        from openjiuwen.core.common.exception.errors import BaseError

        with pytest.raises(BaseError):
            qa._get_qa_by_strategy({"index": 5})

    @pytest.mark.asyncio
    async def test_empty_options_raises(self):
        """测试空选项列表抛出异常"""
        qa = FlowQA(
            {
                "qaStrategy": "random",
                "options": [],
            }
        )

        from openjiuwen.core.common.exception.errors import BaseError

        with pytest.raises(BaseError):
            qa._get_qa_by_strategy({})

    @pytest.mark.asyncio
    async def test_unsupported_strategy_raises(self):
        """测试不支持的策略抛出异常"""
        qa = FlowQA(
            {
                "qaStrategy": "invalid_strategy",
                "options": ["选项"],
            }
        )

        from openjiuwen.core.common.exception.errors import BaseError

        with pytest.raises(BaseError):
            qa._get_qa_by_strategy({})


class TestFlowQAChatHistory:
    """测试会话历史功能"""

    def test_get_latest_k_rounds_chat(self):
        """测试获取最近 k 轮对话"""
        chat_history = [
            {"role": "user", "content": "第一轮问题"},
            {"role": "assistant", "content": "第一轮回答"},
            {"role": "user", "content": "第二轮问题"},
            {"role": "assistant", "content": "第二轮回答"},
            {"role": "user", "content": "第三轮问题"},
            {"role": "assistant", "content": "第三轮回答"},
        ]

        result = FlowQA.get_latest_k_rounds_chat(chat_history, 1)
        assert len(result) == 3
        assert result[0]["content"] == "第二轮回答"

        result = FlowQA.get_latest_k_rounds_chat(chat_history, 2)
        assert len(result) == 5

        result = FlowQA.get_latest_k_rounds_chat(chat_history, None)
        assert len(result) == 6

    @pytest.mark.asyncio
    async def test_get_latest_chat_history(self):
        """测试获取会话历史"""
        qa = FlowQA({"options": ["选项"]})
        session = _make_qa_session()

        mock_chat_history = [
            {"role": "user", "content": "你好"},
            {"role": "assistant", "content": "你好"},
        ]

        def mock_get_global_state(key):
            if key == "workflow_chat_history":
                return mock_chat_history
            return None

        session.get_global_state = Mock(side_effect=mock_get_global_state)

        history = qa.get_latest_chat_history(session)
        assert len(history) == 2

    @pytest.mark.asyncio
    async def test_get_latest_user_query_intent(self):
        """测试获取用户意图"""
        qa = FlowQA({"options": ["选项"]})
        session = _make_qa_session()

        mock_chat_history = [
            {"role": "user", "content": "查询天气", "intent": "查询"},
            {"role": "assistant", "content": "请告诉城市"},
        ]

        def mock_get_global_state(key):
            if key == "workflow_chat_history":
                return mock_chat_history
            return None

        session.get_global_state = Mock(side_effect=mock_get_global_state)

        intent = qa.get_latest_user_query_intent(session)
        assert intent == "查询"


class TestFlowQAInterruptFlow:
    """测试 FlowQA 中断流程"""

    @pytest.mark.asyncio
    async def test_invoke_without_need_reply(self):
        """测试不需要回复的场景"""
        session = _make_qa_session()
        qa = FlowQA(
            {
                "needReply": False,
                "qaStrategy": "random",
                "options": ["回复内容"],
                "enable_history": True,
            }
        )

        result = await qa.invoke({"userFields": {}}, session, None)

        assert "userFields" in result
        assert "response" in result["userFields"]
        assert qa.get_node_status() == ExecutionStatus.END

    @pytest.mark.asyncio
    async def test_invoke_with_need_reply_start_state(self):
        """测试需要回复的场景 - 起始状态（触发中断）"""
        session = _make_qa_session()
        qa = FlowQA(
            {
                "needReply": True,
                "qaStrategy": "random",
                "options": ["请告诉我您的名字？"],
                "enable_history": True,
            }
        )

        result = await qa.invoke({"userFields": {"query": "hello"}}, session, None)

        assert qa.get_node_status() == ExecutionStatus.USER_INTERACT

    @pytest.mark.asyncio
    async def test_invoke_with_need_reply_user_interact_state(self):
        """测试需要回复的场景 - 用户交互状态"""
        session = _make_qa_session()
        qa = FlowQA(
            {
                "needReply": True,
                "qaStrategy": "random",
                "options": ["请告诉我您的名字？"],
                "enable_history": True,
            }
        )

        qa._node_state.status = ExecutionStatus.USER_INTERACT
        qa._node_state.inputs = {"name": "请提供姓名"}

        mock_history = [
            {"role": "user", "content": "张三"},
            {"role": "assistant", "content": "请告诉我您的名字？"},
        ]

        def mock_get_global_state(key):
            if key == "workflow_chat_history":
                return mock_history
            return None

        session.get_global_state = Mock(side_effect=mock_get_global_state)

        result = await qa.invoke({"userFields": {}}, session, None)

        assert qa.get_node_status() == ExecutionStatus.USER_INTERACT

    @pytest.mark.asyncio
    async def test_invoke_with_need_reply_user_interact_with_intent(self):
        """测试需要回复的场景 - 用户交互状态但有多个意图（问题回放）"""
        session = _make_qa_session()
        qa = FlowQA(
            {
                "needReply": True,
                "qaStrategy": "random",
                "options": ["选项A", "选项B"],
                "enable_history": True,
            }
        )

        qa._node_state.status = ExecutionStatus.USER_INTERACT
        qa._node_state.inputs = {"key": "value"}

        mock_chat_history = [
            {"role": "user", "content": "测试", "intent": ["intent1", "intent2"]}
        ]

        def mock_get_global_state(key):
            if key == "workflow_chat_history":
                return mock_chat_history
            return None

        session.get_global_state = Mock(side_effect=mock_get_global_state)

        result = await qa.invoke({"userFields": {}}, session, None)

        assert qa.get_node_status() == ExecutionStatus.USER_INTERACT


class TestFlowQAStructuredMessage:
    """测试结构化信息功能"""

    @pytest.mark.asyncio
    async def test_struct_output_with_template(self):
        """测试结构化输出模板"""
        session = _make_qa_session()
        qa = FlowQA(
            {
                "needReply": False,
                "qaStrategy": "random",
                "options": ["原始回复"],
                "isStructMessage": True,
                "struct_output_template": "格式化：{{_NODE_OUTPUT}}",
                "enable_history": True,
            }
        )

        result = await qa.invoke({"userFields": {}}, session, None)

        assert "userFields" in result
        session.write_custom_stream.assert_called()


class TestFlowQAIntegration:
    """FlowQA 工作流集成测试"""

    @pytest.mark.asyncio
    async def test_full_workflow_with_interrupt(self):
        """测试完整工作流中断流程"""
        flow = Workflow()

        start = Start()

        flow.set_start_comp("start", start, inputs_schema={"query": "${query}"})

        qa = FlowQA(
            {
                "name": "question_node",
                "needReply": True,
                "qaStrategy": "random",
                "options": [
                    "我是{{input}}，您叫什么名字？",
                    "我是{{input}}，请提供您的姓名",
                ],
                "enable_history": True,
            }
        )
        flow.add_workflow_comp("qa", qa, inputs_schema={"input": "${start.query}"})

        end = End()
        flow.set_end_comp("end", end, inputs_schema={"answer": "${qa.response}"})

        flow.add_connection("start", "qa")
        flow.add_connection("qa", "end")

        session = create_workflow_session()
        session.interact = AsyncMock(return_value="张三")
        session.get_global_state = Mock(return_value=None)
        session.update_global_state = Mock()
        session.write_custom_stream = AsyncMock()

        res = await flow.invoke({"query": "Sam"}, session)

        assert qa.get_node_status() == ExecutionStatus.USER_INTERACT
        assert res.state == WorkflowExecutionState.INPUT_REQUIRED


class TestFlowQAEdgeCases:
    """边界情况测试"""

    @pytest.mark.asyncio
    async def test_empty_inputs(self):
        """测试空输入"""
        session = _make_qa_session()
        qa = FlowQA(
            {
                "needReply": False,
                "qaStrategy": "random",
                "options": ["回复"],
            }
        )

        result = await qa.invoke({}, session, None)
        assert "userFields" in result

    @pytest.mark.asyncio
    async def test_no_user_fields_key(self):
        """测试没有 userFields 键的情况"""
        session = _make_qa_session()
        qa = FlowQA(
            {
                "needReply": False,
                "qaStrategy": "index",
                "options": ["选项0", "选项1"],
                "index_key": "idx",
            }
        )

        result = await qa.invoke({"idx": 1}, session, None)
        print(result)
        assert result["userFields"]["response"] == "选项1"

    @pytest.mark.asyncio
    async def test_chat_history_none(self):
        """测试会话历史为空"""
        qa = FlowQA({"options": ["选项"]})
        session = _make_qa_session()
        session.get_global_state = Mock(return_value=None)

        history = qa.get_latest_chat_history(session)
        assert history == []


if __name__ == "__main__":
    import subprocess
    import sys

    res = subprocess.run([sys.executable, "-m", "pytest", __file__, "-v"])
    sys.exit(res.returncode)
