#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
Contains a collection of utility functions
Updated against upstream v102.1.5.104.001
"""

import ast
import asyncio
import copy
import datetime
import inspect
import json
import operator
import os
import re
from dataclasses import dataclass
from types import AsyncGeneratorType
from typing import (
    Any,
    Generator,
    Union,
    Optional,
    AsyncIterable,
    Dict,
    Tuple,
    AsyncGenerator,
)

from jiuwen.common.configs.env_constants import CONDITION_EXPRESSION_MAX_LEN_KEY
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.orchestration.callbacks.constants import TriggerEventName
from jiuwen.orchestration.callbacks.manager import CallbackHandlerManager
from jiuwen.orchestration.callbacks.tracer import Span
from jiuwen.orchestration.flow.chat_manager import ChatManager
from jiuwen.orchestration.flow.constant import EXCEPTIONENABLE
from jiuwen.orchestration.flow.enum import StreamDataMsg, NodeType
from jiuwen.orchestration.flow.model.workflow_data_class import (
    WorkflowMetadata,
    WorkflowStreamingData,
)
from jiuwen.orchestration.flow.runtime_context import RuntimeContext
from jiuwen.orchestration.flow.stream.base import StreamCode, StreamData, SseStreamData
from jiuwen.orchestration.flow.string_utils import get_ref_str, split_string
from pydantic import ValidationError

PROPOSE_NEXT = "propose_next"
ANSWER = "answer"
ORIGIN_ANSWER = "origin_answer"
OUTPUTMODE = "output_mode"
THINK = "think"
ORIGIN_MESSAGE = "original_message"
USER_FIELDS = "user_fields"
OUTPUTS = "outputs"
SPLIT_DELIMITER = "\001"
BJ_TZ = datetime.timezone(
    datetime.timedelta(hours=8),
    name="Asia/Beijing",
)

RULES = [
    (re.compile(r"&&"), "and"),
    (re.compile(r"\|\|"), "or"),
    (re.compile(r"\btrue\b"), "True"),
    (re.compile(r"\bfalse\b"), "False"),
    (re.compile(r"\blength\("), "len("),
    (re.compile(r"\bnot_in\b"), "not in"),
    (re.compile(r"is_empty\("), "_safe_is_empty("),
    (re.compile(r"is_not_empty\("), "_safe_is_not_empty("),
]

"""
将新节点类型值映射到旧节点类型的字典。

