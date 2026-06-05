from jiuwen.orchestration.callbacks.manager import CallbackHandlerManager

from .global_vals_callback_handler import GlobalVals4CallbackHandler


def init_long_memory():
    CallbackHandlerManager().add_handler(GlobalVals4CallbackHandler())
