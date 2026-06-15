import logging
from typing import Any, Dict, List, Optional, Union

from openjiuwen.core.foundation.store.base_vector_store import (
    BaseVectorStore,
    CollectionSchema,
    FieldSchema,
    VectorDataType,
    VectorSearchResult,
)
from opensearchpy import AsyncOpenSearch
from opensearchpy.helpers import async_bulk

logger = logging.getLogger(__name__)

_TYPE_MAP: dict[VectorDataType, dict] = {
    VectorDataType.VARCHAR: {"type": "text"},
    VectorDataType.INT64: {"type": "long"},
    VectorDataType.INT32: {"type": "integer"},
    VectorDataType.INT16: {"type": "short"},
    VectorDataType.INT8: {"type": "byte"},
    VectorDataType.FLOAT: {"type": "float"},
    VectorDataType.DOUBLE: {"type": "double"},
    VectorDataType.BOOL: {"type": "boolean"},
    VectorDataType.JSON: {"type": "object", "enabled": False},
    VectorDataType.ARRAY: {"type": "object", "enabled": False},
}


def _field_to_mapping(field: FieldSchema) -> dict:
    if field.dtype == VectorDataType.FLOAT_VECTOR:
        return {
            "type": "knn_vector",
            "dimension": field.dim,
            "method": {
                "name": "hnsw",
                "space_type": "cosinesimil",
                "engine": "faiss",
            },
        }
    base = _TYPE_MAP.get(field.dtype, {"type": "text"}).copy()
    if field.dtype == VectorDataType.VARCHAR and field.is_primary:
        # primary key stored as keyword for exact match
        base = {"type": "keyword"}
    return base


def _build_mappings(schema: CollectionSchema) -> dict:
    properties: dict[str, Any] = {}
    for field in schema.fields:
        properties[field.name] = _field_to_mapping(field)
    return {"properties": properties}