此字典用于将NodeType枚举类的值转换为对应的旧节点类型名称。
"""

CONDITION_EXPRESSION_PATTERN = re.compile(
    r"^\s*(\$\{[^{}]*\}|'[^']*'|\"[^\"]*\"|\S+)\s+(not_in|in|==|!=)\s+(\$\{[^{}]*\}|'[^']*'|\"[^\"]*\"|\S+)\s*$"
)


class EvalResult:
    """统一的返回值包装类"""

    def __init__(self, value: Optional[Any]):
        self._value = value

    @property
    def value(self):
        """value"""
        return self._value


async def pass_streaming_data_without_finish(
    generator: Generator,
    workflow_streaming_data: WorkflowStreamingData,
    metadata: WorkflowMetadata,
    begin_index: int = 0,
    output_mode=None,
):
    """将生成器非finish以及error结果写入到StreamWriter中，并返回最终结果"""
    final_res = ""
    final_think = ""
    for item in generator:
        # 模型的生成器中数据的node_id与node_type需要改为引用模型输出的节点的node_id与node_type
        if ORIGIN_MESSAGE in item.data:
            # api返回结果需要增加origin_message
            item.data = get_data_of_streaming_with_metadata_api(
                SseStreamData(**json.loads(item.data.get(ORIGIN_MESSAGE))),
                metadata=metadata,
            )
        else:
            answer, struct_answer = (
                (item.data.get(ORIGIN_ANSWER), item.data.get(ANSWER))
                if item.data.get(ORIGIN_ANSWER, "")
                else (item.data.get(ANSWER), "")
            )
            enable_history = item.data.get("enable_history", True)
            item.data = get_data_of_streaming_with_metadata(
                answer,
                metadata=metadata,
                outputs=item.data.get(OUTPUTS, {}).get(USER_FIELDS),
                output_mode=item.data.get("output_mode"),
                struct_answer=struct_answer,
            )
            answer = item.data.get(ANSWER)
            item.data = get_data_of_streaming_with_metadata(
                answer,
                metadata=metadata,
                outputs=item.data.get(OUTPUTS, {}).get(USER_FIELDS),
                struct_answer=struct_answer,
            )
            if item.code == StreamCode.MESSAGE_END.value:
                item.data["enable_history"] = enable_history
            if metadata.node_type == NodeType.QA.value:
                answer_item = answer if isinstance(answer, dict) else struct_answer
                answer_key = ANSWER if isinstance(answer, dict) else ORIGIN_ANSWER
                item.data.update(
                    {
                        answer_key: answer_item.get(ANSWER, ""),
                        "enable_history": answer_item.get("enable_history", True),
                        "need_reply": answer_item.get("need_reply", True),
                    }
                )
        item.index = begin_index
        if output_mode is not None:
            item.data["output_mode"] = output_mode
        if item.code == StreamCode.PARTIAL_CONTENT.value:
            final_res += str(item.data.get(ANSWER))
            final_think += str(item.data.get("think", ""))
        if item.code in [StreamCode.FINISH.value, StreamCode.ERROR.value]:
            final_res = (
                final_res + SPLIT_DELIMITER + final_think if final_think else final_res
            )
            return final_res, begin_index
        await workflow_streaming_data.stream_callback.on_recv(item)
        begin_index += 1


async def pass_async_streaming_data_without_finish(
    async_generator,
    workflow_streaming_data: WorkflowStreamingData,
    metadata: WorkflowMetadata,
    begin_index: int = 0,
    output_mode=None,
    key=None,
    ref=None,
):
    """将异步生成器非finish以及error结果写入到StreamWriter中，并返回最终结果"""
    final_res = ""
    async for item in async_generator:
        # 模型的生成器中数据的node_id与node_type需要改为引用模型输出的节点的node_id与node_type
        item = process_node_reference(item, key, ref)
        if ORIGIN_MESSAGE in item.data:
            # api返回结果需要增加origin_message
            origin_message = item.data.get(ORIGIN_MESSAGE)
            if isinstance(origin_message, str):
                try:
                    parsed_data = json.loads(origin_message)
                    # 确保解析后是字典类型
                    if not isinstance(parsed_data, dict):
                        raise ValueError("Invalid data format")
                    item.data = get_data_of_streaming_with_metadata_api(
                        SseStreamData(**parsed_data), metadata=metadata
                    )
                except (json.JSONDecodeError, TypeError) as e:
                    # 解析失败时使用安全默认值
                    logger.warning(
                        "Failed to parse origin message: %s", type(e).__name__
                    )
                    item.data = get_data_of_streaming_with_metadata_api(
                        SseStreamData(), metadata=metadata
                    )
            else:
                # 非字符串类型直接使用空数据
                item.data = get_data_of_streaming_with_metadata_api(
                    SseStreamData(), metadata=metadata
                )
        else:
            item.data = get_data_of_streaming_with_metadata(
                item.data.get(ANSWER),
                metadata=metadata,
                think=item.data.get("think", ""),
                outputs=item.data.get(OUTPUTS, {}).get(USER_FIELDS),
            )
        item.index = begin_index
        if output_mode is not None:
            item.data["output_mode"] = output_mode

        if item.code == StreamCode.PARTIAL_CONTENT.value:
            final_res += str(item.data.get(ANSWER))
        if item.code in [StreamCode.FINISH.value, StreamCode.ERROR.value]:
            return final_res, begin_index
        await workflow_streaming_data.stream_callback.on_recv(item)
        begin_index += 1

    return final_res, begin_index


def process_node_reference(item, key, ref):
    """处理节点引用逻辑"""
    # 先判断前置条件，不符合直接返回原item
    if item.code == StreamCode.FINISH.value:
        return item
    if not (key and ref):
        return item

    # 只有key和ref都存在时才检查node_id
    if item.data.get("node_id") != "node_plugin":
        return item

    # 符合所有条件，才进行深拷贝和处理
    current_item = copy.deepcopy(item)

    ref_path = get_ref_str(ref["userFields"][key])
    if not ref_path:
        return current_item

    path_list = split_string(ref_path)
    if (
        len(path_list) >= 2
        and path_list[0] == "node_plugin"
        and path_list[1] == "userFields"
    ):
        path_list = path_list[2:]

    original_answer = current_item.data.get("answer", {})
    if not path_list:
        return current_item

    value = original_answer
    for p in path_list:
        if isinstance(value, dict) and p in value:
            value = value.get(p)
        else:
            value = None
            break

    current_item.data["answer"] = {path_list[-1]: value}
    return current_item


async def flow_card_pass_async_streaming_data_without_finish(
    async_generator: AsyncGenerator,
    workflow_streaming_data: WorkflowStreamingData,
    metadata: WorkflowMetadata,
    begin_index: int = 0,
    output_mode: Optional[str] = None,
    output_name: Optional[str] = None,
    instance_id: Optional[str] = None,
) -> Tuple[str, int]:
    """
    消费卡片节点中的异步生成器中的流式片段（仅 PARTIAL_CONTENT），转换为卡片格式并发送。
    累积 answer 内容作为最终结果，不处理 FINISH/ERROR（由调用方统一结束）。

    返回：(累积的最终 answer 字符串, 下一个可用的 index)
    """
    final_answer = ""
    current_index = begin_index

    async for item in async_generator:
        # 只处理 PARTIAL_CONTENT 的流式数据
        if item.code != StreamCode.PARTIAL_CONTENT.value:
            # 非流数据（如 FINISH/ERROR）跳过，由外层统一处理结束
            continue

        answer_chunk = item.data.get(ANSWER, "")
        if not answer_chunk:
            continue

        # 累积最终结果
        final_answer += str(answer_chunk)

        # 构造卡片流式上下文
        card_context = {
            output_name: {
                "stream": "true",
                "answer": answer_chunk,
            }
        }

        flow_card_data = {
            "card": {"context": card_context},
            "instance_id": instance_id,
            "answer": "",
        }

        # 构造新的流数据
        wrapped_data = flow_card_get_data_of_streaming_with_metadata(
            data=flow_card_data,
            metadata=metadata,
        )
        if output_mode is not None:
            wrapped_data["output_mode"] = output_mode

        stream_item = StreamData(
            code=StreamCode.PARTIAL_CONTENT.value,
            msg=item.msg,  # 保留原始 msg
            data=wrapped_data,
            execution_id=workflow_streaming_data.execution_id,
            index=current_index,
        )

        await workflow_streaming_data.stream_callback.on_recv(stream_item)
        current_index += 1

    return final_answer, current_index


async def pass_streaming_data_with_finish(
    generator: Generator,
    workflow_streaming_data: WorkflowStreamingData,
    metadata: WorkflowMetadata,
):
    """将生成器所有结果写入到StreamWriter中，并返回最终结果"""
    for item in generator:
        # 模型的生成器中数据的node_id与node_type需要改为引用模型输出的节点的node_id与node_type
        item.data = get_data_of_streaming_with_metadata(
            item.data.get(ANSWER), metadata=metadata
        )
        await workflow_streaming_data.stream_callback.on_recv(item)
        if item.code in [StreamCode.FINISH.value, StreamCode.ERROR.value]:
            return item.data.get(ANSWER)


async def process_generator_values_of_dict(
    origin_template: str,
    inputs: dict,
    workflow_streaming_data: WorkflowStreamingData,
    metadata: WorkflowMetadata,
    output_mode=None,
    ref=None,
):
    """
    处理消息以及结束节点的执行回复模板：
    若模板中引用的变量值为生成器类型，需要进行遍历传递。
    节点执行完毕后需要在最后添加一个message_end结束标识。
    """
    response_list = process_specified_reply(origin_template)
    index = 0
    # 记录流式返回值，防止多次引用同一个流，导致第二次读流失败
    flow_record = {}
    for res in response_list:
        if res.startswith("{{") and res.endswith("}}"):
            param_name = res[2:-2]
            param_value = inputs.get(param_name)
            if param_value is None:
                continue
            # 检查是否为生成器或异步生成器/异步迭代器
            is_async_generator = (
                isinstance(param_value, (AsyncGeneratorType, AsyncIterable))
                or inspect.isasyncgen(param_value)
                or hasattr(param_value, "__aiter__")
            )

            if isinstance(param_value, Generator) or is_async_generator:
                generator_identifier = id(param_value)
                if generator_identifier not in flow_record:
                    # 处理异步迭代器
                    if is_async_generator:
                        # 处理异步生成器/异步迭代器
                        (
                            inputs[param_name],
                            index,
                        ) = await pass_async_streaming_data_without_finish(
                            async_generator=param_value,
                            workflow_streaming_data=workflow_streaming_data,
                            metadata=metadata,
                            begin_index=index,
                            output_mode=output_mode,
                            key=param_name,
                            ref=ref,
                        )
                    else:
                        # 处理同步生成器（原有逻辑）
                        (
                            inputs[param_name],
                            index,
                        ) = await pass_streaming_data_without_finish(
                            generator=param_value,
                            workflow_streaming_data=workflow_streaming_data,
                            metadata=metadata,
                            begin_index=index,
                            output_mode=output_mode,
                        )
                    flow_record[generator_identifier] = param_name
                else:
                    inputs[param_name] = inputs.get(flow_record[generator_identifier])
                    await workflow_streaming_data.stream_callback.on_recv(
                        StreamData(
                            code=StreamCode.PARTIAL_CONTENT.value,
                            msg=StreamDataMsg.SUCCESS.value,
                            data=get_data_of_streaming_with_metadata(
                                answer=inputs[param_name],
                                metadata=metadata,
                                output_mode=output_mode,
                            ),
                            execution_id=workflow_streaming_data.execution_id,
                            index=index,
                        )
                    )
            else:
                # 值不是生成器的话，直接返回一条流式数据
                await workflow_streaming_data.stream_callback.on_recv(
                    StreamData(
                        code=StreamCode.PARTIAL_CONTENT.value,
                        msg=StreamDataMsg.SUCCESS.value,
                        data=get_data_of_streaming_with_metadata(
                            answer=str(param_value),
                            metadata=metadata,
                            output_mode=output_mode,
                        ),
                        execution_id=workflow_streaming_data.execution_id,
                        index=index,
                    )
                )
                index += 1
        else:
            cur_stream_data = StreamData(
                code=StreamCode.PARTIAL_CONTENT.value,
                msg=StreamDataMsg.SUCCESS.value,
                data=get_data_of_streaming_with_metadata(
                    answer=res, metadata=metadata, output_mode=output_mode
                ),
                execution_id=workflow_streaming_data.execution_id,
                index=index,
            )
            await workflow_streaming_data.stream_callback.on_recv(cur_stream_data)
            index += 1
    return inputs


@dataclass
class ParamModel:
    """卡片节点的引用变量"""

    param_name: str
    param_type: str  # "String", "SyncGenerator", "AsyncGenerator"
    param_value: Any


async def flow_card_process_generator_values_of_dict(
    origin_template: str,
    inputs: dict,
    workflow_streaming_data,
    metadata,
    output_mode=None,
    card_metadata=None,
):
    """
    处理卡片节点的模板中引用的变量，若为生成器则流式消费并返回。
    分阶段发送：元信息 → 初始上下文（生成器占位）→ 流数据 → 结束包。
    """
    # 1: 解析模板并分类参数
    response_dict = json.loads(origin_template)
    param_models: Dict[str, ParamModel] = {}
    flow_record = {}  # 生成器去重：id -> param_name

    for key, placeholder in response_dict.items():
        if not (placeholder.startswith("{{") and placeholder.endswith("}}")):
            continue
        var_name = placeholder[2:-2]
        value = inputs.get(var_name)
        if value is None:
            continue

        if isinstance(value, Generator):
            param_type = "SyncGenerator"
        elif (
            isinstance(value, (AsyncGeneratorType, AsyncIterable))
            or inspect.isasyncgen(value)
            or hasattr(value, "__aiter__")
        ):
            param_type = "AsyncGenerator"
        else:
            param_type = "String"

        param_models[key] = ParamModel(var_name, param_type, value)

    # 2: 发送初始包
    index = 0
    initial_context = _build_context(param_models, inputs, is_final=False)
    index_1_data = {
        "card": {"context": initial_context},
        "metaData": card_metadata.get("config_metadata"),
        "pack": card_metadata.get("pack"),
        "instance_id": card_metadata.get("instance_id"),
        "card_code": card_metadata.get("card_code"),
        "version_id": card_metadata.get("version"),
        "resource_type": card_metadata.get("resourceType"),
        "resource_url": card_metadata.get("resourceUrl"),
        "answer": "",
    }
    await _send_stream_data(
        workflow_streaming_data, metadata, output_mode, index, index_1_data
    )
    index += 1

    # 3: 消费所有生成器并流式发送
    for key, param in param_models.items():
        if param.param_type == "String":
            continue

        gen_id = id(param.param_value)
        if gen_id in flow_record:
            # 已消费过：复用结果（避免重复消费生成器）
            inputs[param.param_name] = inputs[flow_record[gen_id]]
            continue

        if param.param_type == "AsyncGenerator":
            (
                final_value,
                index,
            ) = await flow_card_pass_async_streaming_data_without_finish(
                async_generator=param.param_value,
                workflow_streaming_data=workflow_streaming_data,
                metadata=metadata,
                begin_index=index,
                output_mode=output_mode,
                output_name=key,
                instance_id=card_metadata.get("instance_id"),
            )
            inputs[param.param_name] = final_value
        elif param.param_type == "SyncGenerator":
            # 同步生成器处理（包装为异步）
            final_value, index = await _consume_sync_generator_and_stream(
                sync_generator=param.param_value,
                workflow_streaming_data=workflow_streaming_data,
                metadata=metadata,
                begin_index=index,
                output_mode=output_mode,
                instance_id=card_metadata.get("instance_id"),
            )
            inputs[param.param_name] = final_value

        flow_record[gen_id] = param.param_name

    # 4: 发送结束包(含完整 context)
    final_context = _build_context(param_models, inputs, is_final=True)
    end_data = {
        "card": {"context": final_context},
        "instance_id": card_metadata.get("instance_id"),
        "answer": str(final_context),
    }
    await _send_stream_data(
        workflow_streaming_data, metadata, output_mode, index, end_data, is_end=True
    )

    return inputs


def _build_context(
    param_models: Dict[str, ParamModel], inputs: dict, is_final: bool
) -> dict:
    """统一构建 卡片节点消息context"""
    context = {}
    for key, param in param_models.items():
        if param.param_type == "String":
            context[key] = param.param_value
        elif is_final:
            # 结束包：填入完整结果
            context[key] = {
                "stream": "true",
                "answer": inputs.get(param.param_name),
            }
        else:
            # 初始包：占位
            context[key] = {"stream": "true", "answer": ""}
    return context


async def _send_stream_data(
    workflow_streaming_data, metadata, output_mode, index, data, is_end=False
):
    code = StreamCode.MESSAGE_END.value if is_end else StreamCode.PARTIAL_CONTENT.value
    msg = StreamDataMsg.MESSAGE_END.value if is_end else StreamDataMsg.SUCCESS.value

    payload = flow_card_get_data_of_streaming_with_metadata(
        data=data, metadata=metadata, output_mode=output_mode
    )

    stream_data = StreamData(
        code=code,
        msg=msg,
        data=payload,
        execution_id=workflow_streaming_data.execution_id,
        index=index,
    )
    await workflow_streaming_data.stream_callback.on_recv(stream_data)


async def _consume_sync_generator_and_stream(
    sync_generator: Generator,
    workflow_streaming_data: WorkflowStreamingData,
    metadata: WorkflowMetadata,
    begin_index: int = 0,
    output_mode: Optional[str] = None,
    output_name: Optional[str] = None,
    instance_id: Optional[str] = None,
) -> Tuple[str, int]:
    """
    将同步生成器中的流式片段（字符串 chunk）转换为卡片格式并通过异步回调发送。
    仅处理有效字符串 chunk，不处理异常或结束信号（由外层统一处理）。
    返回：(累积的完整 answer, 下一个可用的 index)
    """
    final_answer = ""
    current_index = begin_index

    # 使用线程安全队列在线程和事件循环之间传递数据
    queue: asyncio.Queue = asyncio.Queue()
    loop = asyncio.get_running_loop()

    def _consume_in_thread():
        """在独立线程中消费同步生成器，并将 chunk 放入队列"""
        try:
            for chunk in sync_generator:
                if chunk is None:
                    continue
                chunk_str = str(chunk) if not isinstance(chunk, str) else chunk
                if chunk_str:
                    loop.call_soon_threadsafe(queue.put_nowait, chunk_str)
        except Exception:
            logger.warning("Card consume sync generator failed", exc_info=True)
        finally:
            loop.call_ssoon_threadsafe(queue.put_nowait, None)

    # 启动后台线程消费同步生成器
    thread = asyncio.to_thread(_consume_in_thread)
    asyncio.create_task(
        thread
    )  # 或用 loop.run_in_executor，但 asyncio.to_thread 更简洁

    # 异步消费队列中的 chunk
    while True:
        chunk = await queue.get()
        if chunk is None:
            break  # 生成器结束

        final_answer += chunk

        # 构造卡片流式上下文
        card_context = {
            output_name: {
                "stream": "true",
                "answer": chunk,
            }
        }

        flow_card_data = {
            "card": {"context": card_context},
            "instance_id": instance_id,
            "answer": "",
        }

        wrapped_data = flow_card_get_data_of_streaming_with_metadata(
            data=flow_card_data,
            metadata=metadata,
        )
        if output_mode is not None:
            wrapped_data["output_mode"] = output_mode

        stream_item = StreamData(
            code=StreamCode.PARTIAL_CONTENT.value,
            msg=StreamDataMsg.SUCCESS.value,
            data=wrapped_data,
            execution_id=workflow_streaming_data.execution_id,
            index=current_index,
        )

        await workflow_streaming_data.stream_callback.on_recv(stream_item)
        current_index += 1

    return final_answer, current_index


async def send_common_stream_data(
    workflow_streaming_data: WorkflowStreamingData,
    answer: str,
    metadata: WorkflowMetadata,
    index: int,
):
    """发送一条正常的中间流式数据"""
    await workflow_streaming_data.stream_callback.on_recv(
        StreamData(
            code=StreamCode.PARTIAL_CONTENT.value,
            msg=StreamDataMsg.SUCCESS.value,
            data=get_data_of_streaming_with_metadata(answer=answer, metadata=metadata),
            execution_id=workflow_streaming_data.execution_id,
            index=index,
        )
    )


def wrap_str_to_generator_with_finish(
    output: str, struct_output: str, metadata: WorkflowMetadata, execution_id: str
):
    """将一个字符串封装为带结束信息的生成器"""
    yield StreamData(
        code=StreamCode.PARTIAL_CONTENT.value,
        msg=StreamDataMsg.SUCCESS.value,
        execution_id=execution_id,
        data=get_data_of_streaming_with_metadata(answer=output, metadata=metadata),
    )
    yield StreamData(
        code=StreamCode.MESSAGE_END.value,
        msg=StreamDataMsg.MESSAGE_END.value,
        execution_id=execution_id,
        data=get_data_of_streaming_with_metadata(
            answer=output, metadata=metadata, struct_answer=struct_output
        ),
        is_struct_message=bool(struct_output),
    )
    yield StreamData(
        code=StreamCode.FINISH.value,
        msg=StreamDataMsg.FINISH.value,
        execution_id=execution_id,
        data=get_data_of_streaming_with_metadata(answer=output, metadata=metadata),
    )


def get_data_of_streaming_with_metadata(
    answer: Union[str, dict],
    metadata: WorkflowMetadata,
    think: str = "",
    outputs: dict = None,
    output_mode: Optional[str] = None,
    struct_answer: str = "",
) -> dict:
    """
    封装流式输出的结果，包括原始输出、节点ID、节点类型
    """
    base = dict(
        answer=answer,
        node_id=metadata.node_id,
        node_name=metadata.node_name,
        node_type=metadata.node_type,
        should_interrupt=metadata.should_interrupt,
    )
    if struct_answer:
        base[ORIGIN_ANSWER] = struct_answer
    if think:
        base["think"] = think
    if output_mode is not None:
        base["output_mode"] = output_mode
    return base | ({OUTPUTS: {USER_FIELDS: outputs}} if outputs else {})


def flow_card_get_data_of_streaming_with_metadata(
    data: Union[str, dict],
    metadata: WorkflowMetadata,
    think: str = "",
    outputs: dict = None,
    output_mode: Optional[str] = None,
    struct_answer: str = "",
) -> dict:
    """
    封装卡片节点的流式输出的结果，包括原始输出、节点ID、节点类型
    """
    base = dict(
        node_id=metadata.node_id,
        node_name=metadata.node_name,
        node_type=metadata.node_type,
    )
    base = base | data
    if struct_answer:
        base[ORIGIN_ANSWER] = struct_answer
    if think:
        base["think"] = think
    if output_mode is not None:
        base["output_mode"] = output_mode
    return base | ({OUTPUTS: {USER_FIELDS: outputs}} if outputs else {})


def get_data_of_streaming_with_metadata_api(
    answer: SseStreamData, metadata: WorkflowMetadata
) -> dict:
    """
    封装插件流式输出的结果，包括原始输出、节点ID、节点类型、以及原始结果（包括扩展内容）
    """
    return dict(
        answer=answer.content,
        node_id=metadata.node_id,
        node_name=metadata.node_name,
        node_type=metadata.node_type,
        original_message=answer.json(),
    )


async def process_output_of_interaction_component(
    stream_related_info: WorkflowStreamingData,
    origin_output: str,
    chat_manager: ChatManager,
    metadata: WorkflowMetadata,
    struct_answer: str = None,
):
    """
    如果要求流式输出：
    封装交互式组件的中间结果为流式输出。
    如果要求非流式输出：
    直接操作异步队列即可。
    """
    if not stream_related_info.stream_callback:
        await chat_manager.put_out(origin_output)
    else:
        res_generator = wrap_str_to_generator_with_finish(
            output=origin_output,
            metadata=metadata,
            execution_id=stream_related_info.execution_id,
            struct_output=struct_answer,
        )
        origin_output = await pass_streaming_data_without_finish(
            generator=res_generator,
            workflow_streaming_data=stream_related_info,
            metadata=metadata,
        )
    return origin_output


def process_specified_reply(template):
    """第一次获取"""
    if template is None:
        return []
    """
    切分指定回复模板。
    example：
        这是结果：{{result}}   ->    ["这是结果："，"{{result}}"]
    """
    # 定义正则表达式：匹配由双大括号 {{}} 包裹的内容
    pattern = re.compile(r"(\{\{[^{}]*?\}\})")
    # 使用正则表达式 split 函数来拆分字符串
    return pattern.split(template)


async def process_on_invoke_info(
    component_metadata: dict,
    runtime_context: Optional[RuntimeContext],
    data: dict,
    span: Span,
):
    """
    Processes intermediate results of multiple rounds of interaction components.
    """
    await CallbackHandlerManager().trigger_event(
        event_name=TriggerEventName.ON_INVOKE.value,
        data=data,
        component_metadata=component_metadata,
        runtime_context=runtime_context,
        span_instance=span,
    )


def format_interaction_res(
    result: str,
    node_type: str,
    meta_data: dict = None,
    message_outputs: list = None,
    card_outputs: list = None,
    user_fields: dict = None,
):
    """
    format output of the interact node
    the format is :
            {
              "answer": "object | null", // 最终结果 (如果还未完成则为空)
              "result_metadata": {
                "node_type": "string", 必选// 结果来源的节点类型
                "node_name": "string" 可选// 结果来源的节点名称
                "tip_content": "string" 可选// 结果来源的提示信息
              },
              "message_outputs": [          可选// 组件中间执行结果
                {
                  "node_type": "string",
                  "node_name": "string", // 组件的名称
                  "output": "object | null", // 组件的输出数据(如果有的话)
                  "time_stamp": "string"  //时间戳
                },
              "card_outputs": [            可选// 卡片组件中间执行结果
                {
                  "node_type": "string",
                  "node_name": "string", // 组件的名称
                  "output": "object | null", // 组件的输出数据(如果有的话)
                  "time_stamp": "string"  //时间戳
                }
              ],
            }
    """
    result_metadata = dict(node_type=node_type)
    if meta_data and isinstance(meta_data, dict):
        result_metadata.update(meta_data)
    base = dict(
        answer=result,
        user_fields=user_fields,
        result_metadata=result_metadata,
        message_outputs=message_outputs,
        card_outputs=card_outputs,
    )
    return base


def split_condition_expressions(expressions):
    """
    split condition expression use '&&' or '||', keep && or || in result list
    """
    if not expressions:
        return []
    pattern = r"\s?(&&|\|\|)\s?"
    return re.split(pattern, expressions)


def split_one_condition_expression(expression):
    """
    split one condition expression to left、mid and right value
    Define regex patterns for both formats with various operators
    left and right value format can be 'xxx_value' or ${start_node.query} format, mid is ==, !=, in or not_in
    """
    if not expression or len(expression) > int(
        os.getenv(CONDITION_EXPRESSION_MAX_LEN_KEY, 512)
    ):
        logger.error("invalid condition expression")
        return None, None, None
    match = CONDITION_EXPRESSION_PATTERN.match(expression)
    if match:
        first = match.group(1)  # Includes quotes or ${}
        match_operator = match.group(2)
        third = match.group(3)  # Includes quotes or ${}
        return first, match_operator, third

    return None, None, None


def format_pydantic_validation_error_message(validation_error: ValidationError):
    """
    格式化组件初始化时抛出的pydantic错误信息。

    该函数用于处理pydantic的ValidationError，并将其格式化为更具可读性的字符串形式，
    以便于调试和错误追踪。

    Args:
        validation_error (ValidationError): 由pydantic抛出的验证错误对象。

    Returns:
        str: 格式化后的错误信息字符串，包含错误位置和错误消息。

    Raises:
        无

    Examples:
        format_pydantic_validation_error_message(validation_error)
    """
    formatted_err_message_list = []
    for err in validation_error.errors():
        err_loc = map(str, err["loc"])
        formatted_err_message_list.append(f"{'.'.join(err_loc)}: {err['msg']}")
    return "reason: [{}]".format("; ".join(formatted_err_message_list))


def format_node_info_with_metadata(metadata: WorkflowMetadata):
    """
    格式化节点元信息
    """
    return "[node_name: {} | node_type: {}]".format(
        metadata.node_name, metadata.node_type
    )


def find_references(expression):
    """
    match all ${} like reference
    """
    if not expression:
        return []
    pattern = r"\$\{[^{}]+\}"
    return re.findall(pattern, expression)


def filter_enable_history(chat_history):
    """
    过滤enable_history为False的话
    """
    filtered_history = []
    for his in chat_history:
        enable_history = his.get("enable_history", True)
        if enable_history:
            filtered_history.append(his)
    return filtered_history


# 判断当前是否需要忽略异常返回默认值
def _err_ignore(conf):
    exception_enable = conf.get(EXCEPTIONENABLE)
    if exception_enable is not None and exception_enable:
        return True
    return False


# 解析成json格式
def _load_json(input_data):
    """解析json"""
    if isinstance(input_data, dict):
        return input_data

    if not isinstance(input_data, str):
        return {}

    try:
        return json.loads(input_data)
    except json.JSONDecodeError as e:
        logger.error(
            f"JSON decode failed: {e.msg} at position {e.pos}",
            simple_log=f"JSON decode failed at position {e.pos}",
        )
        return {}


def get_callback_data(
    invokable_metadata: WorkflowMetadata, conversation_id="", workflow_id=""
):
    """
    获取组件元信息
    """
    return dict(
        conversation_id=conversation_id,
        current_time=datetime.datetime.now(tz=BJ_TZ),
        on_invoke_data=[],  # 用于记录当前组件执行时的中间过程信息
        # 元数据信息
        agent_id=workflow_id,
        component_id=invokable_metadata.node_id,
        component_name=invokable_metadata.node_name,
        component_type=convert_to_old_type.get(invokable_metadata.node_type),
    )


class ObjTrans:
    """
    Common object conversion class.
    """

    def __init__(self, **kwargs):
        for key, value in kwargs.items():
            setattr(self, key, value)


def convert_condition(condition, inputs):
    """
    convert condition inputs
    """
    # 先替换变量占位符
    condition = re.sub(r"\$\{node\.user\.(\w+)\}", r'node.user["\1"]', condition)

    # 分离字符串和非字符串部分
    parts = re.split(r"('(?:[^'\\]|\\.)*'|\"(?:[^\"\\\\]|\\\\.)*\")", condition)

    for i, part in enumerate(parts):
        # 只处理非字符串部分
        if i % 2 == 0:  # 非字符串部分
            # 逐个应用规则替换
            for pattern, replacement in RULES:
                part = pattern.sub(replacement, part)
            parts[i] = part

    return "".join(parts)


# 判断方法
def evaluate_expression(expression: str, inputs: dict, input_logger) -> bool:
    """
    Evaluate the boolean expression using the provided inputs with AST.

    Args:
        expression (str): The boolean expression to evaluate.
        inputs (dict): The input data for variable substitution in the expression.

    Returns:
        bool: The result of the evaluated expression.
    """

    processed_expression = convert_condition(expression, inputs)

    var_pattern = r"\$\{([^{}]*)\}"
    var_mapping = {}
    for i, match in enumerate(re.findall(var_pattern, processed_expression)):
        full_match = f"${{{match}}}"
        safe_var_name = f"var_{i}"
        var_mapping[full_match] = safe_var_name

    for full_match, safe_var_name in sorted(
        var_mapping.items(), key=lambda x: len(x[0]), reverse=True
    ):
        processed_expression = processed_expression.replace(full_match, safe_var_name)

    # Prepare a safe evaluation context
    context = {
        "inputs": inputs,
        "len": _safe_len,
        "bool": bool,
        "not": operator.not_,
        "and": operator.and_,
        "or": operator.or_,
        "in": operator.contains,
        "sum": sum,
        "_safe_is_empty": _safe_is_empty,
        "_safe_is_not_empty": _safe_is_not_empty,
        "is_empty": _safe_is_empty,
        "is_not_empty": _safe_is_not_empty,
    }

    for full_match, safe_var_name in var_mapping.items():
        if full_match in inputs:
            context[safe_var_name] = inputs[full_match]

    try:
        # Compile the expression
        tree = ast.parse(processed_expression, mode="eval")
        return _evaluate_ast(tree.body, context)

    except SyntaxError as e:
        input_logger.error("Syntax error in expression")
        raise JiuWenBaseException(
            error_code=StatusCode.WORKFLOW_BRANCH_SYNTAX_ERROR.code,
            message=StatusCode.WORKFLOW_BRANCH_SYNTAX_ERROR.errmsg,
        ) from e
    except JiuWenBaseException as e:
        expression_length = len(expression) if expression else 0
        input_logger.error(
            f"Error evaluating expression: length={expression_length}, error_type={type(e).__name__}"
        )
        raise e
    except Exception as e:
        expression_length = len(expression) if expression else 0
        input_logger.error(
            f"Error evaluating expression: length={expression_length}, error_type={type(e).__name__}"
        )
        raise JiuWenBaseException(
            error_code=StatusCode.WORKFLOW_BRANCH_EVALUATE_ERROR.code,
            message=StatusCode.WORKFLOW_BRANCH_EVALUATE_ERROR.errmsg.format(
                expression="workflow condition"
            ),
        ) from e


def _evaluate_ast(node: Any, context: dict) -> Optional[Any]:
    """
    Recursively evaluate the AST nodes.

    Args:
        node (Any): The AST node to evaluate.
        context (dict): The context containing input data for evaluation.

    Returns:
        Optional[Any]: The result of the evaluation, which can be any type or None.
    """
    try:
        if isinstance(node, ast.UnaryOp):
            return EvalResult(_evaluate_unary_op(node, context)).value
        if isinstance(node, ast.BoolOp):
            return EvalResult(_evaluate_bool_op(node, context)).value
        if isinstance(node, ast.Compare):
            return EvalResult(_evaluate_compare(node, context)).value
        if isinstance(node, ast.Name):
            return EvalResult(_evaluate_name(node, context)).value
        if isinstance(node, ast.Constant):
            return EvalResult(node.value).value
        if isinstance(node, ast.Subscript):
            return EvalResult(_evaluate_subscript(node, context)).value
        if isinstance(node, ast.Attribute):
            return EvalResult(_evaluate_attribute(node, context)).value
        if isinstance(node, ast.Call):
            return EvalResult(_evaluate_call(node, context)).value
        if isinstance(node, ast.List):
            return EvalResult(_evaluate_list(node, context)).value
        if isinstance(node, ast.Tuple):
            return EvalResult(_evaluate_tuple(node, context)).value
        if isinstance(node, ast.Dict):
            return EvalResult(_evaluate_dict(node, context)).value
        if isinstance(node, ast.Expression):
            return EvalResult(_evaluate_ast(node.body, context)).value
        raise JiuWenBaseException(
            StatusCode.EXPRESSION_CONDITION_EVAL_ERROR.code,
            StatusCode.EXPRESSION_CONDITION_EVAL_ERROR.errmsg.format(
                error_msg=f"Unsupported AST node type: {type(node).__name__}"
            ),
        )
    except JiuWenBaseException:
        raise
    except Exception as e:
        raise JiuWenBaseException(
            StatusCode.EXPRESSION_CONDITION_EVAL_ERROR.code,
            StatusCode.EXPRESSION_CONDITION_EVAL_ERROR.errmsg.format(
                error_msg=f"Error evaluating AST: {str(e)}"
            ),
        ) from e


def _safe_is_empty(value):
    """Safely check if a value is empty"""
    if value is None:
        return True
    try:
        return len(value) == 0
    except (TypeError, AttributeError):
        return False


def _safe_is_not_empty(value):
    """Safely check if a value is not empty"""
    if value is None:
        return False
    try:
        return len(value) > 0
    except (TypeError, AttributeError):
        return False


def _evaluate_unary_op(node: ast.UnaryOp, context: dict) -> Any:
    op = node.op
    if not isinstance(op, ast.Not):
        raise ValueError(f"Not support operator `{op}` in expression.")
    return not _evaluate_ast(node.operand, context)


def _evaluate_bool_op(node: ast.BoolOp, context: dict) -> Any:
    op = node.op
    if isinstance(op, ast.And):
        # Short-circuit evaluation: return immediately once False is found
        for value_node in node.values:
            value = _evaluate_ast(value_node, context)
            if not value:
                return False
        return True

    # Short-circuit evaluation
    for value_node in node.values:
        value = _evaluate_ast(value_node, context)
        if value:
            return True
    return False


def _safe_len(value: Any):
    if value is None:
        logger.warning("Error evaluating expression: length() operate on None value")
        return None

    if not hasattr(value, "__len__"):
        logger.error(
            "Error evaluating expression: length() operate on non-sequence value"
        )
        raise JiuWenBaseException(
            error_code=StatusCode.WORKFLOW_BRANCH_EVALUATE_ERROR.code,
            message=StatusCode.WORKFLOW_BRANCH_EVALUATE_ERROR.errmsg.format(
                expression="length()"
            ),
        )

    leg = len(value)
    return leg


def _evaluate_compare(node: ast.Compare, context: dict) -> Any:
    left = _evaluate_ast(node.left, context)
    for operation, comparator in zip(node.ops, node.comparators):
        right = _evaluate_ast(comparator, context)
        if not _compare_values(left, right, operation):
            return False
    return True


# @override(jiuwen) added nullable variable assignment logic
def _compare_values(left: Any, right: Any, operation: Any) -> Any:
    if isinstance(operation, ast.Eq):
        return left == right
    if isinstance(operation, ast.NotEq):
        return left != right
    # None 除相等判断外 直接判定失败
    if left is None or right is None:
        return False
    if isinstance(operation, ast.Lt):
        return left < right
    if isinstance(operation, ast.LtE):
        return left <= right
    if isinstance(operation, ast.Gt):
        return left > right
    if isinstance(operation, ast.GtE):
        return left >= right
    if isinstance(operation, ast.In):
        return left in right
    if isinstance(operation, ast.NotIn):
        return left not in right
    return False


# @override end


def _evaluate_name(node: ast.Name, context: dict) -> Any:
    return context.get(node.id)


def _evaluate_subscript(node: ast.Subscript, context: dict) -> Any:
    value = _evaluate_ast(node.value, context)
    index = _evaluate_ast(node.slice, context)
    return value[index]


def _evaluate_attribute(node: ast.Attribute, context: dict) -> Any:
    value = _evaluate_ast(node.value, context)
    return getattr(value, node.attr)


def _evaluate_call(node: ast.Call, context: dict) -> Any:
    func = _evaluate_ast(node.func, context)
    args = [_evaluate_ast(arg, context) for arg in node.args]
    if func is None or not callable(func):
        raise ValueError("Function is not defined or not callable.")
    if func not in _whitelist_func:
        raise ValueError(f"Function {func} is not support.")
    return func(*args)


def _evaluate_list(node: ast.List, context: dict) -> Any:
    return [_evaluate_ast(elt, context) for elt in node.elts]


def _evaluate_tuple(node: ast.Tuple, context: dict) -> Any:
    return tuple(_evaluate_ast(elt, context) for elt in node.elts)


def _evaluate_dict(node: ast.Dict, context: dict) -> Any:
    """
    Evaluate a dictionary AST node.
    """
    evaluated_dict = {}
    for key, value in zip(node.keys, node.values):
        evaluated_key = _evaluate_ast(key, context)
        evaluated_value = _evaluate_ast(value, context)
        evaluated_dict[evaluated_key] = evaluated_value
    return evaluated_dict


def validate_ref_recursive(path_list, outputs_structure):
    """
    递归版本 - 验证引用路径是否在outputs结构中存在
    """
    if not path_list or len(path_list) < 2 or path_list[1] != "userFields":
        return False

    def _validate(parts, current, index):
        if index >= len(parts):
            return True

        if not isinstance(current, dict) or parts[index] not in current:
            return False

        return _validate(parts, current[parts[index]], index + 1)

    return _validate(path_list, outputs_structure.get("userFields", {}), 2)


def _is_empty(value):
    if value is None:
        return True
    if isinstance(value, (str, list, dict, tuple, set)):
        return len(value) == 0
    return False


def _is_not_empty(value):
    return not _is_empty(value)


_whitelist_func = [
    len,
    bool,
    _is_empty,
    _is_not_empty,
    operator.not_,
    operator.and_,
    operator.or_,
    operator.contains,
    sum,
    _safe_len,
    _safe_is_empty,
    _safe_is_not_empty,
]


convert_to_old_type = {
    NodeType.START.value: "Start",
    NodeType.END.value: "End",
    NodeType.BRANCH.value: "Branch",
    NodeType.LLM.value: "LLM",
    NodeType.TASK.value: "Task",
    NodeType.CODE.value: "Code",
    NodeType.API.value: "Api",
    NodeType.QUESTIONER.value: "Questioner",
    NodeType.EXTRACTOR.value: "Extractor",
    NodeType.INTENT_DETECTION.value: "IntentDetection",
    NodeType.MESSAGE.value: "Message",
    NodeType.ERROR_END.value: "jiuwen.exception",
    NodeType.CARD.value: "Card",
    NodeType.STREAM_TRANSFORM.value: "StreamTransform",
}
