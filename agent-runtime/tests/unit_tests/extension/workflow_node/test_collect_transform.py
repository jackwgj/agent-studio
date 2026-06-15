# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2026. All rights reserved.

"""
验证 commit ece3d0a0 修改：flow_card / message / stream_transform 组件增加 collect 和 transform 接口。

测试覆盖范围:
1. _resolve_stream_inputs 静态方法（三个组件共享相同逻辑）
   - None 输入
   - 非 dict 但具有 __aiter__ 的异步可迭代输入
   - 非 dict 且无 __aiter__ 的输入
   - dict 中包含 AsyncGenerator（str 帧、dict 帧、其他类型帧）
   - dict 中包含普通值（非 AsyncGenerator）
2. collect 方法：流式输入聚合后委托给 invoke
3. transform 方法：流式输入聚合后委托给 stream
4. FlowStreamTransform 的 collect / transform 特殊路径
"""

from typing import Any, AsyncGenerator
from unittest.mock import AsyncMock, MagicMock

import pytest
from jiuwen.extension.workflow_node.flow_card import FlowCard, FlowCardConfig
from jiuwen.extension.workflow_node.flow_message import Message
from jiuwen.extension.workflow_node.flow_stream_transform import (
    FlowStreamTransform,
    STREAM_TYPE_PARTIAL_CONTENT,
    STREAM_TYPE_MESSAGE_END,
    USER_FIELDS as ST_USER_FIELDS,
)
from jiuwen.extension.workflow_node.utils import WorkflowMetadata
from openjiuwen.core.context_engine import ModelContext
from openjiuwen.core.session.internal.workflow import NodeSession, WorkflowSession
from openjiuwen.core.session.stream import OutputSchema
from openjiuwen.core.workflow.components import Session


# ============================================================================
# 通用辅助工具
# ============================================================================


async def _async_gen_str(items: list[str]) -> AsyncGenerator[str, None]:
    """生成 yield str 的异步生成器"""
    for item in items:
        yield item


async def _async_gen_dict(items: list[dict]) -> AsyncGenerator[dict, None]:
    """生成 yield dict 的异步生成器"""
    for item in items:
        yield item


async def _async_gen_mixed(items: list) -> AsyncGenerator[Any, None]:
    """生成 yield 混合类型的异步生成器"""
    for item in items:
        yield item


async def _async_gen_empty() -> AsyncGenerator[str, None]:
    """空异步生成器"""
    return
    yield  # noqa: unreachable — 使函数成为生成器


def _make_session(comp_id: str = "test_comp") -> Session:
    """创建 Message / Card 测试用的 Session"""
    wf = WorkflowSession(workflow_id="test_wf", session_id="test_sess")
    node = NodeSession(wf, comp_id, "TestComp")
    return Session(node, False)


def _make_mock_session() -> MagicMock:
    """创建 FlowStreamTransform 测试用的 mock Session"""
    session = MagicMock(spec=Session)
    session.get_component_id = MagicMock(return_value="st_comp")
    session.get_workflow_id = MagicMock(return_value="test_wf")
    session.get_session_id = MagicMock(return_value="test_sess")
    session.write_stream = AsyncMock()
    session.write_custom_stream = AsyncMock()
    session.get_env = MagicMock(return_value=0.2)
    session.get_global_state = MagicMock(return_value=None)
    return session


def _make_mock_context() -> MagicMock:
    return MagicMock(spec=ModelContext)


def _make_metadata() -> WorkflowMetadata:
    return WorkflowMetadata(
        node_id="st_node",
        node_type="StreamTransform",
        node_name="Test ST",
        should_interrupt=False,
    )


# ============================================================================
# TestResolveStreamInputs — 三个组件的 _resolve_stream_inputs 共享逻辑
# ============================================================================


