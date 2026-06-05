#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
agentBuilder chain in memory store
"""

import json

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.language import Language
from jiuwen.common.log.base import logger
from jiuwen.prompt.common.document import Document
from jiuwen.prompt.common.exception.template import TemplateDuplicatedException
from jiuwen.prompt.index.docstore.in_memory import InMemory, TemplateId
from jiuwen.prompt.index.template_store import (
    TemplateStore,
    Template,
    register_template_store,
)
from jiuwen.prompt.index.template_store.store_type import StoreType


@register_template_store(StoreType.IN_MEMORY)
class InMemoryTemplateStore(TemplateStore):
    """In memory Template Store class"""

    def __init__(self):
        """"""
        self.index = InMemory()

    @staticmethod
    def __format_template_name(name: str, filters: dict):
        if not filters:
            return name
        ordered_kv = [f"{key}#{filters.get(key)}" for key in sorted(filters.keys())]
        return name + "".join("###" + kv_pair for kv_pair in ordered_kv)

    @staticmethod
    def __parse_document(doc: dict):
        """__parse_document"""
        try:
            model = json.loads(doc.get("model"))
            assemble_config = json.loads(doc.get("assemble_config", "{}"))
        except Exception as error:
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_DATA_INCORRECT_ERROR.code,
                message=StatusCode.PROMPT_DATA_INCORRECT_ERROR.errmsg,
            ) from error
        template_obj = Template(
            name=doc.get("name"),
            content=doc.get("content"),
            user_id=doc.get("user_id", ""),
            embedding=None,
            model=model,
            description=doc.get("description", ""),
            function=None,
            assemble_config=assemble_config,
            meta_template_type=doc.get("meta_template_type", ""),
        )
        return template_obj

    def delete_template(self, name: str, filters: dict):
        """remove template"""
        template_name = self.__format_template_name(name=name, filters=filters)
        template_id = TemplateId(template_name, filter_data=filters)
        if not self.index.get_documents(template_id=template_id):
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_TEMPLATE_NOT_FOUND_ERROR.code,
                message=StatusCode.PROMPT_TEMPLATE_NOT_FOUND_ERROR.errmsg.format(
                    error_msg=f"Template: {name} not found to delete"
                ),
            )
        return self.index.delete_document(template_id=template_id)

    def register_template(self, template: Template):
        """register template"""
        template_name = self.__format_template_name(
            name=template.name, filters=template.filters
        )
        template_id = TemplateId(template_name, filter_data=template.filters)
        if self.index.get_documents(template_id=template_id):
            raise TemplateDuplicatedException("The template is duplicated to register")
        return self.index.add_document(
            template_id=template_id,
            record=Document(page_content="", metadata=self._convert_to_str(template)),
        )

    def search_template(self, name: str, filters: dict):
        """search template"""
        result = self.__get_document(name=name, filters=filters)
        if filters and not result:
            logger.debug("Template not found, falling back to default template")
            result = self.__get_document(
                name=name,
                filters={"tag": "default", "language": filters.get("language", "")},
            )
            if not result and filters.get("language"):
                # 给定语言没有默认模板，则基于默认语言继续搜索
                result = self.__get_document(
                    name=name,
                    filters={
                        "tag": "default",
                        "language": Language.get_default_language(),
                    },
                )
        if not result:
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_TEMPLATE_NOT_FOUND_ERROR.code,
                message=StatusCode.PROMPT_TEMPLATE_NOT_FOUND_ERROR.errmsg.format(
                    error_msg=f"template name: {name}"
                ),
            )
        return self.__parse_document(doc=result)

    def update_template(self, doc: Template, **kwargs):
        """update template"""
        template_id = TemplateId(
            name=self.__format_template_name(name=doc.name, filters=doc.filters),
            filter_data=doc.filters,
        )
        return self.index.update_document(
            template_id=template_id,
            data=Document(page_content="", metadata=self._convert_to_str(doc)),
        )

    def __get_document(self, name: str, filters: dict):
        """__get_document"""
        template_id = TemplateId(
            name=self.__format_template_name(name=name, filters=filters),
            filter_data=filters,
        )
        return self.index.get_documents(template_id=template_id)
