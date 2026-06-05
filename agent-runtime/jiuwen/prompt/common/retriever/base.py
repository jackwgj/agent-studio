#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""retriever base"""

from __future__ import annotations

import warnings
from abc import ABC
from inspect import signature
from typing import Any, Dict, List, Optional

from jiuwen.prompt.common.document import Document
from pydantic import BaseModel, Field


class BaseRetriever(BaseModel, ABC):
    """Abstract base class for a Document retrieval function.

    Args:
        top_k (int, optional): If set, the retriever will return at most top_k documents.
    """

    class Config:
        """Configuration for this pydantic object."""

        arbitrary_types_allowed = True

    new_arg_supported: bool = Field(default=False)
    expects_other_args: bool = Field(default=False)
    tags: Optional[List[str]] = Field(default=None)

    metadata: Optional[Dict[str, Any]] = Field(default=None)
    top_k: Optional[int] = Field(default=0)

    def __init_subclass__(cls, **kwargs: Any) -> None:
        super().__init_subclass__(**kwargs)
        # Version upgrade for old retrievers that implemented the public
        # methods directly.
        if cls.get_relevant_documents != BaseRetriever.get_relevant_documents:
            warnings.warn(
                "Retrievers must implement abstract `_get_relevant_documents` method"
                " instead of `get_relevant_documents`",
                DeprecationWarning,
            )
            swap = cls.get_relevant_documents
            cls.get_relevant_documents = (  # type: ignore[assignment]
                BaseRetriever.get_relevant_documents
            )
            cls._get_relevant_documents = swap  # type: ignore[assignment]
        if (
            hasattr(cls, "aget_relevant_documents")
            and cls.aget_relevant_documents != BaseRetriever.aget_relevant_documents
        ):
            warnings.warn(
                "Retrievers must implement abstract `_aget_relevant_documents` method"
                " instead of `aget_relevant_documents`",
                DeprecationWarning,
            )
            aswap = cls.aget_relevant_documents
            cls.aget_relevant_documents = (  # type: ignore[assignment]
                BaseRetriever.aget_relevant_documents
            )
            cls._aget_relevant_documents = aswap  # type: ignore[assignment]
        parameters = signature(cls._get_relevant_documents).parameters
        cls.new_arg_supported = parameters.get("run_manager") is not None
        # If a V1 retriever broke the interface and expects additional arguments
        cls.expects_other_args = (
            len(set(parameters.keys()) - {"self", "query", "run_manager"}) > 0
        )

    def get_relevant_documents(
        self,
        query: str,
        tags: Optional[List[str]] = None,
        metadata: Optional[Dict[str, Any]] = None,
        **kwargs: Any,
    ) -> List[Document]:
        """Retrieve documents relevant to a query.

        Args:
            query (str): string to find relevant documents for
            tags (List[str], optional): Optional list of tags associated with the retriever. Defaults to None
                These tags will be associated with each call to this retriever,
                and passed as arguments to the handlers defined in `callbacks`. Defaults: ``None``.
            metadata: Optional metadata associated with the retriever.
                This metadata will be associated with each call to this retriever,
                and passed as arguments to the handlers defined in `callbacks`. Defaults: ``None``.
        Returns:
            List[Document]: List of relevant documents
        """

        try:
            _kwargs = kwargs if self.expects_other_args else {}
            if self.new_arg_supported:
                result = self._get_relevant_documents(query)
            else:
                result = self._get_relevant_documents(query, **_kwargs)
        except Exception as error:
            raise ValueError("retriever handler value error") from error
        if self.top_k:
            result = result[: self.top_k]
        return result

    def _get_relevant_documents(self, query: str) -> List[Document]:
        """Abstract method that should be implemented by the subclasses.

        Args:
            query (str): String to find relevant documents for

        Returns:
            List[Document]: List of relevant documents
        """
        raise NotImplementedError()