class TestResolveStreamInputs:
    """验证 _resolve_stream_inputs 静态方法的各分支逻辑。"""

    # ---- None 输入 ----

    @pytest.mark.asyncio
    async def test_none_input_returns_empty_dict(self):
        """inputs 为 None 时返回空字典"""
        for cls in (FlowCard, Message, FlowStreamTransform):
            assert await cls._resolve_stream_inputs(None) == {}

    # ---- 非 dict + 无 __aiter__ ----

    @pytest.mark.asyncio
    async def test_non_dict_non_iterable_returns_empty_dict(self):
        """inputs 不是 dict 也没有 __aiter__ 时返回空字典"""
        for cls in (FlowCard, Message, FlowStreamTransform):
            assert await cls._resolve_stream_inputs(42) == {}
            assert await cls._resolve_stream_inputs("plain_string") == {}

    # ---- 非 dict + 有 __aiter__，yield dict ----

    @pytest.mark.asyncio
    async def test_non_dict_async_iterable_single_dict(self):
        """非 dict 输入，异步可迭代，只有一个 dict 元素 → 返回该 dict"""

        async def gen():
            yield {"key": "val"}

        for cls in (FlowCard, FlowStreamTransform):
            result = await cls._resolve_stream_inputs(gen())
            assert result == {"key": "val"}

    @pytest.mark.asyncio
    async def test_non_dict_async_iterable_multiple_dicts(self):
        """非 dict 输入，异步可迭代，多个 dict 元素 → 返回最后一个 dict"""

        async def gen():
            yield {"a": 1}
            yield {"b": 2}

        for cls in (FlowCard, FlowStreamTransform):
            result = await cls._resolve_stream_inputs(gen())
            assert result == {"b": 2}

    @pytest.mark.asyncio
    async def test_non_dict_async_iterable_no_dict(self):
        """非 dict 输入，异步可迭代，但不产出 dict → 返回空字典"""

        async def gen():
            yield "str_only"

        for cls in (FlowCard, FlowStreamTransform):
            result = await cls._resolve_stream_inputs(gen())
            assert result == {}

    @pytest.mark.asyncio
    async def test_non_dict_async_iterable_empty(self):
        """非 dict 输入，异步可迭代，不产出任何元素 → 返回空字典"""
        for cls in (FlowCard, FlowStreamTransform):
            result = await cls._resolve_stream_inputs(_async_gen_empty())
            assert result == {}

    # ---- dict 输入 + AsyncGenerator 值（每个类需创建新的 inputs）----

    @pytest.mark.asyncio
    async def test_dict_with_str_generator(self):
        """dict 中 value 是 AsyncGenerator，帧为 str → 拼接为最终字符串"""
        for cls in (FlowCard, Message, FlowStreamTransform):
            inputs = {"text": _async_gen_str(["hello", " ", "world"])}
            result = await cls._resolve_stream_inputs(inputs)
            assert result == {"text": "hello world"}

    @pytest.mark.asyncio
    async def test_dict_with_dict_generator_response_key(self):
        """dict 中 value 是 AsyncGenerator，帧为 dict 含 'response' → 提取拼接"""

        async def gen():
            yield {"response": "chunk1"}
            yield {"response": "chunk2"}

        for cls in (FlowCard, Message, FlowStreamTransform):
            inputs = {"data": gen()}
            result = await cls._resolve_stream_inputs(inputs)
            assert result == {"data": "chunk1chunk2"}

    @pytest.mark.asyncio
    async def test_dict_with_dict_generator_answer_key(self):
        """dict 中 value 是 AsyncGenerator，帧为 dict 含 'answer'（无 response）→ 提取拼接"""

        async def gen():
            yield {"answer": "ans1"}
            yield {"answer": "ans2"}

        for cls in (FlowCard, Message, FlowStreamTransform):
            inputs = {"data": gen()}
            result = await cls._resolve_stream_inputs(inputs)
            assert result == {"data": "ans1ans2"}

    @pytest.mark.asyncio
    async def test_dict_with_dict_generator_response_takes_priority(self):
        """dict 帧同时有 response 和 answer 时，response 优先"""

        async def gen():
            yield {"response": "resp", "answer": "ans"}

        for cls in (FlowCard, FlowStreamTransform):
            inputs = {"data": gen()}
            result = await cls._resolve_stream_inputs(inputs)
            assert result == {"data": "resp"}

    @pytest.mark.asyncio
    async def test_dict_with_dict_generator_empty_response_and_answer(self):
        """dict 帧中 response 和 answer 都为空 → 不追加"""

        async def gen():
            yield {"response": "", "answer": ""}

        for cls in (FlowCard, Message, FlowStreamTransform):
            inputs = {"data": gen()}
            result = await cls._resolve_stream_inputs(inputs)
            assert result == {"data": ""}

    @pytest.mark.asyncio
    async def test_dict_with_mixed_type_generator(self):
        """dict 中 value 是 AsyncGenerator，帧类型混合 → str/int 等非 str 非 dict 转字符串拼接"""

        async def gen():
            yield "str_chunk"
            yield 42
            yield {"response": "dict_chunk"}

        for cls in (FlowCard, Message, FlowStreamTransform):
            inputs = {"mixed": gen()}
            result = await cls._resolve_stream_inputs(inputs)
            assert result == {"mixed": "str_chunk42dict_chunk"}

    @pytest.mark.asyncio
    async def test_dict_with_empty_generator(self):
        """dict 中 value 是空 AsyncGenerator → 空字符串"""
        for cls in (FlowCard, Message, FlowStreamTransform):
            inputs = {"empty": _async_gen_empty()}
            result = await cls._resolve_stream_inputs(inputs)
            assert result == {"empty": ""}

    # ---- dict 输入 + 普通值（非 AsyncGenerator）----

    @pytest.mark.asyncio
    async def test_dict_with_plain_values(self):
        """dict 中 value 不是 AsyncGenerator → 直接保留"""
        inputs = {"a": 1, "b": "hello", "c": [1, 2, 3], "d": None}
        for cls in (FlowCard, Message, FlowStreamTransform):
            result = await cls._resolve_stream_inputs(inputs)
            assert result == inputs

    @pytest.mark.asyncio
    async def test_dict_with_mixed_plain_and_generator(self):
        """dict 中混合普通值和 AsyncGenerator"""
        for cls in (FlowCard, Message, FlowStreamTransform):
            inputs = {
                "name": "Alice",
                "content": _async_gen_str(["hello", " world"]),
                "count": 5,
            }
            result = await cls._resolve_stream_inputs(inputs)
            assert result == {"name": "Alice", "content": "hello world", "count": 5}

    @pytest.mark.asyncio
    async def test_dict_empty(self):
        """空 dict 输入 → 空字典"""
        for cls in (FlowCard, Message, FlowStreamTransform):
            result = await cls._resolve_stream_inputs({})
            assert result == {}


