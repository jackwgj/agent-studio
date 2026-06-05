#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""conversation and plan message definition"""

from .base_vector import BaseVector
from .dumb_vector import DumpVector


class VectorDbFactory:
    __instance: BaseVector = None

    @classmethod
    def register_vector(cls, vector):
        VectorDbFactory.__instance = vector

    @staticmethod
    def get_storage():
        return VectorDbFactory.__instance if VectorDbFactory.__instance else DumpVector
