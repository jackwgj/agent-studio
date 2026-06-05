#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
TemplateEditor
"""

from abc import abstractmethod

from jiuwen.prompt.mining.template.meta_template.placeholder_type import PlaceholderData
from pydantic import BaseModel, Field


class TemplateEditor(BaseModel):
    """TemplateEditor"""

    def __call__(self, concrete_template: str):
        self.run(concrete_template)

    @abstractmethod
    def run(self, concrete_template: str):
        """abstract method of run"""
        raise NotImplementedError("run method not implemented")

    @abstractmethod
    def stream_run(self, concrete_template: str):
        """abstract method of stream_run"""
        raise NotImplementedError("run method not implemented")


class PlaceholderEditor(TemplateEditor):
    """Editor for injecting placeholder blocks into templates."""

    placeholders: list[PlaceholderData] = Field(default=[])

    def run(self, concrete_template: str):
        """Inject missing placeholders into the template."""
        if self.placeholders:
            for placeholder in self.placeholders:
                if (placeholder.value) and (
                    placeholder.format() not in concrete_template
                ):
                    concrete_template += placeholder.format()
        return concrete_template

    def stream_run(self, concrete_template: str):
        """Stream template characters and placeholder injection events."""
        for char in concrete_template:
            yield char
        for placeholder in self.placeholders:
            yield dict(code=0, message="success", data=placeholder.format())