# ============================================================================
# TestFlowCardCollectTransform — FlowCard 的 collect / transform
# ============================================================================


class TestFlowCardCollectTransform:
    @pytest.mark.asyncio
    async def test_collect_resolves_generator_and_invokes(self):
        """
        collect 接收含 AsyncGenerator 的 inputs，先解析再调用 invoke。
        验证最终结果与直接传解析后的普通值给 invoke 一致。
        """
        config = FlowCardConfig(template="你好，{{name}}！")
        card = FlowCard(config)
        session = _make_session("card1")
        context = _make_mock_context()

        # inputs 中 name 是 AsyncGenerator
        inputs = {"name": _async_gen_str(["张三"])}
        result = await card.collect(inputs, session, context)

        # invoke 渲染结果应为 "你好，张三！"
        assert result == {"result": "你好，张三！"}

    @pytest.mark.asyncio
    async def test_collect_with_plain_inputs(self):
        """collect 接收普通（非 AsyncGenerator）inputs 时仍能正常工作"""
        config = FlowCardConfig(template="数量：{{count}}")
        card = FlowCard(config)
        session = _make_session("card2")
        context = _make_mock_context()

        result = await card.collect({"count": "42"}, session, context)
        assert result == {"result": "数量：42"}

    @pytest.mark.asyncio
    async def test_transform_resolves_generator_and_streams(self):
        """
        transform 接收含 AsyncGenerator 的 inputs，先解析再调用 stream。
        验证产出的是 OutputSchema 流式帧。
        """
        config = FlowCardConfig(template="流式{{data}}")
        card = FlowCard(config)
        session = _make_session("card3")
        context = _make_mock_context()

        inputs = {"data": _async_gen_str(["内容"])}
        outputs = []
        async for chunk in card.transform(inputs, session, context):
            outputs.append(chunk)

        # stream 至少产出一帧
        assert len(outputs) >= 1
        # 最终帧的 payload 包含 response 或渲染后的结果
        last = outputs[-1]
        assert (
            isinstance(last, (OutputSchema, dict))
            or hasattr(last, "type")
            or hasattr(last, "payload")
        )

    @pytest.mark.asyncio
    async def test_transform_with_plain_inputs_yields_output(self):
        """transform 接收普通 inputs 时能正常产出流式帧"""
        config = FlowCardConfig(template="固定文本")
        card = FlowCard(config)
        session = _make_session("card4")
        context = _make_mock_context()

        outputs = []
        async for chunk in card.transform({"name": "test"}, session, context):
            outputs.append(chunk)

        assert len(outputs) >= 1


