"""Process-lifetime sandbox registration, separate from request-owned tools.

One effective configuration is allowed per ResourceMgr. Configuration changes
require a Runtime restart; never replace an operation underneath active requests.
The SDK registry owns the operation for the lifetime of that manager. Request
cleanup must not release its gateway or the externally managed AIO container.
"""

from __future__ import annotations

from dataclasses import dataclass
from threading import RLock
from weakref import WeakKeyDictionary

from openjiuwen.core.sys_operation import OperationMode, SysOperationCard

from agent_runtime.conversation.operation_result import operation_error_detail, operation_succeeded


SHARED_OPERATION_ID = "conversation_sandbox_sys_op_shared"
_registration_lock = RLock()


@dataclass(frozen=True)
class _Registration:
    card: SysOperationCard
    operation: object


_registrations: WeakKeyDictionary = WeakKeyDictionary()


def get_conversation_sandbox_operation(resource_manager, card: SysOperationCard):
    """Atomically register or borrow only our dedicated SANDBOX operation.

    No business identity or cwd is cached here. Those belong to execution contexts
    and request-scoped tool closures. The lock covers synchronous SDK registration
    only, never network calls or awaits.
    """
    if card.mode is not OperationMode.SANDBOX or card.gateway_config is None:
        raise RuntimeError("conversation registration requires a SANDBOX gateway configuration")
    shared_card = card.model_copy(deep=True)
    shared_card.id = SHARED_OPERATION_ID
    shared_card.gateway_config.isolation.prefix = "conversation"

    with _registration_lock:
        previous = _registrations.get(resource_manager)
        if previous is not None:
            if previous.card.gateway_config != shared_card.gateway_config:
                # Do not expose URLs, credentials or complete configuration in errors.
                raise RuntimeError("conversation sandbox configuration changed; restart Runtime before using it")
            current = resource_manager.get_sys_operation(SHARED_OPERATION_ID, tag=SHARED_OPERATION_ID)
            if current is not previous.operation:
                raise RuntimeError("conversation sandbox registration ownership changed; restart Runtime")
            return current

        if resource_manager.get_sys_operation(SHARED_OPERATION_ID, tag=SHARED_OPERATION_ID) is not None:
            raise RuntimeError("conversation sandbox operation already exists without registration ownership")
        result = resource_manager.add_sys_operation(shared_card, tag=SHARED_OPERATION_ID)
        if not operation_succeeded(result):
            raise RuntimeError(
                "Failed to register conversation SANDBOX SysOperation: "
                f"{operation_error_detail(result)}"
            )
        operation = resource_manager.get_sys_operation(SHARED_OPERATION_ID, tag=SHARED_OPERATION_ID)
        if operation is None:
            raise RuntimeError("conversation SANDBOX SysOperation unavailable after registration")
        _registrations[resource_manager] = _Registration(shared_card, operation)
        return operation
