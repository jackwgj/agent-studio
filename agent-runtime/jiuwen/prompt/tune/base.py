# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
base prompt optimizer
"""

import json
import random
import re
import threading
import time
from abc import ABC, abstractmethod
from enum import Enum
from typing import List, Dict, Optional

import numpy as np
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.messages import AIMessage
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.prompt import PromptAssembler
from jiuwen.prompt.common.config import LLMModelInfo
from jiuwen.prompt.common.exception.base import JiuWenBaseException
from jiuwen.prompt.template.template_manager import InnerTemplate
from pydantic import BaseModel, Field
from tqdm import tqdm

SYSTEM_ROLE = "system"
ASSISTANT_ROLE = "assistant"
USER_ROLE = "user"

MESSAGE_ROLE_KEY = "role"
MESSAGE_CONTENT_KEY = "content"
MESSAGE_AGENT_BUILDER_RAW_CONTENT_KEY = "raw_content"
MESSAGE_TOOL_CALLS_KEY = "tool_calls"

# 下列agentBuilder相关常量以llm/common/llm_service/language_model内模型文件名为准，通常不会改变
AGENT_BUILDER_XIAOYI_MODEL_NAME = "agentBuilder_xiaoyi"  # 在小艺场景中使用盘古模型时指定的模型名称，对算法执行逻辑有影响
AGENT_BUILDER_POC_MODEL_NAME = (
    "poc_agentBuilder"  # 在poc场景中使用盘古模型时指定的模型名称，对算法执行逻辑有影响
)
AGENT_BUILDER_MODEL_FAMILY = {
    AGENT_BUILDER_XIAOYI_MODEL_NAME,
    AGENT_BUILDER_POC_MODEL_NAME,
    "ei_agentBuilder",
    "agentBuilder",
}  # 当前算法可用的各盘古模型

DEFAULT_TRAIN_SET_NUM_RATIO = 0.8
NOT_AVAILABLE = -1


class JobStatus(str, Enum):
    RUNNING = "Running"
    FINISHED = "Finished"
    FAILED = "Failed"
    QUEUED = "Queued"
    STOP = "Stop"
    SUSPEND = "Suspend"


class Progress(BaseModel):
    """progress"""

    status: JobStatus = Field(default=JobStatus.QUEUED)
    best_prompts: list[str] = []
    message: str = ""

    def __init__(self, **data):
        super().__init__(**data)
        self._lock = threading.Lock()

    def update_prompt(self, prompt: str):
        """update prompt"""
        try:
            self._lock.acquire(True)
            self.best_prompts.append(prompt)
        finally:
            self._lock.release()

    def set_status(self, status: JobStatus):
        """set_status"""
        try:
            self._lock.acquire(True)
            self.status = status
        finally:
            self._lock.release()

    def set_message(self, message: str):
        """record_message"""
        try:
            self._lock.acquire(True)
            self.message = message
        finally:
            self._lock.release()

    def get_message(self):
        """get message"""
        try:
            self._lock.acquire(True)
            return self.message
        finally:
            self._lock.release()

    def get_status(self):
        """get status"""
        try:
            self._lock.acquire(True)
            return self.status
        finally:
            self._lock.release()

    def get_prompts(self):
        """get list prompts"""
        try:
            self._lock.acquire(True)
            return self.best_prompts
        finally:
            self._lock.release()


class Case(BaseModel):
    """Definition of prompt optimization user cases."""

    messages: List[Dict] = Field(default=[])
    tools: Optional[List[Dict]] = Field(default=None)


class LLMModelProcess:
    """Definition of LLM invoke process."""

    chat_llm = None

    def __init__(self, model_info: LLMModelInfo, retry_times: int = 5):
        """init"""
        if model_info is None:
            raise JiuWenBaseException(
                error_code=StatusCode.LLM_MODEL_SOURCE_ERROR.code,
                message=StatusCode.LLM_MODEL_SOURCE_ERROR.errmsg.format(
                    error_msg="the model info input is missing."
                ),
            )
        self.chat_llm = ModelFactory().get_model(
            model_type=model_info.model_source,
            model_name=model_info.model,
            temperature=model_info.temperature,
            top_p=model_info.top_p,
        )

        self.model_name = model_info.model_source
        self.cache = {}
        self.retry_times = retry_times

    def llm_request_with_retry(self, language_model_input) -> AIMessage:
        """llm_request_with_retry"""
        exc = None
        for attempt in range(self.retry_times):
            try:
                result = self.chat_llm.invoke(language_model_input)
                return result
            except Exception as e:
                exc = e
                time.sleep(attempt)
        raise JiuWenBaseException(
            error_code=StatusCode.PROMPT_OPTIMIZE_EVALUATE_ERROR.code,
            message=StatusCode.PROMPT_OPTIMIZE_EVALUATE_ERROR.errmsg.format(
                error_msg="LLM evaluate answer failed"
            ),
        ) from exc

    def chat_multi_messages(self, messages: List[Dict], tools: List[dict]) -> dict:
        """chat_multi_messages"""
        # 这里我们保证输入和输出的递归定义都是词典，不包含任何basemodel
        if f"str{messages}-str{tools}" in self.cache:
            return self.cache[f"str{messages}-str{tools}"]

        if self.model_name == AGENT_BUILDER_XIAOYI_MODEL_NAME:
            language_model_input = LanguageModelInput(messages=messages, tools=tools)
        else:
            language_model_input = LanguageModelInput(
                messages=ModelUtil.switch_message(messages=messages), tools=tools
            )

        result = self.llm_request_with_retry(language_model_input)
        if tools is None:
            # 此时表明为开放式场景，因此只需关注解析后的纯文本信息即可
            final_result = {
                MESSAGE_ROLE_KEY: ASSISTANT_ROLE,
                MESSAGE_CONTENT_KEY: result.content,
            }
        else:
            tool_calls = {"name": None, "args": None}
            if hasattr(result.tool_calls, "name"):
                tool_calls["name"] = result.tool_calls.name
            if hasattr(result.tool_calls, "args"):
                tool_calls["args"] = result.tool_calls.args
            final_result = {
                MESSAGE_ROLE_KEY: ASSISTANT_ROLE,
                MESSAGE_TOOL_CALLS_KEY: tool_calls,
            }
        if result.raw_content is not None:
            final_result[MESSAGE_AGENT_BUILDER_RAW_CONTENT_KEY] = result.raw_content
        self.cache[f"str{messages}-str{tools}"] = final_result
        return final_result

    def chat_single_message(self, message: str) -> List[str]:
        """chat_single_message"""
        # 这种场景的唯一目的是为了让optim模型输出优化的指导意见，因此输出一律为模型端做过预处理后的结果，且不需要tool的参与"""
        if f"str{message}" in self.cache:
            return self.cache[f"str{message}"]
        messages = [{MESSAGE_ROLE_KEY: USER_ROLE, MESSAGE_CONTENT_KEY: message}]
        if self.model_name in AGENT_BUILDER_MODEL_FAMILY:
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.code,
                message="all kind of agent-buildermodels can not been used as optimizer!!!",
            )
            # 原因在于，如果优化出的prompt里有unused包裹的示例，且示例之后还有别的结果，
            # 那么此时盘古模型则会直接将unused之后的内容进行移除，导致优化出的prompt内容不全
        language_model_input = LanguageModelInput(
            messages=ModelUtil.switch_message(messages=messages), tools=None
        )
        result = self.llm_request_with_retry(language_model_input)
        final_result = [result.content]
        self.cache[f"str{message}"] = final_result
        return final_result


