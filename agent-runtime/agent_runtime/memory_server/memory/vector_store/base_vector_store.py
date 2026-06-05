from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Any, Dict, List, Optional


@dataclass
class SearchDocsItem:
    """
    文档列表查询结果

    Attributes:
        id: 向量库内自动生成的ID
        score: 相似度分数
        data: 从索引中查出来的实际数据
    """

    id: str
    score: float
    data: Dict[str, Any]


@dataclass
class ListDocsResult:
    """
    文档列表查询结果

    Attributes:
        total: 符合条件的数据总数
        data: 实际返回的数据列表
    """

    total: int
    data: List[SearchDocsItem]


class BaseVectorStore(ABC):
    @abstractmethod
    async def create_collection(self, collection_name: str) -> None:
        """
        创建一个新的集合（索引）

        Args:
            collection_name: 集合名称

        Raises:
            Exception: 如果集合创建失败
        """
        pass

    @abstractmethod
    async def delete_collection(
        self,
        collection_name: str,
        **kwargs: Any,
    ) -> None:
        """
        删除一个集合（索引）

        Args:
            collection_name: 集合名称
            **kwargs: 额外的删除参数

        Raises:
            Exception: 如果集合删除失败
        """
        pass

    @abstractmethod
    async def collection_exists(self, collection_name: str) -> bool:
        """
        检查集合是否存在

        Args:
            collection_name: 集合名称

        Returns:
            bool: 如果集合存在返回 True，否则返回 False
        """
        pass

    @abstractmethod
    async def add_docs(self, collection_name: str, docs: List[Dict[str, Any]]) -> None:
        """
        向集合中添加文档

        Args:
            collection_name: 集合名称
            docs: 文档列表，每个文档是一个字典

        Raises:
            Exception: 如果文档插入失败
        """
        pass

    @abstractmethod
    async def delete_docs_by_ids(
        self,
        collection_name: str,
        ids: List[str],
    ) -> None:
        """
        根据 ID 删除文档

        Args:
            collection_name: 集合名称
            ids: 文档 ID 列表

        Raises:
            Exception: 如果删除失败
        """
        pass

    @abstractmethod
    async def delete_docs_by_filters(
        self,
        collection_name: str,
        filters: Dict[str, Any],
    ) -> None:
        """
        从索引中根据指定条件删除索引

        Args:
            collection_name: 集合名称
            filters: 过滤条件

        Raises:
            Exception: 如果删除失败
        """
        pass

    @abstractmethod
    async def search(
        self,
        collection_name: str,
        query_vector: List[float],
        vector_field: str,
        top_k: int = 5,
        filters: Optional[Dict[str, Any]] = None,
    ) -> List[SearchDocsItem]:
        """
        从索引中检索数据

        Args:
            collection_name: 集合名称
            query_vector: 查询向量
            vector_field: 向量字段名
            top_k: 返回的前 k 个结果
            filters: 过滤条件

        Raises:
            Exception: 如果搜索失败
        """
        pass

    @abstractmethod
    async def list_docs(
        self,
        collection_name: str,
        offset: int = 0,
        limit: int = 10,
        order_field: Optional[str] = None,
        order_type: str = "asc",
        filters: Optional[Dict[str, Any]] = None,
        ids: Optional[List[str]] = None,
    ) -> ListDocsResult:
        """
        从索引中检索数据

        Args:
            collection_name: 集合名称
            offset: 偏移量，默认为0
            limit: 返回数量限制，默认为10
            order_field: 排序字段，默认为None
            order_type: 排序类型（asc/desc），默认为"asc"
            filters: 过滤条件
            ids: 文档ID列表，如果提供则根据这些ID精确查询。如果同时提供ids和filters参数，优先使用ids查询。

        Returns:
            ListDocsResult: 包含总数、数据列表的集合体

        Raises:
            Exception: 如果检索失败
        """
        pass
