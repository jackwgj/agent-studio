#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Request model"""

from jiuwen.plugin.common.constant import HttpMethod
from jiuwen.plugin.models.base_model import BaseModel, validator, Field


# 有default参数表示实例化的时候可以不写
# min_length=1 搭配 anystr_strip_whitespace = True 表示写了就不能为空
class RegisterItem(BaseModel):
    """RegisterItem model"""

    id: int = Field(title="插件记录id", default=0)
    plugin_name: str = Field(title="插件名", min_length=1)
    plugin_description: str = Field(title="插件描述", min_length=1)
    plugin_dependency: str = Field(title="插件依赖", min_length=1, default="")
    func_name: str = Field(title="函数名", min_length=1)
    func_description: str = Field(title="函数描述", min_length=1)
    url: str = Field(title="函数url", min_length=1, default="")
    method: str = Field(title="访问函数url的方法类型", min_length=1, default="get")
    result: str = Field(title="访问函数url返回的结果样式", min_length=1, default="")
    label: str = Field(min_length=1, default="")
    principle: str = Field(min_length=1, default="")
    headers: str = Field(min_length=1, default="")
    arguments: str = Field(min_length=1, default="")
    callable_function: str = Field(min_length=1, default="")
    auth: str = Field(min_length=1, default="")
    create_by: str = Field(title="插件所有人", min_length=1, default="")
    related_info: dict = Field(min_length=1, default="")
    status: int = Field(title="插件公开状态", default=0)
    user_id: str = Field(min_length=1, default="")
    project_id: str = Field(title="项目id", min_length=1, default="")
    domain_id: str = Field(title="领域id", min_length=1, default="")

    @validator("method")
    def validate_method(self, method: str):
        """校验method， 忽略大小写"""
        if method.lower() not in HttpMethod.__members__.values():
            raise ValueError("method: %s 参数不合法" % method)
        return method

    class Config:
        """config"""

        extra = "forbid"  # 禁止额外的参数
        anystr_strip_whitespace = True  # 自动去除参数中的两端的空格


class UpdateItem(BaseModel):
    """UpdateItem model"""

    plugin_name: str = Field(title="插件名", min_length=1)
    plugin_description: str = Field(title="插件描述", min_length=1, default="")
    plugin_dependency: str = Field(title="插件依赖", min_length=1, default="")
    func_name: str = Field(title="函数名", min_length=1)
    func_description: str = Field(title="函数描述", min_length=1, default="")
    url: str = Field(title="函数url", min_length=1, default="")
    method: str = Field(title="访问函数url的方法类型", min_length=1, default="")
    result: str = Field(title="访问函数url返回的结果样式", min_length=1, default="")
    label: str = Field(min_length=1, default="")
    principle: str = Field(min_length=1, default="")
    headers: str = Field(min_length=1, default="")
    arguments: str = Field(min_length=1, default="")
    callable_function: str = Field(min_length=1, default="")
    auth: str = Field(min_length=1, default="")
    create_by: str = Field(title="插件所有人", min_length=1, default="")
    related_info: dict = Field(min_length=1, default="")
    status: int = Field(title="插件公开状态", default=0)
    user_id: str = Field(min_length=1, default="")
    project_id: str = Field(title="项目id", min_length=1, default="")
    domain_id: str = Field(title="领域id", min_length=1, default="")

    @validator("method")
    def validate_method(self, method: str):
        """校验method, 忽略大小写"""
        if method.lower() not in HttpMethod.__members__.values():
            raise ValueError("method: %s 参数不合法" % method)
        return method

    class Config:
        """config"""

        extra = "forbid"  # 禁止额外的参数
        anystr_strip_whitespace = True  # 自动去除参数中的两端的空格


class FindTopKItem(BaseModel):
    """FindTopKItem model"""

    query: str = Field(title="插件描述", min_length=1)
    top_k: int = Field(title="返回top k插件", default=3)

    class Config:
        """config"""

        extra = "forbid"  # 禁止额外的参数
        anystr_strip_whitespace = True  # 自动去除参数中的两端的空格


class PluginFLowRefRegisterItem(BaseModel):
    """PluginWorkflowRef register item"""

    id: int = Field(title="插件flow关系id", default=0)
    user_id: str = Field(title="插件所属user_id", min_length=1, default="")
    workflow_id = Field(title="workflow_id", min_length=1)
    task_id: str = Field(title="workflow下task_id", min_length=1, default="")
    plugin_name: str = Field(title="插件名", min_length=1, default="")
    plugin_id: int = Field(title="插件id")
    func_name: str = Field(title="插件下函数名", min_length=1, default="")
    version: str = Field(title="workflow版本号", min_length=1, default="0.0.0")
    label: str = Field(title="冗余信息", min_length=1, default="")

    class Config:
        """config"""

        extra = "forbid"  # 禁止额外的参数
        anystr_strip_whitespace = True  # 自动去除参数中的两端的空格