class PromptOptimizer(ABC):
    """basic class for prompt optimizer"""

    set_seed = True
    seed = 42
    shuffle_dataset = True

    def __init__(
        self, optim_engine: LLMModelInfo, infer_engine: LLMModelInfo, hard_compare: bool
    ):
        """__init__"""
        self._optim_engine = optim_engine
        self._infer_engine = infer_engine
        self._current_progress = Progress()
        self.set_seed = PromptOptimizer.set_seed
        self.seed = PromptOptimizer.seed
        self.lock = threading.Lock()
        self.shuffle_dataset = PromptOptimizer.shuffle_dataset
        self.optim_model = LLMModelProcess(optim_engine)
        self.infer_model = LLMModelProcess(infer_engine)
        self.hard_compare = hard_compare

    def __call__(self, *args, **kwargs):
        """__call__"""
        return self.optimize(*args, **kwargs)

    @staticmethod
    def get_last_assistant_message_idx(messages: List[dict]) -> int:
        """get_last_assistant_message_idx"""
        last_assistant_message_idx = -1
        for idx, cur_message in reversed(list(enumerate(messages))):
            if cur_message[MESSAGE_ROLE_KEY] == ASSISTANT_ROLE:
                last_assistant_message_idx = idx
                break
        if last_assistant_message_idx == -1:
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_OPTIMIZE_CASE_CONTENT_ERROR.code,
                message=StatusCode.PROMPT_OPTIMIZE_CASE_CONTENT_ERROR.errmsg,
            )
        return last_assistant_message_idx

    @staticmethod
    def get_input_messages(
        messages: List[dict], last_assistant_message_idx: int
    ) -> List[dict]:
        """get_input_messages"""
        input_messages = messages[:last_assistant_message_idx]
        return input_messages

    @staticmethod
    def get_task_system_prompt_idx(
        input_messages: List[dict], original_prompts: List[str]
    ) -> int:
        """get_task_system_prompt_idx"""
        task_system_prompt_idx = -1
        for idx, message in reversed(list(enumerate(input_messages))):
            if (
                message[MESSAGE_CONTENT_KEY] in original_prompts
                and message[MESSAGE_ROLE_KEY] == SYSTEM_ROLE
            ):
                task_system_prompt_idx = idx
                break
        if task_system_prompt_idx == -1:
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_TEMPLATE_CASE_CONTENT_ERROR.code,
                message="The optimized prompts not found",
            )
        for idx in range(task_system_prompt_idx + 1, len(input_messages)):
            if input_messages[idx][MESSAGE_ROLE_KEY] == SYSTEM_ROLE:
                raise JiuWenBaseException(
                    error_code=StatusCode.PROMPT_TEMPLATE_CASE_CONTENT_ERROR.code,
                    message="the system prompt to be optimized must be the last system prompt",
                )

        return task_system_prompt_idx

    @staticmethod
    def replace_message_with_new_prompt(
        messages: List[Dict], new_prompt: str, task_system_prompt_idx: int
    ) -> List[dict]:
        """replace_message_with_new_prompt"""
        new_message = messages[:]
        new_message[task_system_prompt_idx] = {
            MESSAGE_ROLE_KEY: SYSTEM_ROLE,
            MESSAGE_CONTENT_KEY: new_prompt,
        }
        return new_message

    @abstractmethod
    def _do_optimize(
        self,
        train_dataset: List[Case],
        eval_dataset: List[Case],
        iter_turns: int,
        origin_prompts: List[str],
    ) -> str:
        """_do_optimize"""
        raise NotImplementedError("Do optimize function need to be implemented")

    def get_label_message(
        self, messages: List[dict], last_assistant_message_idx: int
    ) -> dict:
        """get_label_message"""
        last_assistant_message = messages[last_assistant_message_idx]
        if "function_call" in last_assistant_message:
            function_call = last_assistant_message["function_call"]
            try:
                label_message = {
                    MESSAGE_ROLE_KEY: last_assistant_message[MESSAGE_ROLE_KEY],
                    MESSAGE_TOOL_CALLS_KEY: {
                        "name": function_call["name"],
                        "args": json.loads(function_call["arguments"]),
                    },
                }

            except Exception as e:
                raise JiuWenBaseException(
                    error_code=StatusCode.PROMPT_TEMPLATE_CASE_CONTENT_ERROR.code,
                    message="The arguments of tools calling ground truth can not be converted "
                    "into a json",
                ) from e
        else:
            label_message = {
                MESSAGE_ROLE_KEY: last_assistant_message[MESSAGE_ROLE_KEY],
                MESSAGE_CONTENT_KEY: last_assistant_message[MESSAGE_CONTENT_KEY],
            }

        return label_message

    def accuracy_score(
        self,
        predictions: List[dict],
        labels: List[dict],
        messages_list: List[List[dict]],
    ) -> (List[int], float):
        """accuracy_score"""
        logger.info("doing accuracy_score")
        count_list, compare_list = [], []
        for prediction, label, messages in tqdm(
            zip(predictions, labels, messages_list),
            total=len(predictions),
            desc="compare answer",
        ):
            # 如果compare后返回NOT_AVAILABLE，那么不纳入acc统计范围内，也不用于后续选取bad case
            compare_result = self.compare_answer(prediction, label, messages)
            if compare_result != NOT_AVAILABLE:
                count_list.append(compare_result)
            compare_list.append(compare_result)
        return compare_list, float(np.mean(count_list))

    def messages_preprocess(self, case: Case, original_prompts: List[str]) -> dict:
        """messages_preprocess"""
        # 在构建数据集时统一采用词典存储，而不使用basemodel
        messages = case.messages
        last_assistant_message_idx = self.get_last_assistant_message_idx(messages)
        label_message = self.get_label_message(messages, last_assistant_message_idx)

        preprocessed_input_messages = self.get_input_messages(
            messages, last_assistant_message_idx
        )

        task_system_prompt_idx = self.get_task_system_prompt_idx(
            preprocessed_input_messages, original_prompts
        )
        input_messages = preprocessed_input_messages[task_system_prompt_idx:]
        result = {
            "input_messages": input_messages,
            "tools": case.tools,
            "label_message": label_message,
            "task_system_prompt_idx": 0,
        }
        # 处理之后 task_system_prompt应始终为input_messages中第一条message，且此时input_message应只有一个system message
        return result

    def compare_answer(
        self, prediction: dict, ground_truth: dict, messages: List[dict]
    ) -> int:
        """both prediction and label are message dict"""
        # ground truth现在还是message格式

        if self.hard_compare:
            if MESSAGE_TOOL_CALLS_KEY in ground_truth:
                # 有tool call的话，直接用tool call进行比较就可以了，
                prediction_tool_calls = prediction[MESSAGE_TOOL_CALLS_KEY]
                ground_truth_tool_calls = ground_truth[MESSAGE_TOOL_CALLS_KEY]

                return int(
                    prediction_tool_calls.get("name")
                    == ground_truth_tool_calls.get("name")
                    and prediction_tool_calls.get("args")
                    == ground_truth_tool_calls.get("args")
                )
            return int(
                prediction[MESSAGE_CONTENT_KEY] == ground_truth[MESSAGE_CONTENT_KEY]
            )
        evaluate_template = InnerTemplate().get(name="prompt_apo_compare")
        history_messages = [
            item for item in messages if item[MESSAGE_ROLE_KEY] != SYSTEM_ROLE
        ]
        if MESSAGE_TOOL_CALLS_KEY in ground_truth:
            message = PromptAssembler(evaluate_template).assemble(
                messages=history_messages,
                ground_truth=ground_truth[MESSAGE_TOOL_CALLS_KEY],
                prediction=prediction[MESSAGE_TOOL_CALLS_KEY],
            )
        else:
            message = PromptAssembler(evaluate_template).assemble(
                messages=history_messages,
                ground_truth=ground_truth[MESSAGE_CONTENT_KEY],
                prediction=prediction[MESSAGE_CONTENT_KEY],
            )
        # 有raw的时候代表有tool 那此时应用json比较；如果没有toolcall gt就没有raw content 因此用content比就可以了
        response = self.optim_model.chat_single_message(message)[0]
        matches = re.findall(r"\[([^]]*)]", response)
        lower_matches = [item.lower() for item in matches]
        if len(matches) and "true" in lower_matches:
            return 1
        if len(matches) and "false" in lower_matches:
            return 0
        return NOT_AVAILABLE

    def get_progress(self) -> Progress:
        """get_progress"""
        try:
            self.lock.acquire(True)
            return self._current_progress
        finally:
            self.lock.release()

    def validate_params(
        self, dataset: List[Case], iter_turns: int, origin_prompts: List[str]
    ):
        """validate_params"""
        try:
            if (
                not isinstance(dataset, list)
                or not all(isinstance(case, Case) for case in dataset)
                or len(dataset) <= 0
            ):
                raise ValueError("dataset must be a list of Case objects and len > 0.")
            if not isinstance(iter_turns, int) or iter_turns < 0:
                raise ValueError("iter_turns must be a non-negative integer.")
            if not isinstance(origin_prompts, list) or not all(
                isinstance(prompt, str) for prompt in origin_prompts
            ):
                raise ValueError("origin_prompts must be a list of strings.")
        except ValueError as e:
            message = StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.errmsg.format(
                error_msg=str(e)
            )
            self._update_progress(status=JobStatus.FAILED, message=message)
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.code,
                message=message,
            ) from e

    def calculate_dataset_sizes(
        self, dataset: List[Case], train_size: int = None, eval_size: int = None
    ):
        """calculate_dataset_sizes"""
        if eval_size is None and train_size is None:
            train_size = int(len(dataset) * DEFAULT_TRAIN_SET_NUM_RATIO)
            eval_size = len(dataset) - train_size
        elif eval_size is None and train_size is not None:
            eval_size = len(dataset) - train_size
        elif eval_size is not None and train_size is None:
            train_size = len(dataset) - eval_size

        if eval_size <= 0 or train_size < 0 or eval_size + train_size > len(dataset):
            if eval_size <= 0:
                error_msg = "The eval size must belong to (0,Inf) for any algorithms"
            elif train_size < 0:
                error_msg = "The train size must belong to [0,Inf) for any algorithms"
            else:
                error_msg = "The sum of eval size and train size must be less than the size of the dataset"
            message = StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.errmsg.format(
                error_msg=error_msg
            )
            self._update_progress(status=JobStatus.FAILED, message=message)
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.code,
                message=message,
            )
        return train_size, eval_size

    def split_datasets(self, dataset: List[Case], train_size: int, eval_size: int):
        """split_datasets"""
        if self.shuffle_dataset:
            if self.set_seed:
                random.seed(self.seed)
                np.random.seed(self.seed)
            random.shuffle(dataset)
        train_dataset, eval_dataset = (
            dataset[:train_size],
            dataset[train_size : train_size + eval_size],
        )
        return train_dataset, eval_dataset

    def optimize(
        self,
        dataset: List[Case],
        iter_turns: int,
        origin_prompts: List[str],
        train_size: int = None,
        eval_size: int = None,
    ) -> str:
        """
        Args:
            dataset: Datasets for llm.
            eval_size: Size of evaluation dataset.
            train_size: Size of training dataset.
            iter_turns: Number of iteration rounds.
            origin_prompts: origin prompts to be optimized.

        Returns:
            str: optimized prompt.
        """
        self._update_progress(status=JobStatus.RUNNING)
        self.validate_params(dataset, iter_turns, origin_prompts)
        train_size, eval_size = self.calculate_dataset_sizes(
            dataset, train_size, eval_size
        )
        train_dataset, eval_dataset = self.split_datasets(
            dataset, train_size, eval_size
        )

        try:
            result = self._do_optimize(
                train_dataset, eval_dataset, iter_turns, origin_prompts
            )
        except Exception as e:
            message = e.message if isinstance(e, JiuWenBaseException) else str(e)
            self._update_progress(status=JobStatus.FAILED, message=message)
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_OPTIMIZE_JOB_STATUS_NOT_EXPECTED_ERROR.code,
                message="prompt optimize task failed",
            ) from e
        self._update_progress(status=JobStatus.FINISHED)

        return result

    def _update_progress(
        self, status: JobStatus = None, prompt: str = None, message: str = None
    ):
        """update progress"""
        try:
            self.lock.acquire(True)
            if status:
                self._current_progress.set_status(status=status)
            if prompt:
                self._current_progress.update_prompt(prompt=prompt)
            if message:
                self._current_progress.set_message(message=message)
        finally:
            self.lock.release()
