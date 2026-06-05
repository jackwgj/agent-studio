#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""conversation memory implementation"""

from typing import Dict, List

from jiuwen.memory.store.base import BaseConversationMemory
from jiuwen.memory.store.buffer import BufferConversation
from jiuwen.memory.utils import ThreadSafeIDGenerator, batch_check_type


class ConversationMemory:
    """
    Conversation Memory.

    Args:
        user_id (Union[str, None]): user id string. If not given, it would be assigned a unique int id. Default: None.
        session_id (Union[str, None]): session id string. If not given, it would be assigned a unique int id. Default:
        None.
        window (int): window size when retrieving the nearest records. If 0, all records would be returned.Default: 0.
        store (Union[List[Type], None]): history memory types, which should be a list of subclasses of Base
        ConversationMemory. If None, only `BufferConversation` would be activated. Default: None.
        **kwargs (dict): Arguments for stores.
    """

    user_prefix: str = "user"
    assistant_prefix: str = "assistant"
    user_id_generator = ThreadSafeIDGenerator()
    session_id_generator = ThreadSafeIDGenerator()
    all_user_ids = set()
    all_session_ids = set()

    def __init__(
        self,
        user_id: str = None,
        session_id: str = None,
        window: int = 0,
        store: list = None,
        **kwargs,
    ) -> None:
        batch_check_type(
            {
                "user_id": (user_id, (str, None)),
                "session_id": (session_id, (str, None)),
                "window": (window, int),
                "store": (store, [list, None]),
            }
        )
        self.user_id = self._init_user_id(user_id)
        self.session_id = self._init_session_id(session_id)
        ConversationMemory.all_session_ids.add(str(session_id))
        ConversationMemory.all_user_ids.add(str(user_id))
        if window < 0:
            raise ValueError("window must be a positive integer or 0.")
        self.window = window
        args_dict = kwargs["store_params"] if "store_params" in kwargs else {}
        if store is None:
            store = [BufferConversation]
        if not store:
            raise ValueError("At least one store should be specified.")
        self.stores: List[BaseConversationMemory] = []
        self.buffer = BufferConversation(**args_dict)
        self.stores.append(self.buffer)

    @staticmethod
    def _init_user_id(user_id: str) -> str:
        if user_id is not None:
            with ConversationMemory.user_id_generator.get_lock():
                return user_id
        user_id = None
        while user_id is None or user_id in ConversationMemory.all_user_ids:
            user_id = ConversationMemory.user_id_generator.generate_safe_str()
        return str(user_id)

    @staticmethod
    def _init_session_id(session_id: str) -> str:
        if session_id is not None:
            with ConversationMemory.session_id_generator.get_lock():
                return session_id
        session_id = None
        while session_id is None or session_id in ConversationMemory.all_session_ids:
            session_id = ConversationMemory.session_id_generator.generate_safe_str()
        return session_id

    def save_context(self, inputs: str, outputs: str) -> bool:
        """
        save a key-value pair to the memory history.

        Args:
            inputs (Union[str]): User's input string.
            outputs (str): output string of LLM.

        Returns:
            bool，saving successful or not. When the given string exists, the old record will be overwritten.

        """
        if not isinstance(inputs, str) or not isinstance(outputs, str):
            raise TypeError(
                f"inputs and outputs must be str. but got ({type(inputs)}, {type(outputs)})"
            )
        for store in self.stores:
            store.add(inputs, outputs)
        return True

    def to_messages(self) -> Dict[str, object]:
        """
        Convert history to formatted dict.

        Returns:
            Dict[str, object]，containing the history string and so on.
        """
        if self.window == 0:
            message_dict = self.buffer.get_all()
        else:
            message_dict = self.buffer.get_nearest_k(self.window)
        role_splitter, round_splitter = "\n", "\n"
        msg_list = []
        for user_msg, assistant_msg in message_dict.items():
            round_msg = f"{self.user_prefix}:{user_msg}{role_splitter}{self.assistant_prefix}:{assistant_msg}"
            msg_list.append(round_msg)
        return_msg = {"history": round_splitter.join(msg_list)}
        return return_msg
