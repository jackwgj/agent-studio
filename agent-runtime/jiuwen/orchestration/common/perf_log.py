#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""Used to define orchestration module performance buffered logger."""

from typing import Dict

from jiuwen.common.log.base import performance_logger, get_thread_session
from jiuwen.common.log.perf_log import PerformanceLogBuffer, TOTAL_LOG_TAG


class WorkFlowStage:
    WORKFLOW_INIT = "workflow_init"  # workflow初始化总时长
    WORKFLOW_INIT_SUB = "workflow_init_sub"  # workflow初始化子工作流时长
    WORKFLOW_INIT_CHAT_MANAGER = (
        "workflow_init_chat_manager"  # workflow初始化状态管理器时长
    )
    WORKFLOW_INIT_RUNTIME_CONTEXT = (
        "workflow_init_runtime_context"  # workflow初始化上下文时长
    )
    WORKFLOW_INIT_BPMN = "workflow_init_bpmn"  # workflow初始化bpmn工作流

    WORKFLOW_EXECUTION = "workflow_execution"  # workflow执行时长
    WORKFLOW_EXECUTION_OUTPUT = "workflow_execution_output"  # workflow输出总时长
    WORKFLOW_EXECUTION_COMPONENTS = (
        "workflow_execution_components"  # workflow执行组件时长
    )


class WorkflowPerformanceLogBuffer(PerformanceLogBuffer):
    def record(self, uid: str = None):
        """record perf logs to console/logfile"""
        uid = uid or get_thread_session()
        records = self._perf_records.get(uid)
        if not uid or not records:
            return

        cost_info = records.get_record_map()
        if not cost_info:
            with self._lock:
                self._perf_records.pop(uid, None)
                del records
            return

        total_init_cost, sub_init_cost = self._process_init(cost_info)
        total_execution_cost, execution_output_cost, execution_comp_cost = (
            self._process_execution(cost_info)
        )
        ir_execution_cost = total_execution_cost + execution_output_cost
        extra_info = "|".join(records.extra) if records.extra else ""

        log_str = (
            f"{TOTAL_LOG_TAG}|{extra_info}|{ir_execution_cost}|{total_execution_cost}|{execution_comp_cost}|"
            f"{total_init_cost}|{sub_init_cost}|{execution_output_cost}"
        )
        performance_logger.log(self._log_level, log_str)
        with self._lock:
            self._perf_records.pop(uid, None)
            del records

    def _process_init(self, records: Dict[str, int]):
        """process init info"""
        total_init_cost = records.get(WorkFlowStage.WORKFLOW_INIT, 0)
        sub_init_cost = []
        sub_init_cost.append(records.get(WorkFlowStage.WORKFLOW_INIT_SUB, 0))
        sub_init_cost.append(records.get(WorkFlowStage.WORKFLOW_INIT_CHAT_MANAGER, 0))
        sub_init_cost.append(
            records.get(WorkFlowStage.WORKFLOW_INIT_RUNTIME_CONTEXT, 0)
        )
        sub_init_cost.append(records.get(WorkFlowStage.WORKFLOW_INIT_BPMN, 0))
        return total_init_cost, sub_init_cost

    def _process_execution(self, records: Dict[str, int]):
        total_execution_cost = records.get(WorkFlowStage.WORKFLOW_EXECUTION, 0)
        execution_output_cost = records.get(WorkFlowStage.WORKFLOW_EXECUTION_OUTPUT, 0)
        execution_comp_cost = []
        execution_comp_cost.append(
            records.get(WorkFlowStage.WORKFLOW_EXECUTION_COMPONENTS, 0)
        )
        return total_execution_cost, execution_output_cost, execution_comp_cost


wf_performance_buffer = WorkflowPerformanceLogBuffer()
