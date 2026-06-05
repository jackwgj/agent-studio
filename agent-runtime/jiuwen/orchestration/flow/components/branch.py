#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
from jiuwen.common.log.base import logger
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.flow.components.util import evaluate_expression
from jiuwen.orchestration.flow.constant import SYSTEM_FIELDS, USER_FIELDS
from jiuwen.orchestration.flow.model.workflow_data_class import WorkflowMetadata
from jiuwen.orchestration.flow.runtime_context import RuntimeContext
from jiuwen.orchestration.utils import Input
from pydantic import BaseModel, Field


class BranchComponentConfig(BaseModel):
    """
    分支组件 配置校验
    """

    branches: list = Field(min_items=1)


class Branch(Invokable):
    """
    The definition of Branch component.

    This class handles the initialization, execution, and evaluation of branch logic
    based on given configurations and input data.
    """

    def __init__(
        self,
        conf: dict,
        metadata: WorkflowMetadata,
        runtime_context: RuntimeContext = None,
    ):
        """
        Initialize Branch component with provided configuration.

        Args:
            conf (dict): Configuration dictionary containing branches.
        """
        super().__init__()
        self.branches = conf.get("branches")
        self.runtime_context = runtime_context
        self._validate_configs()

    def invoke(self, inputs: Input, **kwargs):
        """
        Obtain and evaluate branch judgment conditions based on input data.

        Args:
            inputs (dict): Input data against which branches will be evaluated.

        Returns:
            dict: A dictionary containing the result of the branch evaluation.
        """
        # Initialize userFields in the outputs
        outputs = {SYSTEM_FIELDS: {"result": None}}

        # Remove coefficients of SYSTEM_FIELDS and USER_FIELDS from Inputs
        inputs.pop(SYSTEM_FIELDS, None)
        inputs.pop(USER_FIELDS, None)

        # Iterate through each branch to evaluate its conditions
        for branch in self.branches:
            # Check if the current branch has a boolean expression
            if "boolExpression" in branch:
                # Evaluate the boolean expression for current branch
                # 与循环组件共用表达式判断逻辑
                if evaluate_expression(branch["boolExpression"], inputs, logger):
                    outputs[SYSTEM_FIELDS]["result"] = branch["id"]
                    break
            else:
                outputs[SYSTEM_FIELDS]["result"] = branch.get("default", "default")

        # If no true condition found, set a default or null result
        if outputs[SYSTEM_FIELDS]["result"] is None:
            outputs[SYSTEM_FIELDS]["result"] = "default"

        return outputs

    def _validate_configs(self):
        BranchComponentConfig.model_validate(dict(branches=self.branches))
