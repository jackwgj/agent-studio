#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
agentBuilder chain TemplateManager
"""

import copy
import os
from typing import List, Callable, Tuple

import yaml
from jiuwen.common.configs.env_constants import PROMPT_DEFAULT_TEMPLATES_PATH_KEY
from jiuwen.common.language import Language
from jiuwen.common.log.base import logger
from jiuwen.common.utils.singleton import Singleton
from jiuwen.prompt.assemble.message_handler import (
    template_to_messages,
    messages_to_template,
)
from jiuwen.prompt.common.config import PromptConfig
from jiuwen.prompt.index.template_store import (
    TemplateStore,
    Template,
    TEMPLATE_STORE_REGISTRY,
)
from jiuwen.prompt.index.template_store.store_type import StoreType
from jiuwen.prompt.template.prompt_storage import PromptStorage
from pydantic import ValidationError

MAX_FILE_SIZE_LIMIT = 10 * 1024 * 1024
FILES_LENGTH_LIMIT = 1000
NULL_TEMPLATE_TAG = "_"


def default_template_filter(default_filters) -> dict:
    """default_template_filter"""
    return default_filters


class TemplateManager(metaclass=Singleton):
    """TemplateManager provides the full life cycle management of templates,
    including template registration, deregistration, and obtaining.

    """

    template_store: TemplateStore
    __filter_func: Callable = None
    __lazy_flag: bool = False
    prompt_storage: PromptStorage = None

    def __init__(self):
        self.__load_config()
        self.__filter_func = default_template_filter

    @staticmethod
    def load_from_dir(
        dir_path: str, suffix: str = ".pr", dir_with_language: bool = False
    ) -> Tuple[List[Template], bool]:
        """Read all templates from the dir_path

        Args:
            dir_path: templates from dir_path to register.
            suffix: suffix of template file. Default ``.pr``.
            dir_with_language: Whether the directory structure is organized by language.
        Returns:
            Tuple[List[Template], bool]:
                - templates (List[Template]): A list of successfully loaded Template objects.
                - success (bool): Indicates whether at least one template was loaded.
        """
        if dir_with_language:
            templates = TemplateManager._load_from_dir_with_language(dir_path, suffix)
            if templates:
                return templates, True
        return TemplateManager._load_from_dir_with_tag(dir_path, suffix), False

    @staticmethod
    def _load_from_dir_with_tag(dir_path: str, suffix: str = ".pr") -> List[Template]:
        """
        Load template files from a given directory, with optional metadata from a YAML file.
        """
        abs_path = os.path.abspath(os.path.normpath(dir_path))
        files = os.listdir(abs_path)
        if len(files) > FILES_LENGTH_LIMIT:
            raise ValueError(
                f"Files under directory exceed limits: {FILES_LENGTH_LIMIT}"
            )
        for f in files:
            if os.path.getsize(os.path.join(dir_path, f)) > MAX_FILE_SIZE_LIMIT:
                raise ValueError(
                    f"File size under directory exceed limits: {MAX_FILE_SIZE_LIMIT}"
                )

        yaml_file_name = "prompts.yaml"
        # find the yaml file firstly
        yaml_file = next((f for f in files if f == yaml_file_name), None)
        if not yaml_file:
            yaml_file = next(
                (f for f in files if f.endswith(".yaml") or f.endswith(".yml")), None
            )
        metadata = dict()
        if yaml_file:
            # Read the yaml file
            with open(os.path.join(dir_path, yaml_file), "r", encoding="utf-8") as f:
                metadata = yaml.safe_load(f)
        else:
            logger.debug(
                "If user wants to add descriptions for prompts,"
                "please identify the description yaml"
            )
        templates = []
        files = [f for f in files if f.endswith(suffix)]
        for template_file in files:
            # Get the file name without the extension
            name = os.path.splitext(template_file)[0]
            # Find the corresponding metadata
            with open(
                os.path.join(dir_path, template_file), "r", encoding="utf-8"
            ) as f:
                content = f.read()
            # Add the name, description, and content to the result list
            description = (
                metadata.get(name).get("description") if metadata.get(name) else ""
            )
            language = (
                metadata.get(name).get("language", "") if metadata.get(name) else ""
            )
            tmpl = Template(
                name=name,
                description=description,
                content=content,
                filters={"language": language} if language else {},
            )
            templates.append(tmpl)
        return templates

    @staticmethod
    def _load_from_dir_with_language(
        dir_path: str, suffix: str = ".pr"
    ) -> List[Template]:
        """Load template files from subdirectories organized by language tags."""
        base_dir_realpath = os.path.realpath(dir_path)
        tag_dirs = os.listdir(dir_path)
        all_templates = []
        for tag_name in tag_dirs:
            template_path = os.path.join(dir_path, tag_name)

            # 规范化 template_path
            template_path_realpath = os.path.realpath(template_path)

            # 确保 template_path 在 allowed_base_dir 内
            if not os.path.isdir(
                template_path
            ) or not template_path_realpath.startswith(base_dir_realpath):
                continue
            templates = TemplateManager._load_from_dir_with_tag(template_path, suffix)
            for template in templates:
                template.filters = dict(tag=tag_name)
            all_templates.extend(templates)
        return all_templates

    @staticmethod
    def _get_default_language() -> str:
        """Retrieve the default language."""
        return Language.get_context_language() or Language.get_default_language() or ""

    def init_storage_templates(self, prompt_storage: PromptStorage = None):
        """dynamic uploading template, loads the template into memory by PromptStorage instance"""
        if prompt_storage is None:
            logger.warning("Init_storage_templates failed, caused by no prompt storage")
            return
        self.prompt_storage = prompt_storage
        templates = self.prompt_storage.get_prompts()
        for template in templates:
            try:
                template = Template(**template)
            except ValueError as e:
                raise ValueError(
                    "Invalid template to register, please check template type!"
                ) from e
            except ValidationError or ValueError as e:
                raise ValueError(
                    "Invalid template to register, please check template keys!"
                ) from e
            if not self.register(template, force=True):
                raise ValueError("Invalid template to register!")

    def init_filter(self, filter_func: Callable = None):
        """init model resolver"""
        self.__filter_func = filter_func

    def reload(self):
        """re-load config for template manager.

        Args: None

        Returns: None
        """
        self.__load_config()

    def delete(self, name: str, filters: dict = None):
        """delete prompt template by template name.

        Args:
            name (str): template name
            filters (dict): filters content.

        Returns:
            bool, return True If delete success, else return False

        Raises:
            TemplateNotFoundException: Template is not found to delete.
        """
        all_filters = self.__align_filters(self.__filter_func(filters))
        return self.template_store.delete_template(name, filters=all_filters)

    def get(self, name: str, filters: dict = None) -> Template:
        """query prompt template by template name.

        Args:
            name (str): template name.
            filters (dict): filters content.

        Returns:
            Template, Template.
        """
        all_filters = self.__align_filters(self.__filter_func(filters))
        result_template = self.template_store.search_template(
            name=name, filters=all_filters
        )
        if result_template:
            transed_result = template_to_messages(result_template.content)
            if transed_result:
                result_template.content = transed_result
        return result_template

    def register(self, template: Template, force: bool = False):
        """delete prompt template by template name.

        Args:
            template (Template): template class.
            force(bool): If True, force coverage, Default ``False``.

        Raises:
            TemplateDuplicatedException: Template is duplicated to register.

        Returns:
            bool, return True If register success, else return False.
        """
        cpy_template = copy.deepcopy(template)
        cpy_template.filters = self.__align_filters(cpy_template.filters)
        if isinstance(cpy_template.content, list):
            cpy_template.content = messages_to_template(cpy_template.content)
        if force:
            return self.template_store.update_template(cpy_template)
        return self.template_store.register_template(cpy_template)

    def register_in_bulk(self, dir_path: str) -> bool:
        """
        templates register in bulk from specify dir path.

        Note:
            file content must be formatted to ``text``.
            file suffix only allow to use ``.pr``.
            The ``prompts.yaml`` file in ``dir_path`` is used to configure the detailed information about each template.
            prompt file as :

            >>> prompt_name:
            >>>     description: "your description"

        Args:
            dir_path (str): templates from dir_path to register.

        Returns:
            bool. return True If all templates register success, else return False.

        Raises:
            NotADirectoryError: dir_path is not a directory.
        """
        if not os.path.isdir(dir_path):
            raise NotADirectoryError("dir_path is not a folder")
        templates, _ = self.load_from_dir(
            dir_path=dir_path, dir_with_language=bool(self._get_default_language())
        )
        result = []
        for template in templates:
            result.append(self.register(template, force=True))
        return all(result)

    def init_prompt_templates(self):
        """init_prompt_templates"""
        self.__init_default_templates()
        self.__init_customer_templates()

    def __init_customer_templates(self):
        """init_customer_templates"""
        customer_env = os.environ.get(
            PROMPT_DEFAULT_TEMPLATES_PATH_KEY, None
        )  # PATH TO USER PROMPT
        if not customer_env:
            logger.warning("Customer template path is None")
            return
        self.__load_from_templates_dir(root_dir=customer_env)

    def __init_default_templates(self):
        """init_default_templates"""
        dir_path = os.path.join(os.path.dirname(__file__), "../../resource")
        self.__load_from_templates_dir(root_dir=dir_path)

    def __load_from_templates_dir(self, root_dir):
        """Load template from the directory."""
        templates_path = os.path.join(root_dir, "templates")
        files = os.listdir(templates_path)
        for file_name in files:
            template_path = os.path.join(templates_path, file_name)
            if not os.path.isdir(template_path):
                continue
            templates, load_from_language_dir = TemplateManager.load_from_dir(
                dir_path=template_path,
                dir_with_language=bool(self._get_default_language()),
            )
            for template in templates:
                if load_from_language_dir:
                    template.filters.update({"language": file_name})
                else:
                    template.filters = dict(
                        tag=file_name, language=self._get_default_language()
                    )
                if not self.register(template=template, force=True):
                    raise ValueError("Invalid template to register!")

    def __load_config(self):
        """load config"""
        try:
            cfg = PromptConfig()
            self.match_type = StoreType.match(cfg["template_store_type"])
            logger.info(f"Config template store type {cfg['template_store_type']}")
            if self.match_type:
                logger.info(f"Using match type: {self.match_type}")
                self.template_store = TEMPLATE_STORE_REGISTRY[self.match_type]()
            else:
                raise ValueError("invalid template store type")
        except ValueError as error:
            logger.error("Invalid template store type")
            raise ValueError("invalid template store type") from error

    def __align_filters(self, filters: dict):
        """align_filters"""
        if not filters:
            return dict(tag=NULL_TEMPLATE_TAG, language=self._get_default_language())
        # 兼容性处理，后续统一使用tag字段
        return dict(
            tag=filters.get("tag", "")
            or filters.get("model_name", "")
            or NULL_TEMPLATE_TAG,
            language=filters.get("language", "") or self._get_default_language(),
        )


class InnerTemplate(metaclass=Singleton):
    """inner template"""

    def __init__(self):
        self.__lazy_flag: bool = False
        self._inner_template_store = TEMPLATE_STORE_REGISTRY[StoreType.IN_MEMORY]()
        self.__lazy_loading()

    def register_in_bulk(self, dir_path: str) -> bool:
        """register_in_bulk"""
        if not os.path.isdir(dir_path):
            raise NotADirectoryError("register_in_bulk is not a folder")
        templates, _ = TemplateManager.load_from_dir(dir_path=dir_path)
        result = []
        for template in templates:
            result.append(self._inner_template_store.register_template(template))
        return all(result)

    def get(self, name: str = ""):
        """get inner template"""
        result_template = self._inner_template_store.search_template(
            name=name, filters={}
        )
        return result_template

    def __lazy_loading(self):
        """lazy loading"""
        if self.__lazy_flag:
            return
        if not self.__lazy_flag:
            self.__lazy_flag = True
        current_path = os.path.join(os.path.dirname(__file__))
        self.__load_default_templates(os.path.join(current_path, "default"))
        self.__load_meta_templates(os.path.join(current_path, "meta"))

    def __load_meta_templates(self, full_path):
        """load_meta_templates"""
        if not self.register_in_bulk(dir_path=full_path):
            raise ValueError("meta template register failed!")

    def __load_default_templates(self, full_path):
        """load default templates"""
        files = os.listdir(full_path)
        for file in files:
            template_path = os.path.join(full_path, file)
            if not os.path.isdir(template_path):
                continue
            if not self.register_in_bulk(dir_path=template_path):
                raise ValueError("Default template register failed!")
        result = self.register_in_bulk(dir_path=full_path)
        if not result:
            logger.error("Default templates loaded failed!")
