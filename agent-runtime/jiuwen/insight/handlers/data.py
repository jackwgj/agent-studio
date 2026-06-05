#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""save data handler"""

from datetime import datetime
from enum import Enum
from typing import Optional, Dict, List, Any

from dateutil.tz import tzlocal
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.insight.handlers.base import (
    PromptTraceHandler,
    LLMTraceHandler,
    PluginTraceHandler,
    ChainTraceHandler,
    RetrieverTraceHandler,
    EvaluatorTraceHandler,
)
from pydantic import BaseModel, model_validator, Field

NAME = "name"


class InvokeData(BaseModel):
    """
    Invoke Data
    """

    invoke_id: str
    parent_invoke_id: Optional[str] = None
    chain_id: str
    invoke_type: Enum
    name: str
    start_time: datetime
    end_time: Optional[datetime] = None
    inputs: Optional[dict]
    outputs: Optional[dict] = None
    error: Optional[dict] = None
    execution_order_index: int
    child_execution_order_index: int
    child_invokes: List["InvokeData"] = []
    serialized: Optional[dict] = None
    extra: Optional[dict] = None

    @classmethod
    @model_validator(mode="before")
    def assign_name(cls, values: dict) -> dict:
        """Assign name to invoke."""
        extra_name = values["extra"].get(NAME)
        instance_attributes_name = values["serialized"]["instance_attributes"].get(NAME)
        if extra_name is not None:
            values[NAME] = extra_name
        elif instance_attributes_name is not None:
            values[NAME] = f"""{values[NAME]}:{instance_attributes_name}"""

        return values


class InvokeData4Callback(BaseModel):
    """
    Invoke Data
    """

    execution_id: str = Field(default="", alias="executionId")
    conversation_id: str = Field(default="", alias="conversationId")
    start_time: datetime = Field(default=None, alias="startTime")
    end_time: Optional[datetime] = Field(default=None, alias="endTime")
    on_invoke_data: List[dict] = Field(
        default=[], alias="onInvokeData"
    )  # 用于记录当前组件执行时的中间过程信息
    agent_id: str = Field(default="", alias="agentId")
    component_id: str = Field(default="", alias="componentId")  # 放到metadata
    component_name: str = Field(default="", alias="componentName")  # 放到metadata
    component_type: str = Field(default="", alias="componentType")  # 即invoke_type
    agent_parent_invoke_id: str = Field(
        default="", alias="agentParentInvokeId"
    )  # 给未来适配workflow节点中嵌套workflow预留
    inputs: Optional[dict] = Field(default=None, alias="inputs")
    outputs: Optional[dict] = Field(default=None, alias="outputs")
    error: Optional[dict] = Field(default=None, alias="error")
    meta_data: Optional[dict] = Field(
        default=None, alias="metaData"
    )  # 包括：模型的输入的function tools信息，模型的token使用信息
    # from span
    invoke_id: str = Field(default=None, alias="invokeId")  # invoke_id
    parent_invoke_id: Optional[str] = Field(
        default=None, alias="parentInvokeId"
    )  # parent_invoke_id
    trace_id: str = Field(default=None, alias="traceId")  #
    # for loop component
    loop_node_id: Optional[str] = Field(default=None, alias="loopNodeId")
    loop_index: Optional[int] = Field(default=None, alias="loopIndex")
    # 用于内部异常重试情况
    inner_error: Optional[dict] = Field(default=None, alias="innerError")
    memory: Optional[dict] = Field(default=None, alias="memory")

    def model_dump(self, *args, **kwargs):
        """重写model_dump方法，在memory为None时排除该字段"""
        result = super().model_dump(*args, **kwargs)
        if "memory" in result and result["memory"] is None:
            del result["memory"]
        return result

    def dict_to_attrs(self, attributes: dict):
        """
        将字典中的键值对更新为类实例的属性。
        :param attributes: 包含要更新的属性名和对应值的字典
        """
        if attributes is not None:
            for key, value in attributes.items():
                if hasattr(self, key):
                    setattr(self, key, value)