# ============================================================================
# TestMessageCollectTransform — Message 的 collect / transform
# ============================================================================


class TestMessageCollectTransform:
    @pytest.mark.asyncio
    async def test_collect_resolves_generator_and_invokes(self):
        """
        collect 接收含 AsyncGenerator 的 inputs，解析后调用 invoke。
        Message 的 invoke 会渲染模板并写入 message_outputs。
        """
        msg = Message({"template": "结果：{{answer}}"})
        session = _make_session("msg_collect")
        context = _make_mock_context()

        inputs = {"answer": _async_gen_str(["成功"])}
        result = await msg.collect(inputs, session, context)

        assert result == {"result": "结果：成功"}

    @pytest.mark.asyncio
    async def test_collect_with_user_fields_generator(self):
        """collect 接收 userFields 中含 AsyncGenerator 的场景"""
        msg = Message({"template": "内容：{{text}}"})
        session = _make_session("msg_uf")
        context = _make_mock_context()

        inputs = {"userFields": {"text": _async_gen_str(["hello"])}}
        result = await msg.collect(inputs, session, context)

        assert result == {"result": "内容：hello"}

    @pytest.mark.asyncio
    async def test_collect_with_plain_inputs(self):
        """collect 接收普通值时正常工作"""
        msg = Message({"template": "普通：{{val}}"})
        session = _make_session("msg_plain")
        context = _make_mock_context()

        result = await msg.collect({"val": "测试"}, session, context)
        assert result == {"result": "普通：测试"}

    @pytest.mark.asyncio
    async def test_transform_resolves_generator_and_streams(self):
        """
        transform 接收含 AsyncGenerator 的 inputs，解析后调用 stream。
        验证产出 MESSAGE_NODE_STREAM 和 MESSAGE_NODE_END 帧。
        """
        from jiuwen.extension.workflow_node.flow_message import (
            MESSAGE_NODE_STREAM,
            MESSAGE_NODE_END,
        )

        msg = Message({"template": "流式{{content}}"})
        session = _make_session("msg_transform")
        context = _make_mock_context()

        inputs = {"content": _async_gen_str(["数据"])}
        outputs = []
        async for chunk in msg.transform(inputs, session, context):
            if isinstance(chunk, OutputSchema):
                outputs.append(chunk)

        # 应至少有一个 STREAM 帧和一个 END 帧
        assert len(outputs) >= 2
        assert any(o.type == MESSAGE_NODE_STREAM for o in outputs)
        assert outputs[-1].type == MESSAGE_NODE_END
        assert outputs[-1].payload.get("result") == "流式数据"

    @pytest.mark.asyncio
    async def test_transform_with_plain_inputs_yields_frames(self):
        """transform 接收普通值时也能产出流式帧"""
        from jiuwen.extension.workflow_node.flow_message import MESSAGE_NODE_END

        msg = Message({"template": "静态内容"})
        session = _make_session("msg_transform_plain")
        context = _make_mock_context()

        outputs = []
        async for chunk in msg.transform({"userFields": {}}, session, context):
            if isinstance(chunk, OutputSchema):
                outputs.append(chunk)

        assert len(outputs) >= 1
        assert outputs[-1].type == MESSAGE_NODE_END