class OpenSearchVectorStore(BaseVectorStore):
    """Agent-core BaseVectorStore backed by OpenSearch."""

    def __init__(
        self,
        hosts: list[str] | str,
        **kwargs: Any,
    ):
        self._client = AsyncOpenSearch(hosts=hosts, **kwargs)

    async def ping(self) -> bool:
        try:
            return await self._client.ping()
        except Exception:
            return False

    async def create_collection(
        self,
        collection_name: str,
        schema: Union[CollectionSchema, Dict[str, Any]],
        **kwargs: Any,
    ) -> None:
        if isinstance(schema, dict):
            schema = CollectionSchema.from_dict(schema)

        body: dict[str, Any] = {"mappings": _build_mappings(schema)}
        has_vector = any(f.dtype == VectorDataType.FLOAT_VECTOR for f in schema.fields)
        if has_vector:
            body["settings"] = {"index": {"knn": True}}

        try:
            await self._client.indices.create(index=collection_name, body=body)
        except Exception as e:
            err_str = str(e).lower()
            if "resource_already_exists_exception" in err_str or "already exists" in err_str:
                logger.debug("Index %s already exists, skipping creation", collection_name)
            else:
                raise

    async def delete_collection(self, collection_name: str, **kwargs: Any) -> None:
        try:
            await self._client.indices.delete(index=collection_name, ignore=[404])
        except Exception as e:
            if "index_not_found_exception" not in str(e).lower():
                raise

    async def collection_exists(self, collection_name: str, **kwargs: Any) -> bool:
        return bool(await self._client.indices.exists(index=collection_name))

    async def get_schema(self, collection_name: str, **kwargs: Any) -> CollectionSchema:
        mapping = await self._client.indices.get_mapping(index=collection_name)
        idx_data = mapping[collection_name]["mappings"]
        fields: list[FieldSchema] = []
        props = idx_data.get("properties", {})
        for name, cfg in props.items():
            typ = cfg.get("type", "text")
            if typ == "knn_vector":
                fields.append(FieldSchema(
                    name=name,
                    dtype=VectorDataType.FLOAT_VECTOR,
                    dim=cfg.get("dimension"),
                ))
            else:
                dtype = _os_type_to_vector_data_type(typ)
                fields.append(FieldSchema(name=name, dtype=dtype))
        return CollectionSchema(fields=fields)

    async def add_docs(
        self,
        collection_name: str,
        docs: List[Dict[str, Any]],
        **kwargs: Any,
    ) -> None:
        if not docs:
            return
        actions = []
        for doc in docs:
            action = {"_index": collection_name, "_source": dict(doc)}
            doc_id = doc.get("id")
            if doc_id is not None:
                action["_id"] = str(doc_id)
            actions.append(action)
        success, errors = await async_bulk(self._client, actions, raise_on_error=False)
        if errors:
            logger.warning(
                "add_docs to %s: %d errors out of %d",
                collection_name,
                len(errors) if isinstance(errors, list) else errors,
                len(docs),
            )

    async def search(
        self,
        collection_name: str,
        query_vector: List[float],
        vector_field: str,
        top_k: int = 5,
        filters: Optional[Dict[str, Any]] = None,
        **kwargs: Any,
    ) -> List[VectorSearchResult]:
        knn_query: dict[str, Any] = {
            "vector": query_vector,
            "k": top_k,
        }
        if filters:
            must = [{"term": {k: v}} for k, v in filters.items()]
            query = {
                "bool": {
                    "must": [{"knn": {vector_field: knn_query}}],
                    "filter": must,
                }
            }
        else:
            query = {"knn": {vector_field: knn_query}}

        body = {"size": top_k, "query": query}
        resp = await self._client.search(index=collection_name, body=body)

        results: list[VectorSearchResult] = []
        for hit in resp["hits"]["hits"]:
            source = hit["_source"]
            source["id"] = hit["_id"]
            results.append(VectorSearchResult(score=hit["_score"], fields=source))
        return results

    async def delete_docs_by_ids(
        self,
        collection_name: str,
        ids: List[str],
        **kwargs: Any,
    ) -> None:
        if not ids:
            return
        body = {
            "query": {
                "terms": {"_id": [str(i) for i in ids]}
            }
        }
        await self._client.delete_by_query(
            index=collection_name, body=body, ignore=[404]
        )

    async def delete_docs_by_filters(
        self,
        collection_name: str,
        filters: Dict[str, Any],
        **kwargs: Any,
    ) -> None:
        must = [{"term": {k: v}} for k, v in filters.items()]
        body = {"query": {"bool": {"filter": must}}}
        await self._client.delete_by_query(
            index=collection_name, body=body, ignore=[404]
        )

    async def list_collection_names(self) -> List[str]:
        indices = await self._client.cat.indices(format="json")
        return [
            idx["index"]
            for idx in indices
            if not idx["index"].startswith(".")
        ]

    async def update_schema(
        self, collection_name: str, operations: List[Any]
    ) -> None:
        raise NotImplementedError("update_schema not supported for OpenSearch adapter")

    async def update_collection_metadata(
        self, collection_name: str, metadata: Dict[str, Any]
    ) -> None:
        # OpenSearch doesn't have a separate metadata concept; store as _meta
        body = {"_meta": metadata}
        await self._client.indices.put_mapping(index=collection_name, body=body, ignore=[404])

    async def get_collection_metadata(self, collection_name: str) -> Dict[str, Any]:
        mapping = await self._client.indices.get_mapping(index=collection_name)
        idx_data = mapping[collection_name]["mappings"]
        return idx_data.get("_meta", {})


def _os_type_to_vector_data_type(os_type: str) -> VectorDataType:
    mapping = {
        "text": VectorDataType.VARCHAR,
        "keyword": VectorDataType.VARCHAR,
        "long": VectorDataType.INT64,
        "integer": VectorDataType.INT32,
        "short": VectorDataType.INT16,
        "byte": VectorDataType.INT8,
        "float": VectorDataType.FLOAT,
        "double": VectorDataType.DOUBLE,
        "boolean": VectorDataType.BOOL,
        "object": VectorDataType.JSON,
    }
    return mapping.get(os_type, VectorDataType.VARCHAR)
