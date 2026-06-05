"""IR 构建相关异常"""

from agent_runtime.common.exception.errors import AgentBuilderError, ExtensionStatusCode


class IRBuildException(AgentBuilderError):
    """IR 构造异常"""

    def __init__(self, msg: str = "", **kwargs):
        super().__init__(ExtensionStatusCode.IR_BUILD_ERROR, msg=msg, **kwargs)