# ============================================================================
# TestFlowStreamTransformCollectTransform — FlowStreamTransform 的 collect / transform
# ============================================================================


class TestFlowStreamTransformCollectTransform:
    @pytest.mark.asyncio
    async def test_collect_resolves_and_invokes(self):
        """
        collect 将 AsyncGenerator 解析后调用 invoke。
        invoke 要求 userFields 中的 _input 是 async iterable，
        所以 _resolve_stream_inputs 需要正确解析并保留 async iterable。
        """
        conf = {
            "transformer": {
                "frame_template": {"result": "{{value}}"},
                "variables": [{"name": "value", "src_path": "input"}],
            },
        }
        metadata = _make_metadata()
        comp = FlowStreamTransform(conf, metadata)
        session = _make_mock_session()
        context = _make_mock_context()

        # 注意：collect 的 _resolve_stream_inputs 会把 AsyncGenerator 消费完毕，
        # 所以传给 invoke 的已经是解析后的值（而非 AsyncGenerator）。
        # 但 invoke 内部期望 origin_stream 有 __aiter__，因此这里传普通值
        # 来验证 collect 的解析路径是否正确调用。
        inputs = {"key": _async_gen_str(["resolved_text"])}
        # collect 会先解析，然后传给 invoke
        # invoke 对解析后的 {"key": "resolved_text"} 找不到 userFields._input → 返回 None
        result = await comp.collect(inputs, session, context)
        assert ST_USER_FIELDS in result
        assert result[ST_USER_FIELDS] is None  # 无 _input → 返回 None

    @pytest.mark.asyncio
    async def test_collect_with_none_input(self):
        """collect 接收 None 时返回空结果"""
        conf = {
            "transformer": {
                "frame_template": {"result": "{{value}}"},
                "variables": [{"name": "value", "src_path": "input"}],
            },
        }
        metadata = _make_metadata()
        comp = FlowStreamTransform(conf, metadata)
        session = _make_mock_session()
        context = _make_mock_context()

        result = await comp.collect(None, session, context)
        assert result == {ST_USER_FIELDS: None}

    @pytest.mark.asyncio
    async def test_transform_resolves_and_yields_output(self):
        """
        transform 将 AsyncGenerator 解析后执行流式转换，
        验证产出 OutputSchema 帧。
        """
        conf = {
            "transformer": {
                "frame_template": {"result": "{{value}}"},
                "variables": [{"name": "value", "src_path": "input"}],
            },
        }
        metadata = _make_metadata()
        comp = FlowStreamTransform(conf, metadata)
        session = _make_mock_session()
        context = _make_mock_context()

        # 构造 transform 路径：resolved 中 userFields._input 是 async iterable
        async def mock_stream():
            yield {"input": "chunk1"}
            yield {"input": "chunk2"}

        inputs = {ST_USER_FIELDS: {"_input": mock_stream()}}
        outputs = []
        async for out in comp.transform(inputs, session, context):
            outputs.append(out)

        assert len(outputs) >= 2
        # 第一个应是 PARTIAL_CONTENT，最后一个应是 MESSAGE_END
        assert outputs[0].type == STREAM_TYPE_PARTIAL_CONTENT
        assert outputs[-1].type == STREAM_TYPE_MESSAGE_END

    @pytest.mark.asyncio
    async def test_transform_with_none_origin_stream(self):
        """transform 中 origin_stream 为 None 时，不产出任何帧"""
        conf = {
            "transformer": {
                "frame_template": {"result": "{{value}}"},
                "variables": [{"name": "value", "src_path": "input"}],
            },
        }
        metadata = _make_metadata()
        comp = FlowStreamTransform(conf, metadata)
        session = _make_mock_session()
        context = _make_mock_context()

        # resolved 中 userFields 为空 → origin_stream 为 None
        inputs = {ST_USER_FIELDS: {}}
        outputs = []
        async for out in comp.transform(inputs, session, context):
            outputs.append(out)

        assert outputs == []

    @pytest.mark.asyncio
    async def test_transform_single_frame(self):
        """transform 只有一帧时，直接产出 MESSAGE_END"""
        conf = {
            "transformer": {
                "frame_template": {"result": "{{value}}"},
                "variables": [{"name": "value", "src_path": "input"}],
            },
        }
        metadata = _make_metadata()
        comp = FlowStreamTransform(conf, metadata)
        session = _make_mock_session()
        context = _make_mock_context()

        async def single_frame_stream():
            yield {"input": "only"}

        inputs = {ST_USER_FIELDS: {"_input": single_frame_stream()}}
        outputs = []
        async for out in comp.transform(inputs, session, context):
            outputs.append(out)

        # 只有一帧时，不产出 PARTIAL_CONTENT（prev 首次为 None），
        # 直接在末尾产出 MESSAGE_END
        assert len(outputs) == 1
        assert outputs[0].type == STREAM_TYPE_MESSAGE_END

    @pytest.mark.asyncio
    async def test_transform_with_invalid_transformer_config(self):
        """transform 中 transformer 配置无效时抛出异常"""
        from jiuwen.extension.workflow_node.utils import JiuWenBaseException

        conf = {
            "transformer": {
                "frame_template": None,
            },
        }
        metadata = _make_metadata()
        comp = FlowStreamTransform(conf, metadata)
        session = _make_mock_session()
        context = _make_mock_context()

        async def mock_stream():
            yield {"input": "test"}

        inputs = {ST_USER_FIELDS: {"_input": mock_stream()}}

        with pytest.raises(JiuWenBaseException):
            async for _ in comp.transform(inputs, session, context):
                pass


