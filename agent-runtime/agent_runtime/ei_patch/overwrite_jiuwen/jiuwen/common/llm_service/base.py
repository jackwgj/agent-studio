#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
LLM Service Base
"""

# @override(jiuwen) adapted for NL2AGENT intent detection; decouple nl2_agentBuilder_model.py post-processing dependency
import importlib.util
import json
import os
from typing import Callable, Optional

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.utils.singleton import Singleton

NL2_AGENT = "nl2_agentBuilder_model"


def default_model_resolver(model_type, model_name, **kwargs) -> Optional[dict]:
    """get default model config information"""
    model_dict_str = os.environ.get("DEFAULT_MODELS", "{}")
    try:
        model_dict = json.loads(model_dict_str)
        return model_dict.get(model_type, {}).get(model_name)
    except Exception as err:
        raise JiuWenBaseException(
            StatusCode.LLM_RESOLVER_DECODER_ERROR.code,
            StatusCode.LLM_RESOLVER_DECODER_ERROR.errmsg.format(
                error_msg="default_models error: Decoder error"
            ),
        ) from err


class ModelFactory(metaclass=Singleton):
    """Model Factory"""

    def __init__(self):
        """init Model Factory"""
        self.model_map = {}
        current_dir = os.path.dirname(os.path.abspath(__file__))
        default_model_dir = os.path.join(current_dir, "language_model")
        self._load_model_dir(default_model_dir)
        self._load_model_dir(os.path.join(current_dir, "search_model"))
        custom_model_dir = os.getenv("MODEL_DIR", None)  # "/path/to/custom/models"
        if custom_model_dir:
            self._load_model_dir(custom_model_dir)
        self.default_model_resolver = default_model_resolver
        self.model_resolver = None

    @staticmethod
    def _load_models(model_dir):
        """load models from model_dir"""
        try:
            py_files = [
                f
                for f in os.listdir(model_dir)
                if (f.endswith(".py") or f.endswith(".pyc"))
                and f != "base.py"
                and f != "base.pyc"
            ]
            modules_name = [
                os.path.splitext(os.path.basename(item))[0] for item in py_files
            ]
            model_dict = {}
            for module_name, path_name in zip(modules_name, py_files):
                spec = importlib.util.spec_from_file_location(
                    module_name, os.path.join(model_dir, path_name)
                )
                module = importlib.util.module_from_spec(spec)
                spec.loader.exec_module(module)
                for obj in module.__dict__.values():
                    if (
                        isinstance(obj, type)
                        and issubclass(obj, BaseChatModel)
                        and obj != BaseChatModel
                    ):
                        model_dict.update({module_name: obj})
        except Exception as e:
            raise JiuWenBaseException(
                StatusCode.LLM_LOAD_ERROR.code,
                StatusCode.LLM_LOAD_ERROR.errmsg.format(
                    error_msg="please check model file path."
                ),
            ) from e

        return model_dict

    def get_search_model(self, model_type, deployment_id, api_type, **kwargs):
        """get model instance by model_type and model_name
        Args:
            model_type: consistent with model class type which defined by user or default
            model_tag: actual is model id
            kwargs: other params
        """
        if self.model_resolver:
            model_config = self.model_resolver(
                model_type, deployment_id, **kwargs
            ) or self.default_model_resolver(model_type, deployment_id, **kwargs)
        else:
            model_config = self.default_model_resolver(
                model_type, deployment_id, **kwargs
            )
        if not model_config:
            raise JiuWenBaseException(
                StatusCode.LLM_LOAD_ERROR.code,
                StatusCode.LLM_LOAD_ERROR.errmsg.format(
                    error_msg="Unable to obtain the configuration information of the model, please check "
                    "model configuration is correct"
                ),
            )
        model_config.update(kwargs)
        model_config["api_type"] = api_type
        model_cls = self.model_map.get(model_type, None)
        if not model_cls:
            raise JiuWenBaseException(
                StatusCode.LLM_TYPE_ERROR.code,
                StatusCode.LLM_TYPE_ERROR.errmsg.format(error_msg="model type error"),
            )

        model = model_cls(**model_config)

        return model

    def get_model(self, model_type, model_name, temperature=0.01, top_p=1.0, **kwargs):
        """get model instance by model_type and model_name
        Args:
            model_type: consistent with model class type which defined by user or default
            model_name: actual is model id
            temperature: temperature
            top_p: top_p
            kwargs: other params
        """
        if self.model_resolver:
            model_config = self.model_resolver(
                model_type, model_name, **kwargs
            ) or self.default_model_resolver(model_type, model_name, **kwargs)
        else:
            model_config = self.default_model_resolver(model_type, model_name, **kwargs)
        if not model_config:
            raise JiuWenBaseException(
                StatusCode.LLM_LOAD_ERROR.code,
                StatusCode.LLM_LOAD_ERROR.errmsg.format(
                    error_msg="Unable to obtain the configuration information of the model, please check "
                    "model configuration is correct"
                ),
            )

        model_config["temperature"] = temperature
        model_config["top_p"] = top_p
        model_config.update(kwargs)
        if model_type == NL2_AGENT:
            model_cls = self.model_map.get(model_type, None)
        else:
            model_cls = self.model_map.get(model_config["model_type"], None)
            model_config.pop("model_type")
        if not model_cls:
            raise JiuWenBaseException(
                StatusCode.LLM_TYPE_ERROR.code,
                StatusCode.LLM_TYPE_ERROR.errmsg.format(error_msg="model type error"),
            )

        model = model_cls(**model_config)

        return model

    def init_resolver(self, model_resolver: Callable = None):
        """init model resolver"""
        self.model_resolver = model_resolver

    def _load_model_dir(self, model_dir=None):
        """load available model"""
        if model_dir:
            model_dict = self._load_models(model_dir)
            self.model_map.update(model_dict)
