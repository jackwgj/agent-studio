# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
agentBuilder chain Apo optimizer
"""

import concurrent.futures
import random
import re
from abc import ABC
from typing import List, Union

from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.prompt.common.config import LLMModelInfo
from jiuwen.prompt.common.exception.base import JiuWenBaseException
from jiuwen.prompt.tune.apo.agent_builder_optional import (
    MESSAGE_AGENT_BUILDER_RAW_CONTENT_KEY,
    agent_builder_label_message,
)
from jiuwen.prompt.tune.base import (
    PromptOptimizer,
    MESSAGE_TOOL_CALLS_KEY,
    MESSAGE_CONTENT_KEY,
    Case,
    MESSAGE_ROLE_KEY,
    SYSTEM_ROLE,
    AGENT_BUILDER_MODEL_FAMILY,
)
from pydantic import BaseModel
from tqdm import tqdm


class ApoParams(BaseModel):
    minibatch_size: int = 64
    errors_per_gradient: int = 4
    gradients_per_error: int = 1
    mc_samples_per_step: bool = True
    max_expansion_factor: int = 8
    max_threads: int = 1
    eval_budget: int = 32
    n_gradients: int = 4


class EvaluateData(BaseModel):
    messages_list: List[List[dict]]
    tools_list: Union[List[List[dict]], None]
    prediction_list: List[dict]
    label_list: List[dict]


class ApoOptimizer(PromptOptimizer, ABC):
    def __init__(
        self,
        optim_engine: LLMModelInfo,
        infer_engine: LLMModelInfo,
        beam_size: int = 4,
        hard_compare: bool = False,
    ):
        """init"""
        super().__init__(optim_engine, infer_engine, hard_compare)
        self.param_group = dict(beam_size=beam_size)
        self.default_param = ApoParams()

    @staticmethod
    def parse_tagged_text(text: str, start_tag: str, end_tag: str) -> List[str]:
        """Parse text that is tagged with start and end tags, returning all matches."""
        # 使用 findall 来获取所有匹配的值
        pattern = f"{re.escape(start_tag)}((?:(?!{re.escape(start_tag)}).)*?){re.escape(end_tag)}"
        matches = re.findall(pattern, text, re.DOTALL)
        return [match.strip() for match in matches]

    @staticmethod
    def format_error_str(sample_idxs: List[int], eval_data: EvaluateData) -> str:
        """format_error_str"""
        error_string = ""
        count = 0
        for error_idx in sample_idxs:
            messages, tools, prediction, label = (
                eval_data.messages_list[error_idx],
                eval_data.tools_list[error_idx],
                eval_data.prediction_list[error_idx],
                eval_data.label_list[error_idx],
            )
            error_string += f"### Example {count}\n"
            user_messages = [
                item for item in messages if item[MESSAGE_ROLE_KEY] != SYSTEM_ROLE
            ]
            error_string += f'Input Messages: "{str(user_messages)}"\n'
            if tools is not None:
                error_string += f'Available Tools: "{str(tools)}"\n'
            if MESSAGE_AGENT_BUILDER_RAW_CONTENT_KEY in prediction:
                error_string += f'Prediction: "{str(prediction[MESSAGE_AGENT_BUILDER_RAW_CONTENT_KEY])}"\n'
                error_string += f'Ground Truth: "{str(label[MESSAGE_AGENT_BUILDER_RAW_CONTENT_KEY])}"\n'
            elif MESSAGE_TOOL_CALLS_KEY in prediction:
                # 此时为其他模型的tool call场景，使用tool call的json进行展示
                error_string += (
                    f'Prediction: "{str(prediction[MESSAGE_TOOL_CALLS_KEY])}"\n'
                )
                error_string += (
                    f'Ground Truth: "{str(label[MESSAGE_TOOL_CALLS_KEY])}"\n'
                )
            else:
                # 此时为开放式问答场景，使用content内容进行展示
                error_string += (
                    f'Prediction: "{str(prediction[MESSAGE_CONTENT_KEY])}"\n'
                )
                error_string += f'Ground Truth: "{str(label[MESSAGE_CONTENT_KEY])}"\n'
            count += 1
        return error_string.strip()

    def single_process_example(
        self, converted_messages: List[dict], tools: List[dict], label_message: dict
    ) -> (List[dict], List[dict], dict, dict):
        """sub single_process_example function"""
        return (
            converted_messages,
            tools,
            self.infer_model.chat_multi_messages(converted_messages, tools),
            label_message,
        )

    def multi_threads_evaluate(
        self, dataset: List[dict], new_prompt: str
    ) -> EvaluateData:
        """multi_threads_evaluate"""
        futures = []
        with concurrent.futures.ThreadPoolExecutor(
            max_workers=self.default_param.max_threads
        ) as executor:
            for sample in dataset:
                converted_messages = self.replace_message_with_new_prompt(
                    sample["input_messages"],
                    new_prompt,
                    sample["task_system_prompt_idx"],
                )
                futures.append(
                    executor.submit(
                        self.single_process_example,
                        converted_messages,
                        sample["tools"],
                        sample["label_message"],
                    )
                )
            tqdm_loader = tqdm(
                concurrent.futures.as_completed(futures),
                total=len(futures),
                position=0,
                desc="inference",
            )
            messages_list, tools_list, prediction_list, label_list = zip(
                *[future.result() for future in tqdm_loader]
            )
            eval_data = EvaluateData(
                messages_list=messages_list,
                tools_list=tools_list,
                prediction_list=prediction_list,
                label_list=label_list,
            )
        return eval_data

    def evaluate(
        self, new_prompt: str, dataset: List[dict]
    ) -> (float, EvaluateData, List[int]):
        """evaluate function"""
        eval_data = self.multi_threads_evaluate(dataset, new_prompt)
        compare_list, accuracy = self.accuracy_score(
            eval_data.prediction_list, eval_data.label_list, eval_data.messages_list
        )
        return accuracy, eval_data, compare_list

    def sample_error_str(
        self, eval_data: EvaluateData, compare_list: List[int], sample_num: int
    ) -> str:
        """sample and construct error str"""
        error_idxs = [
            i for i, compare_result in enumerate(compare_list) if compare_result == 0
        ]
        sample_idxs = random.sample(error_idxs, min(len(error_idxs), sample_num))
        return self.format_error_str(sample_idxs, eval_data)

    def get_gradients(
        self, prompt: str, eval_data: EvaluateData, compare_list: List[int]
    ) -> List[str]:
        """get_gradients"""
        prompt_feedbacks = []
        logger.info(f"doing {self.default_param.n_gradients} get_gradients")
        for _ in range(self.default_param.n_gradients):
            error_string = self.sample_error_str(
                eval_data, compare_list, self.default_param.errors_per_gradient
            )
            gradients = self._get_gradients(
                prompt, error_string, self.default_param.gradients_per_error
            )

            prompt_feedbacks += [(t, error_string) for t in gradients]
        prompt_feedbacks = list(set(prompt_feedbacks))
        return prompt_feedbacks

    def apply_gradient(
        self, prompt: str, error_str: str, feedback_str: str
    ) -> List[str]:
        """Incorporate feedback gradient into a prompt."""
        # 所以这个里边 也是有原始prompt 错误例子 上一个llm生成的梯度 然后用来生成新修改的prompt
        transformation_prompt = f"""
        I'm trying to refine a prompt for large language model.

        My current prompt is:
        "{prompt}"

        But it gets the following examples wrong:
        {error_str}

        Based on these examples the problem with this prompt is that {feedback_str}

        Based on the above information, I wrote a different improved prompts.
        The improved prompt should be wrapped with <START> and <END>.
        
        The new prompt are:
        """
        transformation_prompt = "\n".join(
            [line.lstrip() for line in transformation_prompt.split("\n")]
        )
        responses = self.optim_model.chat_single_message(transformation_prompt)
        new_prompts = []
        for response in responses:
            new_prompts += self.parse_tagged_text(response, "<START>", "<END>")
        return new_prompts

    def generate_synonyms(self, prompt_section: str) -> List[str]:
        """Generate synonyms for a prompt section."""
        rewriter_prompt = (
            f"Generate a variation of the following instruction while keeping the semantic "
            f"meaning.\n\nInput: {prompt_section}\n\nOutput:"
        )
        new_instruction = self.optim_model.chat_single_message(rewriter_prompt)
        return new_instruction

    def generate_new_prompts(
        self, candidate: str, eval_data: EvaluateData, compare_list: List[int]
    ) -> List[str]:
        """Generate new prompts based on gradients."""
        cur_new_prompts = []
        gradients = self.get_gradients(candidate, eval_data, compare_list)
        logger.info(f"doing {len(gradients)} apply_gradient")
        for feedback, error_string in gradients:
            tmp = self.apply_gradient(
                candidate, error_string, feedback
            )  # 产出1个新prompt
            cur_new_prompts += tmp
        return cur_new_prompts

    def generate_mc_samples(
        self, cur_new_prompts: List[str], candidate: str
    ) -> List[str]:
        """Generate Monte Carlo samples from new prompts and the candidate."""
        mc_sampled_prompts = []
        for prompt in cur_new_prompts + [candidate]:
            mc_prompts = self.generate_synonyms(prompt)
            mc_sampled_prompts += mc_prompts
        return mc_sampled_prompts

    def expand_candidates(
        self, candidates: List[str], train_dataset: List[dict]
    ) -> List[str]:
        """expand_candidates"""
        logger.info(f"expanding from {len(candidates)} candidates")
        if len(train_dataset) > self.default_param.minibatch_size:
            minibatch = random.sample(
                train_dataset, k=self.default_param.minibatch_size
            )
        else:
            minibatch = train_dataset
        all_prompts = []
        for candidate in candidates:
            # messages_list, tools_list, prediction_list, label_list,
            logger.info(
                f"evaluating on {len(minibatch)} samples from train data on candidate"
            )
            accuracy, eval_data, compare_list = self.evaluate(candidate, minibatch)

            cur_new_prompts = []
            if accuracy < 1.0 and self.default_param.n_gradients > 0:
                logger.info("text grading on error samples on candidate")
                cur_new_prompts = self.generate_new_prompts(
                    candidate, eval_data, compare_list
                )
            mc_sampled_prompts = []
            if self.default_param.mc_samples_per_step:
                # mc等于2 意思是每个旧prompt（包括了旧的，以及梯度更新出来的prompt）采样出两条转义的样本
                # 此时不管是否有没有acc=1也还是要做采样的，因为此时仅在当前minibatch上全对，不代表在全体数据集上全对
                logger.info("mc-ing on candidate")
                mc_sampled_prompts = self.generate_mc_samples(
                    cur_new_prompts, candidate
                )

            cur_merged_prompts = list(set(cur_new_prompts + mc_sampled_prompts))
            if len(cur_merged_prompts) > self.default_param.max_expansion_factor:
                cur_merged_prompts = random.sample(
                    cur_merged_prompts, self.default_param.max_expansion_factor
                )
            all_prompts += cur_merged_prompts
        all_prompts += candidates
        all_prompts = list(set(all_prompts))
        logger.info(f"expanded to get {len(all_prompts)} prompts")
        return all_prompts

    def score_candidates(
        self, candidates: List[str], train_dataset: List[dict]
    ) -> List[float]:
        """score_candidates"""
        if len(candidates) == 1:
            return [1.0]
        if len(train_dataset) > self.default_param.eval_budget:
            eval_dataset = random.sample(
                train_dataset, k=self.default_param.eval_budget
            )
        else:
            eval_dataset = train_dataset
        scores = []
        logger.info(
            f"scoring {len(candidates)} candidates on {len(eval_dataset)} cases sampled from train dataset"
        )
        for idx, candidate in enumerate(candidates):
            logger.info(
                f"scoring {idx} candidate on {len(eval_dataset)} cases sampled from train dataset"
            )
            accuracy, _, _ = self.evaluate(candidate, eval_dataset)
            scores.append(accuracy)
        logger.info(f"get scores for {len(candidates)} candidates: {scores}")
        return scores

    def eval_candidates(
        self, candidates: List[str], eval_dataset: List[dict]
    ) -> (str, float):
        """eval_candidates"""
        scores = []
        for candidate in candidates:
            accuracy, _, _ = self.evaluate(candidate, eval_dataset)
            scores.append(accuracy)
        return candidates[scores.index(max(scores))], max(scores)

    def get_label_message(
        self, messages: List[dict], last_assistant_message_idx: int
    ) -> dict:
        """get_label_message"""

        # tool call的json数据进行优化
        label_message = super().get_label_message(messages, last_assistant_message_idx)
        if (
            self.infer_model.model_name in AGENT_BUILDER_MODEL_FAMILY
            and MESSAGE_TOOL_CALLS_KEY in label_message
        ):
            label_message = agent_builder_label_message(
                label_message,
                label_message[MESSAGE_TOOL_CALLS_KEY]["name"],
                label_message[MESSAGE_TOOL_CALLS_KEY]["args"],
                self.infer_model.model_name,
            )
        return label_message

    def _do_optimize(
        self,
        train_dataset: List[Case],
        eval_dataset: List[Case],
        iter_turns: int,
        origin_prompts: List[str],
    ) -> str:
        """_do_optimize"""
        if len(train_dataset) <= 0:
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.code,
                message=StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.errmsg.format(
                    error_msg="The train size must be bigger than 0 in APO algorithm"
                ),
            )
        processed_train_dataset = [
            self.messages_preprocess(case, origin_prompts) for case in train_dataset
        ]
        processed_eval_dataset = [
            self.messages_preprocess(case, origin_prompts) for case in eval_dataset
        ]
        candidates = origin_prompts[:]

        # 在初始运行时记录最优prompt和初始acc
        logger.info("inferring at eval dataset to get init best candidate")
        best_prompt, best_acc = self.eval_candidates(candidates, processed_eval_dataset)
        self._update_progress(prompt=best_prompt)
        logger.info(f"Init Acc on eval dataset:{best_acc}")

        for i in range(iter_turns):
            logger.info(f"training at train dataset on {i}th turn")
            candidates = self.expand_candidates(candidates, processed_train_dataset)
            scores = self.score_candidates(candidates, processed_train_dataset)
            [_, candidates] = list(
                zip(*sorted(list(zip(scores, candidates)), reverse=True))
            )
            candidates = candidates[: self.param_group.get("beam_size")]
            logger.info(f"finished {i}th training turn")

            logger.info("inferring at eval dataset to get best candidate")
            cur_best_prompt, cur_best_acc = self.eval_candidates(
                candidates, processed_eval_dataset
            )
            if cur_best_acc >= best_acc:
                best_acc = cur_best_acc
                best_prompt = cur_best_prompt
            self._update_progress(prompt=best_prompt)
            logger.info(
                f"After {i} training turns, best acc on eval dataset:{best_acc}"
            )

        logger.info(f"task finished with best accuracy {best_acc}")
        return best_prompt

    def _get_gradients(
        self, prompt: str, error_string: str, num_feedbacks: int
    ) -> List[str]:
        """Get "gradients" for a prompt based on the error string."""
        # 这里传进来的num_feedbacks给的也是一个
        gradient_prompt = f"""
        I'm trying to refine a prompt for large language model.

        My current prompt is:
        "{prompt}"

        But this prompt gets the following examples wrong:
        {error_string}

        Please focus on the difference of ground truth and prediction,
        and give {num_feedbacks} reasons why the prompt could have gotten these examples wrong.
        Wrap each reason with <START> and <END>
        """
        gradient_prompt = "\n".join(
            [line.lstrip() for line in gradient_prompt.split("\n")]
        )  # 这个地方已经避免了出现问题

        response = self.optim_model.chat_single_message(
            gradient_prompt
        )  # n等于1 所以只生成1条reason
        feedbacks = []
        for r in response:
            feedbacks += self.parse_tagged_text(r, "<START>", "<END>")
        return feedbacks
