from typing import Optional

from pydantic import BaseModel, Field


class CommonErrorResponse(BaseModel):
    """
    通用错误响应模型
    """

    error_code: str = Field(..., description="错误码")
    error_msg: str = Field(..., description="错误信息")
    error_reason: Optional[str] = Field(None, description="可能原因")
    error_suggestion: Optional[str] = Field(None, description="修改建议")