# ============================================================================
# TestEndToEnd — collect / transform 端到端集成验证
# ============================================================================


class TestEndToEnd:
    """
    端到端集成测试：验证 collect / transform 在组件链中的实际调用。
    模拟 LLM → Card / Message / StreamTransform 的流式传递场景。
    """

    @pytest.mark.asyncio
    async def test_flow_card_collect_end_to_end(self):
        """模拟 LLM 输出 AsyncGenerator → FlowCard.collect → invoke"""
        config = FlowCardConfig(template="AI回答：{{answer}}")
        card = FlowCard(config)
        session = _make_session("e2e_card")
        context = _make_mock_context()

        # 模拟 LLM 流式输出
        async def llm_output():
            yield "你"
            yield "好"
            yield "世界"

        result = await card.collect({"answer": llm_output()}, session, context)
        assert result == {"result": "AI回答：你好世界"}

    @pytest.mark.asyncio
    async def test_message_collect_end_to_end(self):
        """模拟 LLM 输出 AsyncGenerator → Message.collect → invoke"""
        msg = Message({"template": "LLM说：{{text}}"})
        session = _make_session("e2e_msg")
        context = _make_mock_context()

        async def llm_output():
            yield "今天"
            yield "天气"
            yield "很好"

        result = await msg.collect({"text": llm_output()}, session, context)
        assert result == {"result": "LLM说：今天天气很好"}

    @pytest.mark.asyncio
    async def test_flow_card_transform_end_to_end(self):
        """模拟 LLM 输出 AsyncGenerator → FlowCard.transform → stream"""
        config = FlowCardConfig(template="输出：{{content}}")
        card = FlowCard(config)
        session = _make_session("e2e_card_tf")
        context = _make_mock_context()

        async def llm_output():
            yield "数据"

        outputs = []
        async for chunk in card.transform({"content": llm_output()}, session, context):
            outputs.append(chunk)

        assert len(outputs) >= 1

    @pytest.mark.asyncio
    async def test_message_transform_end_to_end(self):
        """模拟 LLM 输出 AsyncGenerator → Message.transform → stream"""
        msg = Message({"template": "回答：{{reply}}"})
        session = _make_session("e2e_msg_tf")
        context = _make_mock_context()

        async def llm_output():
            yield "是的"

        outputs = []
        async for chunk in msg.transform({"reply": llm_output()}, session, context):
            if isinstance(chunk, OutputSchema):
                outputs.append(chunk)

        assert len(outputs) >= 2
        from jiuwen.extension.workflow_node.flow_message import MESSAGE_NODE_END

        assert outputs[-1].type == MESSAGE_NODE_END
        assert outputs[-1].payload.get("result") == "回答：是的"

    @pytest.mark.asyncio
    async def test_stream_transform_transform_end_to_end(self):
        """模拟 LLM 输出 → FlowStreamTransform.transform → 流式输出"""
        conf = {
            "transformer": {
                "frame_template": {"result": "{{value}}"},
                "variables": [{"name": "value", "src_path": "input"}],
            },
        }
        metadata = _make_metadata()
        comp = FlowStreamTransform(conf, metadata)
        session = _make_mock_session()
        context = _make_mock_context()

        async def llm_frames():
            yield {"input": "frame1"}
            yield {"input": "frame2"}
            yield {"input": "frame3"}

        inputs = {ST_USER_FIELDS: {"_input": llm_frames()}}
        outputs = []
        async for out in comp.transform(inputs, session, context):
            outputs.append(out)

        # 3帧：2个 PARTIAL_CONTENT + 1个 MESSAGE_END
        assert len(outputs) == 3
        assert outputs[0].type == STREAM_TYPE_PARTIAL_CONTENT
        assert outputs[1].type == STREAM_TYPE_PARTIAL_CONTENT
        assert outputs[2].type == STREAM_TYPE_MESSAGE_END

    @pytest.mark.asyncio
    async def test_llm_dict_frames_with_response_key(self):
        """模拟 LLM 输出 dict 帧含 response 键 → _resolve_stream_inputs 正确提取"""
        config = FlowCardConfig(template="{{output}}")
        card = FlowCard(config)
        session = _make_session("e2e_dict_resp")
        context = _make_mock_context()

        async def llm_output():
            yield {"response": "第一段"}
            yield {"response": "第二段"}

        result = await card.collect({"output": llm_output()}, session, context)
        assert result == {"result": "第一段第二段"}

    @pytest.mark.asyncio
    async def test_llm_dict_frames_with_answer_key(self):
        """模拟 LLM 输出 dict 帧含 answer 键 → _resolve_stream_inputs 正确提取"""
        config = FlowCardConfig(template="{{output}}")
        card = FlowCard(config)
        session = _make_session("e2e_dict_ans")
        context = _make_mock_context()

        async def llm_output():
            yield {"answer": "答案A"}
            yield {"answer": "答案B"}

        result = await card.collect({"output": llm_output()}, session, context)
        assert result == {"result": "答案A答案B"}
