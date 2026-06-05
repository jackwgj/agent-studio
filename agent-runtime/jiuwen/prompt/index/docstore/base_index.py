#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
Interface for template index
"""

from abc import ABC, abstractmethod
from typing import List

from jiuwen.prompt.common.document import Document


class BaseIndex(ABC):
    """Interface for template index"""

    @abstractmethod
    def add_document(self, record: Document) -> bool:
        """add document to database"""
        pass

    @abstractmethod
    def get_documents(self, **kwargs) -> List[Document]:
        """get documents from database filter by kwargs"""
        pass

    @abstractmethod
    def update_document(self, record: Document, **kwargs) -> bool:
        """update document to database"""
        pass

    @abstractmethod
    def delete_document(self, **kwargs) -> bool:
        """remove document with kwargs"""
        pass
