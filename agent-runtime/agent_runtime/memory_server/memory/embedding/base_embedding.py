from abc import ABC, abstractmethod
from typing import List


class Embedding(ABC):
    @abstractmethod
    async def embed_query(self, text: str) -> List[float]:
        """向量化文本"""
        pass

    @abstractmethod
    async def embed_documents(self, texts: List[str]) -> List[List[float]]:
        """批量向量化文本"""
        pass
