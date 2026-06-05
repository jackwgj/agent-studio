# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
opening dialog and question generation
"""

from jiuwen.prompt.common.config import LLMModelInfo
from jiuwen.prompt.index.template_store.template_store import Template
from jiuwen.prompt.template.prompt_builder import PromptBuilder


class OpeningBuilder:
    """Generate the corresponding opening dialog and three recommended questions
    based on the agent's name and description

    Attributes:
        model (str): name of the model to be accessed.
        url (str): url of the model to be accessed.
    """

    def __init__(self, model_info: LLMModelInfo):
        self.prompt_builder = PromptBuilder.initialize(model_info=model_info)

    def build(self, agent_name: str, agent_description: str) -> Template:
        """Generate opening dialog and questions

        Args:
            agent_name (str): agent name.
            agent_description (str): agent description.

        Returns:
            Template: opening dialog and questions.
        """
        template = self.prompt_builder.hmi_build(
            meta_template="opening_dialog",
            instruction=f"角色：{agent_name}，简介：{agent_description}。",
            history=[],
        )
        return template
