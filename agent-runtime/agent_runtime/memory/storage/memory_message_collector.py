import asyncio
from typing import Any

from jiuwen.common.llm_service.messages import AIMessage, HumanMessage
from jiuwen.serve.common.message import StreamingCollector
from jiuwen.serve.controllers.execution.enum import ConversationEvent
from memory.storage.user_profile_memory_extractor import get_instance


class UserProfileMemoryMessageCollector(StreamingCollector):
    def __init__(
        self,
        user_query: str,
        user_id: str,
        app_id: str,
        conversation_id: str,
        ir_data: dict,
    ):
        super().__init__()
        self.user_query: str = user_query
        self.user_id: str = user_id
        self.app_id: str = app_id
        self.conversation_id: str = conversation_id
        self.ir_data: dict = ir_data

    def filter(self, message: Any) -> bool:
        # 只获取流式响应的message_end作为记忆提取的消息
        return message.event == ConversationEvent.MESSAGE_END

    async def done(self) -> None:
        filter_messages = self.messages()
        if not filter_messages:
            pass

        messages_to_extract_memory = [HumanMessage(content=self.user_query)]
        for msg in filter_messages:
            answer = msg.data.get("answer")
            messages_to_extract_memory.append(AIMessage(content=answer))
        asyncio.create_task(
            get_instance().async_add_chat_turn(
                self.user_id,
                self.app_id,
                self.conversation_id,
                self.ir_data,
                messages_to_extract_memory,
            )
        )
