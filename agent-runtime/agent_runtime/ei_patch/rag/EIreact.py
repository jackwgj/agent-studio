import json

from jiuwen.orchestration.utils import Input, Output
from jiuwen.planner.common.enum_class import ProviderType


async def ei_ainvoke(self, inputs: Input, **kwargs) -> Output:
    tool_name = inputs.get("tool_name")
    arguments = inputs.get("tool_arguments")

    function_exec_res = None
    if self.provider_type == ProviderType.PLUGIN:
        if tool_name == self.invokable.name or self.invokable.name.startswith(
            tool_name + "#*"
        ):
            aivoke_rsult = await self.invokable.ainvoke(json.loads(arguments), **kwargs)
            function_exec_res = self.function_filter(aivoke_rsult)
    elif self.provider_type == ProviderType.WORKFLOW:
        function_exec_res = self._exec_workflow(arguments, **kwargs)
    elif self.provider_type == ProviderType.MCP:
        function_exec_res = await self.invokable.ainvoke(
            json.loads(arguments), **kwargs
        )
    return function_exec_res
