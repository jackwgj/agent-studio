#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
A module for project initialization.
It is used to register and load prompt templates, plugins, and environment variables.
"""

import importlib.util
import inspect
import json
import os
import sys
from json import JSONDecodeError
from os import listdir
from pathlib import Path
from typing import Optional, List, Callable

import yaml
from i18n.language_processer import LanguageManager
from jiuwen.common.config import config
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.log.base import logger
from jiuwen.common.store.gauss import DBUtil
from jiuwen.orchestration.flow.component_class_pool import component_class_pool
from jiuwen.planner.rails.common.utils import (
    import_all_modules_user_directory,
    import_all_modules_from_default_directory,
)
from jiuwen.prompt import TemplateManager, Template

YAML_FILE_NAME = "prompts.yaml"


class JiuWen:
    """
    This class contains methods related to project initialization.
    """

    @staticmethod
    def init(
        prompt_dir: str = None,
        plugin_dir: str = None,
        model_resolver: Callable = None,
        cfg_file: Optional[str] = ...,
    ) -> None:
        """
        Register all prompt templates under prompt_dir , all plugins under plugin_dir , and Loading user-defined
        environment variable information.

        Args:
            prompt_dir: folder directory for registering prompt templates.
            model_resolver : call third-party model configuration
            plugin_dir: folder directory for registering plugins.
            cfg_file: configuration file path

        Returns:
            None

        """
        # Try to load yaml file of user configurations to environ.
        init_cfg(cfg_file=cfg_file)
        init_db_connections()
        # Loading prompt and plugin.
        # if no path specific, try to use the path defined in config.yaml
        init_prompt_and_plugin(prompt_dir=prompt_dir, plugin_dir=plugin_dir)
        init_rails()
        init_language_config()
        ModelFactory().init_resolver(model_resolver)

    @staticmethod
    def server_init(
        prompt_files: List[str], plugin_files: List[str], userid: str = None
    ) -> None:
        """init function for server api"""
        init_db_connections()
        server_init_plugins(plugin_files=plugin_files, userid=userid)
        server_init_prompts(prompt_files=prompt_files, userid=userid)


def _config_to_env(yaml_cfg, prefix=""):
    for key, value in yaml_cfg.items():
        if value is None:
            continue
        if isinstance(value, dict):
            _config_to_env(value, prefix + key.upper() + "_")
        elif isinstance(value, str):
            os.environ[prefix + key.upper()] = value
        else:
            os.environ[prefix + key.upper()] = json.dumps(value)


def _env_to_config(yaml_cfg, prefix=""):
    for key, value in yaml_cfg.items():
        if isinstance(value, dict):
            _env_to_config(value, prefix + key.upper() + "_")
        else:
            env_var_name = prefix + key.upper()
            if env_var_name and os.environ.get(env_var_name):
                env_var_value = os.environ.get(env_var_name)
                try:
                    yaml_cfg[key] = json.loads(env_var_value)
                except JSONDecodeError:
                    yaml_cfg[key] = env_var_value


def init_rails():
    """加载预置护栏动作"""
    custom_rails_config_dir_str = os.environ.get(
        "RAILS_CUSTOM_RAILS_PATH", config.get("rails", {}).get("custom_rails_path")
    )
    if custom_rails_config_dir_str:
        import_all_modules_user_directory(custom_rails_config_dir_str)
    import_all_modules_from_default_directory()


def init_cfg(cfg_file: str = ...) -> None:
    """
    Loading user-defined environment variable information.

    Args:
        cfg_file: file path of a yaml file containing configurations.
            Pass `None` to skip loading.

    Examples:
        >>> from jiuwen.common.init import init_cfg
        >>> init_cfg(cfg_file="./application.yaml")
    """
    if cfg_file is None:
        logger.info("User skip loading yaml configuration file by cfg_file=None")
        return
    if cfg_file is ...:
        # Default init mode.
        # Get default application.yaml from processing working directory.
        cfg_file = os.path.join("./", "application.yaml")
        if not os.path.exists(cfg_file):
            logger.warning(
                "Try to load config by using default path but the default file"
                " 'application.yaml' cannot be found in working directory "
                f"'{os.getcwd()}'. Skip loading configuration."
            )
            return
        logger.info(
            "Loading config from default 'application.yaml'"
            f" in working directory '{os.getcwd()}'."
        )
    else:
        try:
            if not os.path.isfile(cfg_file):
                from jiuwen.common.exception import FileNotFoundException

                raise FileNotFoundException(
                    f"Specified configuration file was not found or is not a file: {cfg_file}"
                )
            logger.info(f"Loading config from the manually specified '{cfg_file}'.")
        except TypeError as e:
            logger.error(f"[TypeError]: {e}")
            raise e

    # loading from specified yaml file.
    with open(cfg_file, "r", encoding="utf-8") as file:
        try:
            cfg_data = yaml.safe_load(file)
        except Exception as e:
            msg = (
                "Configuration loader terminated.It may caused by"
                f"the config file is not a legal yaml file. Detail: [{type(e).__name__}]{e}"
            )
            logger.error(msg)
            from jiuwen.common.exception import JiuWenException

            raise JiuWenException(msg) from e
        _env_to_config(cfg_data)
        _config_to_env(cfg_data)


def init_db_connections() -> None:
    """
    Init database connection.
    """
    if os.environ.get("DATASOURCE_GAUSSDB_PASSWORD"):
        DBUtil.init()


def init_prompt_and_plugin(prompt_dir: str = None, plugin_dir: str = None) -> None:
    """
    Register all prompt templates under prompt_dir and all plugins under plugin_dir.

    Args:
        prompt_dir: folder directory for registering prompt templates.
        plugin_dir: folder directory for registering plugins.

    Returns:
        None

    Examples:
        >>> from jiuwen.common.init import init_prompt_and_plugin
        >>> from jiuwen.prompt.base import Prompt
        >>> from jiuwen.plugin import PluginManager
        >>>
        >>> init_prompt_and_plugin(prompt_dir="./prompts", plugin_dir="./plugins")
        >>> prompt = Prompt.get_prompt(name="PROMPT_TEMPLATE_NAME")
        >>> plugin = PluginManager().get(plugin_name="PLUGIN_NAME", func_name="FUNC_NAME")

    """
    init_prompt(prompt_dir)
    init_plugin(plugin_dir)


def get_plugins_from_dir(dir_path: str) -> list:
    """
    Get all plugin instances from dir_path,
        first, read .py file or .yaml file from dir_path
        second, if .py file, this function will recognize the FunctionPlugin Class and initialize them,
        if .yaml file, this function will use from_yaml() to get a list of RestFulAPIPlugin
        third, return the list of plugins(including RestFulAPIPlugin and FunctionPlugin)
    """
    plugins = []
    head, tail = os.path.split(os.path.realpath(dir_path))
    sys.path.append(head)
    for filename in os.listdir(dir_path):
        if filename.endswith(".py"):
            module_name = os.path.splitext(filename)[0]
            module_name = ".".join([tail, module_name])
            module = importlib.import_module(module_name)
            for _, obj in inspect.getmembers(module, inspect.isclass):
                if obj.__module__ == "jiuwen.plugin.decorator.plugin":
                    instance = obj()
                    plugins.append(instance)
        elif filename.endswith(".yaml") or filename.endswith(".yml"):
            from jiuwen.plugin import from_yaml

            restful_api_plugins = from_yaml(os.path.join(dir_path, filename))
            plugins.extend(restful_api_plugins)
    return plugins


def init_plugin(plugin_dir: str = None) -> None:
    """
    Register plugins,
        first, get the plugin_dir, if plugin_dir is not existed, no plugin will be registered.
        second, get all plugins(including RestFulAPIPlugin and FunctionPlugin) need to be registered by
        get_plugins_from_dir().
        third, use the PluginManager to register all plugins.
    """
    # If plugin_dir is provided by the user, verify its existence
    # Otherwise, use user-provided plugin_dir or get the default one from config.yaml
    if plugin_dir is not None:
        if not isdir_strict(plugin_dir):
            from jiuwen.common.exception import FileNotFoundException

            raise FileNotFoundException(
                f"The specified plugin directory"
                f" was not found or is not a directory: {plugin_dir}"
            )
    else:
        plugin_dir = config.get("plugin", dict()).get("default_dir")
        if (plugin_dir is None) or not isdir_strict(plugin_dir):
            logger.warning(
                "User does not specific a plugin directory,"
                " and the default directory is not found."
                " Skip init the PluginManager."
            )
            return
        logger.info(
            "User does not specific a plugin directory,"
            " and use the default directory"
            " to init the PluginManager."
        )

    plugins = get_plugins_from_dir(plugin_dir)
    from jiuwen.plugin import PluginManager

    plugin_manager = PluginManager()
    result = plugin_manager.register(plugins, force=True)
    from jiuwen.plugin.common.exception import CommonMessage

    if result.get("message") == CommonMessage.SuccessMsg:
        for plugin in plugins:
            logger.info(f"Add plugin to PluginManager: {plugin.name}")
    else:
        logger.error(f"Auto register plugins failed: {result.get('message')}")


def server_init_plugins(plugin_files: list, userid) -> None:
    """
    init plugins for server api.

    Args:
        plugin_files(List[Tuple]): plugin file contents.
        userid(str): for future usage.

    Returns: None

    """
    plugins = []
    from jiuwen.plugin import from_yaml

    for _, plugin_file in plugin_files:
        restful_api_plugins = from_yaml(plugin_file)
        plugins.extend(restful_api_plugins)
    from jiuwen.plugin import PluginManager

    plugin_manager = PluginManager()
    result = plugin_manager.register(plugins, force=True)
    from jiuwen.plugin.common.exception import CommonMessage

    if result.get("message") == CommonMessage.SuccessMsg:
        for plugin in plugins:
            keys = plugin.api_map.keys()
            logger.info(
                f"Succeed to register plugin {plugin.name!r} with {len(keys)} "
                f"RestfulAPIPlugin(s): {', '.join(repr(k) for k in keys)}"
            )
    else:
        logger.error(f"Auto register plugins failed: {result.get('message')}")


def init_prompt(prompt_dir: str = None) -> None:
    """
    Register prompts,
        use the TemplateManager to register all prompts.
    """
    # Use the user-provided prompt_dir or the default one
    template_manager = TemplateManager()
    # loading customer templates
    template_manager.init_prompt_templates()
    if prompt_dir:
        template_manager.register_in_bulk(prompt_dir)
    else:
        # Get the default prompt_dir from "config.yaml"
        prompt_dir = config.get("prompt", dict()).get("default_dir")
        if (prompt_dir is None) or not os.path.exists(prompt_dir):
            logger.warning(
                "User does not specific a prompt directory,"
                " and the default directory is not found."
                " Skip init the TemplateManager of prompt."
            )
            return
        logger.info(
            "User does not specific a prompt directory,"
            " and use the default directory"
            " to init the TemplateManager of prompt."
        )
        template_manager.register_in_bulk(prompt_dir)


def init_language_config() -> None:
    language_manager = LanguageManager()


def server_init_prompts(prompt_files: list, userid: str) -> None:
    """
    init prompts for server api.
    Args:
        prompt_files: prompts file contents
        userid: for future usage.

    Returns: None

    """
    prompt_info_list = [
        prompt_tuple
        for prompt_tuple in prompt_files
        if isinstance(prompt_tuple[1], dict)
    ]
    metadata = dict()
    length_prompt_info = len(prompt_info_list)
    if not prompt_info_list:
        logger.debug(
            "If user wants to add descriptions for prompts,"
            "please identify the description yaml file"
        )
    elif length_prompt_info > 1:
        desc_filename, _ = prompt_info_list[0]
        logger.warning(
            f"Found more than 1 prompt description yaml files, only the file named {desc_filename}.yaml will be used."
        )
    else:
        _, desc_dict = prompt_info_list[0]
        metadata = desc_dict
    templates = []
    # prompt_tuple include (name, content)
    files = [
        prompt_tuple
        for prompt_tuple in prompt_files
        if isinstance(prompt_tuple[1], str)
    ]
    for name, content in files:
        # Add the name, description, and content to the result list
        description = (
            metadata.get(name).get("description") if metadata.get(name) else ""
        )
        templates.append(Template(name=name, description=description, content=content))
    for template in templates:
        TemplateManager().register(template, force=True)
        logger.info(f"Add prompt to TemplateManager: {template.name}")


def isdir_strict(path: str) -> bool:
    """Test if `path` is a directory (case-sensitive)"""
    abs_path = Path(path).absolute()
    if not abs_path.is_dir():
        return False
    resolved = abs_path.resolve()
    if str(abs_path) == str(resolved):
        return True
    if str(abs_path).lower() == str(resolved).lower():
        # is same regardless of case, reject (for case-sensitive)
        return False
    # using listdir to ignore symbol link
    cur = abs_path
    for parent in abs_path.parents:
        if cur.name not in listdir(parent):
            return False
        cur = parent
    return True


def load_components(extension_path: str = ""):
    """
    初始化flow自定义组件池
    :param extension_path: 拓展路径
    """
    # 获取自定义组件文件夹路径
    if not extension_path:
        extension_path = os.environ.get("JIUWEN_EXTENSION_PATH")
    if not extension_path:
        return
    custom_components_path = os.path.join(extension_path, "extensions", "components")
    if custom_components_path:
        for folder_name in os.listdir(custom_components_path):
            folder_path = os.path.join(custom_components_path, folder_name)
            # 确保是文件夹
            if os.path.isdir(folder_path) and not folder_path.endswith("__pycache__"):
                json_file = None

                # 在文件夹中查找 json 文件
                for file_name in os.listdir(folder_path):
                    if file_name.endswith(".json"):
                        json_file = os.path.join(folder_path, file_name)
                # 确保找到了 json 文件
                if json_file:
                    load_json_file(extension_path, json_file)
                else:
                    raise JiuWenBaseException(
                        error_code=StatusCode.COMPONENT_POOL_INIT_ERROR.code,
                        message=StatusCode.COMPONENT_POOL_INIT_ERROR.errmsg.format(
                            reason=f"Missing json in folder: {folder_path}"
                        ),
                    )


def load_json_file(extension_path, json_file):
    # 读取 json 文件内容
    with open(json_file, "r", encoding="utf-8") as f:
        json_data = json.load(f)
    # 获取 type 和 className
    type_key = json_data.get("type")
    class_name = json_data.get("classInfo", {}).get("className")
    py_file = os.path.join(
        extension_path, json_data.get("classInfo", {}).get("filePath")
    )
    if type_key and class_name and py_file:
        # 动态加载 py 文件中的类
        module_name = os.path.splitext(os.path.basename(py_file))[0]
        spec = importlib.util.spec_from_file_location(module_name, py_file)
        module = importlib.util.module_from_spec(spec)
        sys.modules[module_name] = module
        spec.loader.exec_module(module)

        # 获取类对象
        cls = getattr(module, class_name, None)
        if cls:
            # 将类对象存入自定义组件池
            component_class_pool.register_component_class(type_key, cls)
        else:
            raise JiuWenBaseException(
                error_code=StatusCode.COMPONENT_POOL_INIT_ERROR.code,
                message=StatusCode.COMPONENT_POOL_INIT_ERROR.errmsg.format(
                    reason=f"Class '{class_name}' not found in {py_file}"
                ),
            )
    else:
        raise JiuWenBaseException(
            error_code=StatusCode.COMPONENT_POOL_INIT_ERROR.code,
            message=StatusCode.COMPONENT_POOL_INIT_ERROR.errmsg.format(
                reason=f"Invalid JSON data in {json_file}: missing type or className or file_path"
            ),
        )