class InvokeData4WorkflowNodeMsg(InvokeData4Callback, BaseModel):
    """extend invoke data for node msg"""

    status: str = Field(default="start")


class InvokeData4Agent(BaseModel):
    """
    Agent invoke data
    """

    invoke_id: str = Field(default=None, alias="invokeId")
    parent_invoke_id: Optional[str] = Field(default=None, alias="parentInvokeId")
    chain_id: str = Field(default=None, alias="chainId")  # trace_id
    invoke_type: str = Field(default=None, alias="invokeType")
    name: str = Field(default=None, alias="name")
    start_time: datetime = Field(default=None, alias="startTime")
    end_time: Optional[datetime] = Field(default=None, alias="endTime")
    elapsed_time: Optional[str] = Field(default=None, alias="elapsedTime")
    inputs: Optional[dict] = Field(default=None, alias="inputs")
    outputs: Optional[dict] = Field(default=None, alias="outputs")
    error: Optional[dict] = Field(default=None, alias="error")
    child_invokes: List[str] = Field(default=[], alias="childInvokes")
    meta_data: Optional[dict] = Field(
        default=None, alias="metaData"
    )  # include llm function tools and token infos

    def dict_to_attrs(self, attributes: dict):
        """
        InvokeData4Agent dict to attrs
        :param attributes: dict to attrs
        """
        if attributes:
            for key, value in attributes.items():
                if hasattr(self, key):
                    setattr(self, key, value)


class InvokeType(Enum):
    """
    Invoke Type
    """

    PROMPT = "prompt"
    LLM = "llm"
    PLUGIN = "plugin"
    CHAIN = "chain"
    RETRIEVER = "retriever"
    EVALUATOR = "evaluator"


