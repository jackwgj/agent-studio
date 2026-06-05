#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Plugin class"""

from typing import Dict, List


class BasePlugin:
    """Base Plugin"""

    def __init__(self, name: str, description: str, dependency: str):
        self.name: str = name
        self.description: str = description
        self.dependency: str = dependency

    def __str__(self):
        return f"name : {self.name} \ndescription: {self.description} \ndependency: {self.dependency}"

    @staticmethod
    def __getattr__(component_name: str, component_map: Dict = None):
        if component_map:
            for name, value in component_map.items():
                if name == component_name:
                    return value
        return None

    def __getstate__(self):
        return self.__dict__

    def __setstate__(self, state):
        self.__dict__.update(state)


class RestFulAPIPlugin(BasePlugin):
    """Restful API Plugin"""

    def __init__(
        self, name: str = "", description: str = "", dependency: str = "", api_list=None
    ):
        super().__init__(name, description, dependency)
        if api_list is None:
            api_list = []
        # 这里导入RestFulAPI会引起循环引用，先关闭类型注释 self.api_list: List[RestFulAPI] = api_list
        self.api_list: List = api_list
        self.api_map: Dict = {api.name: api for api in api_list}

    def __getattr__(self, api_name: str, _=None):
        return super().__getattr__(api_name, self.api_map)


class FunctionPlugin(BasePlugin):
    """Function Plugin"""

    def __init__(
        self,
        name: str = "",
        description: str = "",
        dependency: str = "",
        native_function_list=None,
    ):
        super().__init__(name, description, dependency)
        if native_function_list is None:
            native_function_list = []
        # 这里导入Function会引起循环引用，先关闭类型注释 self.function_list: List[Function] = native_function_list
        self.function_list = native_function_list
        # 此处的map对应的是 装饰器中的name与function的对应关系
        self.function_map: Dict = {func.name: func for func in native_function_list}
        # 此处的map对应的是 函数名与function的对应关系
        # 这里导入Function会引起循环引用，先关闭类型注释: Dict[str, Function]
        self.function_map_with_function_name: Dict = {
            func.name: func for func in native_function_list
        }

    def __getattr__(self, function_name: str, _=None):
        return super().__getattr__(function_name, self.function_map_with_function_name)
