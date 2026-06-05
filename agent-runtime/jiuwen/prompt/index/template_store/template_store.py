#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
Interface for template index
"""

import copy
import json
from abc import ABC, abstractmethod
from typing import List, Union

import numpy as np
from pydantic import BaseModel, field_validator, Field


class Template(BaseModel):
    """template class

    Args:
        name (str): template name, unique of a template.
        content (Union[list, str]): content of a template.
        user_id (str): user id. Default value: ``default_user``
        embedding (array): vectorized representation of template. Default: ``None``.
        model (dict): model name and version. Default: ``None``.

            name (str): model name.
            version (str): model version.
            max_tokens_len (int): max token length of model.

        assemble_config (dict):  Configuration for assembling the final output from the template.
            ariable_config (dict): Configuration for handling template variables. Default: ``{"type": "None"}``.
            refiner_config (dict): Configuration for refining the generated output. Default: ``{"type": "None"}``.

        description (str): template description. Default: ``None``.
        function (List): built-in functions. Default: ``None``.

        meta_template_type (str): Type of the template. Default: ``None``.
        filters (dict): Optional filter conditions applied to the template during execution or
                        retrieval. Default: ``None``.

    Examples:
        >>> template = Template(
        ...    name="template_test",
        ...    content=[{"role": "user", "content": "template test, {{query}}"}]
        ... )
    """

    name: str
    content: Union[List, str]
    user_id: str = Field(default="")
    embedding: Union[np.ndarray, None] = Field(default=None)
    model: dict = Field(
        default_factory=lambda: {"name": "", "version": "", "max_tokens_len": 4096}
    )
    description: Union[str, None] = Field(default=None)
    function: Union[List, None] = Field(default=None)
    assemble_config: dict = Field(
        default_factory=lambda: {
            "refiner_config": {"type": "None"},
            "variable_config": {},
        }
    )
    meta_template_type: str = Field(default=None)
    filters: dict = Field(default=None)

    class Config:
        """config class"""

        arbitrary_types_allowed = True

    @field_validator("assemble_config")
    @classmethod
    def assemble_config_check(cls, value):
        """assemble config check function"""
        refiner_config = "refiner_config"
        if refiner_config not in value or len(value[refiner_config]) == 0:
            value[refiner_config] = dict(type="None")
        if "variable_config" not in value:
            value["variable_config"] = dict()
        return value


class TemplateStore(ABC):
    """Template operation store"""

    @staticmethod
    def _convert_to_str(input_template: Template) -> dict:
        """convert template value to string"""
        template = copy.deepcopy(input_template)
        for attr, value in template.__dict__.items():
            if value is None:
                setattr(template, attr, "")
            elif isinstance(value, dict):
                setattr(template, attr, json.dumps(value))
            else:
                setattr(template, attr, str(value))
        return template.dict()

    @abstractmethod
    def delete_template(self, name: str, filters: dict) -> bool:
        """delete template"""

    @abstractmethod
    def register_template(self, template: Template) -> bool:
        """register template"""

    @abstractmethod
    def search_template(self, name: str, filters: dict) -> Template:
        """search template used userid and template name"""

    @abstractmethod
    def update_template(self, doc: Template, **kwargs) -> bool:
        """update template used userid and template name"""