class BaseDataHandler(
    PromptTraceHandler,
    LLMTraceHandler,
    PluginTraceHandler,
    ChainTraceHandler,
    RetrieverTraceHandler,
    EvaluatorTraceHandler,
):
    """
    Base Data Handler
    """

    def __init__(self):
        self.invoke_map: Dict[str, InvokeData] = {}
        self.chain_id = None

    def on_prompt_start(
        self,
        instance_info: dict,
        inputs: Any,
        invoke_id: str,
        *args,
        parent_invoke_id: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """
        Start tracing the Prompt invoke.

        Args:
            instance_info (dict): All attributes of Prompt instance.
            inputs (Any): The input of the Prompt invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
            parent_invoke_id (Optional[str]): The id of this invoke method's parent invoke. If another invoke method is
                called in this invoke method, then this invoke is the parent of the other method which is called.
                Default: ``None`` .
        """
        start_time, execution_order_index = self.__prepare_data(
            invoke_id, parent_invoke_id
        )
        prompt_invoke_data = InvokeData(
            invoke_type=InvokeType.PROMPT,
            invoke_id=invoke_id,
            parent_invoke_id=parent_invoke_id,
            chain_id=self.chain_id,
            name=instance_info["class_name"],
            start_time=start_time,
            inputs={"input": inputs},
            execution_order_index=execution_order_index,
            child_execution_order_index=execution_order_index,
            extra=kwargs,
            serialized=instance_info,
        )
        self._start_trace(prompt_invoke_data)
        self._on_prompt_start(prompt_invoke_data)

    def on_prompt_end(
        self, outputs: Any, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        End tracing the Prompt invoke.

        Args:
            outputs (Any): The output of the Prompt invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        prompt_invoke_data = self._get_invoke_data(invoke_id, InvokeType.PROMPT)
        prompt_invoke_data.outputs = {"output": outputs}
        prompt_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(prompt_invoke_data)
        self._on_prompt_end(prompt_invoke_data)

    def on_prompt_error(
        self, error: BaseException, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        Handle an error when Prompt invoke capture exception.

        Args:
            error (BaseException): The exception captured by invoke.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        prompt_invoke_data = self._get_invoke_data(invoke_id, InvokeType.PROMPT)
        prompt_invoke_data.error = {"error": repr(error)}
        prompt_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(prompt_invoke_data)
        self._on_prompt_error(prompt_invoke_data)

    def on_llm_start(
        self,
        instance_info: dict,
        inputs: Any,
        invoke_id: str,
        *args: Any,
        parent_invoke_id: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """
        Start tracing the LLM invoke.

        Args:
            instance_info (dict): All attributes of LLM instance.
            inputs (Any): The input of the LLM invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
            parent_invoke_id (Optional[str]): The id of this invoke method's parent invoke. If another invoke method is
                called in this invoke method, then this invoke is the parent of the other method which is called.
                Default: ``None`` .
        """
        start_time, execution_order_index = self.__prepare_data(
            invoke_id, parent_invoke_id
        )
        llm_invoke_data = InvokeData(
            invoke_type=InvokeType.LLM,
            name=instance_info["class_name"],
            start_time=start_time,
            inputs={"input": inputs},
            execution_order_index=execution_order_index,
            child_execution_order_index=execution_order_index,
            parent_invoke_id=parent_invoke_id,
            invoke_id=invoke_id,
            chain_id=self.chain_id,
            extra=kwargs,
            serialized=instance_info,
        )
        self._start_trace(llm_invoke_data)
        self._on_llm_start(llm_invoke_data)

    def on_llm_end(
        self, outputs: Any, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        End tracing the LLM invoke.

        Args:
            outputs (Any): The output of the LLM invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        llm_invoke_data = self._get_invoke_data(invoke_id, InvokeType.LLM)
        llm_invoke_data.outputs = {"output": outputs}
        llm_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(llm_invoke_data)
        self._on_llm_end(llm_invoke_data)

    def on_llm_error(
        self, error: BaseException, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        Handle an error when LLM invoke capture exception.

        Args:
            error (BaseException): The exception captured by invoke.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        llm_invoke_data = self._get_invoke_data(invoke_id, InvokeType.LLM)
        llm_invoke_data.error = {"error": type(error).__name__}
        llm_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(llm_invoke_data)
        self._on_llm_error(llm_invoke_data)

    def on_plugin_start(
        self,
        instance_info: dict,
        inputs: Any,
        invoke_id: str,
        *args: Any,
        parent_invoke_id: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """
        Start tracing the Plugin invoke.

        Args:
            instance_info (dict): All attributes of Plugin instance.
            inputs (Any): The input of the Plugin invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
            parent_invoke_id (Optional[str]): The id of this invoke method's parent invoke. If another invoke method is
                called in this invoke method, then this invoke is the parent of the other method which is called.
                Default: ``None`` .
        """
        start_time, execution_order_index = self.__prepare_data(
            invoke_id, parent_invoke_id
        )
        plugin_invoke_data = InvokeData(
            invoke_id=invoke_id,
            parent_invoke_id=parent_invoke_id,
            chain_id=self.chain_id,
            invoke_type=InvokeType.PLUGIN,
            name=instance_info["class_name"],
            start_time=start_time,
            inputs={"input": inputs},
            execution_order_index=execution_order_index,
            child_execution_order_index=execution_order_index,
            extra=kwargs,
            serialized=instance_info,
        )
        self._start_trace(plugin_invoke_data)
        self._on_plugin_start(plugin_invoke_data)

    def on_plugin_end(
        self, outputs: Any, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        End tracing the Plugin invoke.

        Args:
            outputs (Any): The output of the Plugin invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        plugin_invoke_data = self._get_invoke_data(invoke_id, InvokeType.PLUGIN)
        plugin_invoke_data.outputs = {"output": outputs}
        plugin_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(plugin_invoke_data)
        self._on_plugin_end(plugin_invoke_data)

    def on_plugin_error(
        self, error: BaseException, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        Handle an error when Plugin invoke capture exception.

        Args:
            error (BaseException): The exception captured by invoke.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        plugin_invoke_data = self._get_invoke_data(invoke_id, InvokeType.PLUGIN)
        plugin_invoke_data.error = {"error": repr(error)}
        plugin_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(plugin_invoke_data)
        self._on_plugin_error(plugin_invoke_data)

    def on_chain_start(
        self,
        instance_info: dict,
        inputs: Any,
        invoke_id: str,
        *args: Any,
        parent_invoke_id: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """
        Start tracing the Chain invoke.

        Args:
            instance_info (dict): All attributes of Chain instance.
            inputs (Any): The input of the Chain invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
            parent_invoke_id (Optional[str]): The id of this invoke method's parent invoke. If another invoke method is
                called in this invoke method, then this invoke is the parent of the other method which is called.
                Default: ``None`` .
        """
        start_time, execution_order_index = self.__prepare_data(
            invoke_id, parent_invoke_id
        )
        chain_invoke_data = InvokeData(
            invoke_id=invoke_id,
            parent_invoke_id=parent_invoke_id,
            chain_id=self.chain_id,
            invoke_type=InvokeType.CHAIN,
            name=instance_info["class_name"],
            start_time=start_time,
            inputs={"input": inputs},
            execution_order_index=execution_order_index,
            child_execution_order_index=execution_order_index,
            extra=kwargs,
            serialized=instance_info,
        )
        self._start_trace(chain_invoke_data)
        self._on_chain_start(chain_invoke_data)

    def on_chain_end(
        self, outputs: Any, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        End tracing the Chain invoke.

        Args:
            outputs (Any): The output of the Chain invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        chain_invoke_data = self._get_invoke_data(invoke_id, InvokeType.CHAIN)
        chain_invoke_data.outputs = {"output": outputs}
        chain_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(chain_invoke_data)
        self._on_chain_end(chain_invoke_data)

    def on_chain_error(
        self, error: BaseException, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        Handle an error when Chain invoke capture exception.

        Args:
            error (BaseException): The exception captured by invoke.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        chain_invoke_data = self._get_invoke_data(invoke_id, InvokeType.CHAIN)
        chain_invoke_data.error = {"error": type(error).__name__}
        chain_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(chain_invoke_data)
        self._on_chain_error(chain_invoke_data)

    def on_retriever_start(
        self,
        instance_info: dict,
        inputs: Any,
        invoke_id: str,
        *args: Any,
        parent_invoke_id: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """
        Start tracing the Retriever invoke.

        Args:
            instance_info (dict): All attributes of Retriever instance.
            inputs (Any): The input of the Retriever invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
            parent_invoke_id (Optional[str]): The id of this invoke method's parent invoke. If another invoke method is
                called in this invoke method, then this invoke is the parent of the other method which is called.
                Default: ``None`` .
        """
        start_time, execution_order_index = self.__prepare_data(
            invoke_id, parent_invoke_id
        )
        retriever_invoke_data = InvokeData(
            invoke_id=invoke_id,
            parent_invoke_id=parent_invoke_id,
            chain_id=self.chain_id,
            invoke_type=InvokeType.RETRIEVER,
            name=instance_info["class_name"],
            serialized=instance_info,
            extra=kwargs,
            start_time=start_time,
            inputs={"input": inputs},
            execution_order_index=execution_order_index,
            child_execution_order_index=execution_order_index,
        )
        self._start_trace(retriever_invoke_data)
        self._on_retriever_start(retriever_invoke_data)

    def on_retriever_end(
        self, outputs: Any, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        End tracing the Retriever invoke.

        Args:
            outputs (Any): The output of the Retriever invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        retriever_invoke_data = self._get_invoke_data(invoke_id, InvokeType.RETRIEVER)
        retriever_invoke_data.outputs = {"output": outputs}
        retriever_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(retriever_invoke_data)
        self._on_retriever_end(retriever_invoke_data)

    def on_retriever_error(
        self, error: BaseException, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        Handle an error when Retriever invoke capture exception.

        Args:
            error (BaseException): The exception captured by invoke.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        retriever_invoke_data = self._get_invoke_data(invoke_id, InvokeType.RETRIEVER)
        retriever_invoke_data.error = {"error": type(error).__name__}
        retriever_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(retriever_invoke_data)
        self._on_retriever_error(retriever_invoke_data)

    def on_evaluator_start(
        self,
        instance_info: dict,
        inputs: Any,
        invoke_id: str,
        *args: Any,
        parent_invoke_id: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """
        Start tracing the Evaluator invoke.

        Args:
            instance_info (dict): All attributes of Evaluator instance.
            inputs (Any): The input of the Evaluator invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
            parent_invoke_id (Optional[str]): The id of this invoke method's parent invoke. If another invoke method is
                called in this invoke method, then this invoke is the parent of the other method which is called.
                Default: ``None`` .
        """
        start_time, execution_order_index = self.__prepare_data(
            invoke_id, parent_invoke_id
        )
        evaluator_invoke_data = InvokeData(
            invoke_id=invoke_id,
            parent_invoke_id=parent_invoke_id,
            chain_id=self.chain_id,
            invoke_type=InvokeType.EVALUATOR,
            name=instance_info["class_name"],
            serialized=instance_info,
            extra=kwargs,
            start_time=start_time,
            inputs={"input": inputs},
            execution_order_index=execution_order_index,
            child_execution_order_index=execution_order_index,
        )
        self._start_trace(evaluator_invoke_data)
        self._on_evaluator_start(evaluator_invoke_data)

    def on_evaluator_end(
        self, outputs: Any, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        End tracing the Evaluator invoke.

        Args:
            outputs (Any): The output of the Evaluator invoke method.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        evaluator_invoke_data = self._get_invoke_data(invoke_id, InvokeType.EVALUATOR)
        evaluator_invoke_data.outputs = {"output": outputs}
        evaluator_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(evaluator_invoke_data)
        self._on_evaluator_end(evaluator_invoke_data)

    def on_evaluator_error(
        self, error: BaseException, invoke_id: str, *args: Any, **kwargs: Any
    ) -> None:
        """
        Handle an error when Evaluator invoke capture exception.

        Args:
            error (BaseException): The exception captured by invoke.
            invoke_id (str): The identifier of this invoke. Each invoke has a unique id.
        """
        evaluator_invoke_data = self._get_invoke_data(invoke_id, InvokeType.EVALUATOR)
        evaluator_invoke_data.error = {"error": repr(error)}
        evaluator_invoke_data.end_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        self._end_trace(evaluator_invoke_data)
        self._on_evaluator_error(evaluator_invoke_data)

    def _start_trace(self, invoke: InvokeData) -> None:
        """
        start trace
        """
        if invoke.parent_invoke_id:
            parent_invoke = self.invoke_map.get(str(invoke.parent_invoke_id))
            if parent_invoke:
                parent_invoke.child_invokes.append(invoke)
                parent_invoke.child_execution_order_index = max(
                    parent_invoke.child_execution_order_index,
                    invoke.child_execution_order_index,
                )
            else:
                logger.debug("Parent invoke with %s not found", invoke.invoke_id)

        self.invoke_map[str(invoke.invoke_id)] = invoke

    def _end_trace(self, invoke: InvokeData) -> None:
        """
        end trace
        """
        if invoke.parent_invoke_id:
            parent_invoke = self.invoke_map.get(str(invoke.parent_invoke_id))
            if parent_invoke is None:
                logger.debug("Parent invoke with %s not found", invoke.invoke_id)
            elif (
                invoke.child_execution_order_index is not None
                and parent_invoke.child_execution_order_index is not None
                and invoke.child_execution_order_index
                > parent_invoke.child_execution_order_index
            ):
                parent_invoke.child_execution_order_index = (
                    invoke.child_execution_order_index
                )

        self.invoke_map.pop(str(invoke.invoke_id))

    def _get_execution_order(self, parent_invoke_id: Optional[str] = None) -> int:
        """
        get execution order
        """
        if parent_invoke_id is None:
            return 1
        parent_invoke = self.invoke_map.get(parent_invoke_id)
        if parent_invoke is None:
            logger.debug("Parent invoke with invoke_id %s not found.", parent_invoke_id)
            return 1
        if parent_invoke.child_execution_order_index is None:
            raise JiuWenBaseException(
                StatusCode.INSIGHT_CHILD_EXECUTION_ORDER_INDEX_ERROR.code,
                StatusCode.INSIGHT_CHILD_EXECUTION_ORDER_INDEX_ERROR.errmsg.format(
                    parent_invoke_id=parent_invoke_id
                ),
            )
        return parent_invoke.child_execution_order_index + 1

    def _get_invoke_data(self, invoke_id: str, invoke_type: InvokeType) -> InvokeData:
        """获取invoke_id对应的invoke_data"""
        invoke_data = self.invoke_map.get(invoke_id)
        if invoke_data is None:
            raise JiuWenBaseException(
                StatusCode.INSIGHT_INVOKE_ID_NOT_EXIST_ERROR.code,
                StatusCode.INSIGHT_INVOKE_ID_NOT_EXIST_ERROR.errmsg,
            )
        if invoke_data.invoke_type != invoke_type:
            raise JiuWenBaseException(
                StatusCode.INSIGHT_INVOKE_TYPE_ERROR.code,
                StatusCode.INSIGHT_INVOKE_TYPE_ERROR.errmsg.format(
                    invoke_type=invoke_data.invoke_type
                ),
            )
        return invoke_data

    def _set_chain_id(self, invoke_id: str, parent_invoke_id: str) -> None:
        """
        set chain id
        """
        if parent_invoke_id is None and self.chain_id is None:
            self.chain_id = invoke_id

    def __prepare_data(self, invoke_id, parent_invoke_id):
        """
        prepare data
        """
        self._set_chain_id(invoke_id, parent_invoke_id)
        start_time = datetime.now(tz=tzlocal()).replace(tzinfo=None)
        execution_order_index = self._get_execution_order(parent_invoke_id)
        return start_time, execution_order_index

    def _on_prompt_start(self, invoke: InvokeData) -> None: ...

    def _on_prompt_end(self, invoke: InvokeData) -> None: ...

    def _on_prompt_error(self, invoke: InvokeData) -> None: ...

    def _on_llm_start(self, invoke: InvokeData) -> None: ...

    def _on_llm_end(self, invoke: InvokeData) -> None: ...

    def _on_llm_error(self, invoke: InvokeData) -> None: ...

    def _on_plugin_start(self, invoke: InvokeData) -> None: ...

    def _on_plugin_end(self, invoke: InvokeData) -> None: ...

    def _on_plugin_error(self, invoke: InvokeData) -> None: ...

    def _on_chain_start(self, invoke: InvokeData) -> None: ...

    def _on_chain_end(self, invoke: InvokeData) -> None: ...

    def _on_chain_error(self, invoke: InvokeData) -> None: ...

    def _on_retriever_start(self, invoke: InvokeData) -> None: ...

    def _on_retriever_end(self, invoke: InvokeData) -> None: ...

    def _on_retriever_error(self, invoke: InvokeData) -> None: ...

    def _on_evaluator_start(self, invoke: InvokeData) -> None: ...

    def _on_evaluator_end(self, invoke: InvokeData) -> None: ...

    def _on_evaluator_error(self, invoke: InvokeData) -> None: ...
