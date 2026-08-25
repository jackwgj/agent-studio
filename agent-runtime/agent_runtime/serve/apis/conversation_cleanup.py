"""Authenticated internal API for conversation workspace cleanup."""

from __future__ import annotations

import hmac

from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel, ConfigDict, Field, field_validator

from agent_runtime.common.config import settings
from agent_runtime.conversation.workspace_cleanup import delete_conversation_workspace


conversation_cleanup_router = APIRouter(tags=["conversation-cleanup-internal"])


class ConversationWorkspaceCleanupReq(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="forbid")

    project_id: str = Field(alias="projectId")
    workspace_id: str = Field(alias="workspaceId")
    user_id: str = Field(alias="userId")
    conversation_id: str = Field(alias="conversationId")

    @field_validator("project_id", "workspace_id", "user_id", "conversation_id")
    @classmethod
    def reject_blank_identity(cls, value: str) -> str:
        if not value or not value.strip():
            raise ValueError("cleanup identity fields must not be blank")
        return value.strip()


@conversation_cleanup_router.post("/internal/v1/conversation/workspace/cleanup")
async def cleanup_conversation_workspace(
    request: ConversationWorkspaceCleanupReq,
    token: str = Header(default="", alias="X-Conversation-Cleanup-Token"),
) -> dict[str, bool]:
    configured = settings.security_sandbox.cleanup_internal_token
    if not configured or not token or not hmac.compare_digest(configured, token):
        raise HTTPException(status_code=401, detail="invalid cleanup credential")
    await delete_conversation_workspace(
        request.project_id,
        request.workspace_id,
        request.user_id,
        request.conversation_id,
    )
    return {"deleted": True}
