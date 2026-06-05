#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
workflow validation base model
"""

from typing import List

from pydantic import BaseModel, StrictStr, Field


class WorkflowIrConnection(BaseModel):
    source: dict = Field()
    target: dict = Field()


class WorkflowIrComponent(BaseModel):
    id: StrictStr = Field(alias="id", min_length=1)
    name: StrictStr = Field(alias="name", min_length=1)
    type: StrictStr = Field(alias="type", min_length=1)
    inputs: dict = Field(alias="inputs", default={})
    outputs: dict = Field(alias="outputs", default={})
    configs: dict = Field(alias="configs", default={})


class WorkflowIr(BaseModel):
    workflow_id: StrictStr = Field(alias="workflowId", min_length=1)
    workflow_name: StrictStr = Field(alias="workflowName", min_length=1)
    description: StrictStr = Field(alias="description", min_length=1)
    workflow_version: StrictStr = Field(alias="workflowVersion", min_length=1)
    configs: dict = Field(alias="configs", default={})
    components: List[WorkflowIrComponent] = Field(alias="components")
    connections: List[WorkflowIrConnection] = Field(alias="connections")
