from enum import Enum


class Locale(Enum):
    ZH_CN = "zh-CN"
    EN_US = "en-US"


def normalize_locale(locale: str) -> Locale:
    if not locale:
        return Locale.ZH_CN
    if locale.lower().startswith("en"):
        return Locale.EN_US
    elif locale.lower().startswith("zh"):
        return Locale.ZH_CN
    else:
        return Locale.ZH_CN


class I18nMessageCode:
    SYSTEM_ERROR_MESSAGE = "system_error_message"
    SYSTEM_ERROR_REASON = "system_error_reason"
    SYSTEM_ERROR_SUGGESTION = "system_error_suggestion"
    REDIS_NOT_INIT_MESSAGE = "redis_not_init_message"
    REDIS_NOT_INIT_REASON = "redis_not_init_reason"
    REDIS_NOT_INIT_SUGGESTION = "redis_not_init_suggestion"
    MISSING_TOKEN_MESSAGE = "missing_token_message"
    MISSING_TOKEN_REASON = "missing_token_reason"
    MISSING_TOKEN_SUGGESTION = "missing_token_suggestion"
    TOKEN_VALIDATE_ERROR_MESSAGE = "token_validate_error_message"
    TOKEN_VALIDATE_ERROR_REASON = "token_validate_error_reason"
    TOKEN_VALIDATE_ERROR_SUGGESTION = "token_validate_error_suggestion"

    # 向量化模块消息码
    EMBEDDING_REQUEST_FAILED_MESSAGE = "embedding_request_failed_message"
    EMBEDDING_REQUEST_FAILED_REASON = "embedding_request_failed_reason"
    EMBEDDING_REQUEST_FAILED_SUGGESTION = "embedding_request_failed_suggestion"
    EMBEDDING_RESPONSE_PARSE_FAILED_MESSAGE = "embedding_response_parse_failed_message"
    EMBEDDING_RESPONSE_PARSE_FAILED_REASON = "embedding_response_parse_failed_reason"
    EMBEDDING_RESPONSE_PARSE_FAILED_SUGGESTION = (
        "embedding_response_parse_failed_suggestion"
    )
    EMBEDDING_MODEL_RETURN_ERROR_DATA_MESSAGE = (
        "embedding_model_return_error_data_message"
    )
    EMBEDDING_MODEL_RETURN_ERROR_DATA_REASON = (
        "embedding_model_return_error_data_reason"
    )
    EMBEDDING_MODEL_RETURN_ERROR_DATA_SUGGESTION = (
        "embedding_model_return_error_data_suggestion"
    )
    EMBEDDING_DIMENSION_MISMATCH_MESSAGE = "embedding_dimension_mismatch_message"
    EMBEDDING_DIMENSION_MISMATCH_REASON = "embedding_dimension_mismatch_reason"
    EMBEDDING_DIMENSION_MISMATCH_SUGGESTION = "embedding_dimension_mismatch_suggestion"

    # 向量存储模块消息码
    VECTOR_STORE_ADD_DOCUMENT_FAILED_MESSAGE = (
        "vector_store_add_document_failed_message"
    )
    VECTOR_STORE_ADD_DOCUMENT_FAILED_REASON = "vector_store_add_document_failed_reason"
    VECTOR_STORE_ADD_DOCUMENT_FAILED_SUGGESTION = (
        "vector_store_add_document_failed_suggestion"
    )
    VECTOR_STORE_SEARCH_FAILED_MESSAGE = "vector_store_search_failed_message"
    VECTOR_STORE_SEARCH_FAILED_REASON = "vector_store_search_failed_reason"
    VECTOR_STORE_SEARCH_FAILED_SUGGESTION = "vector_store_search_failed_suggestion"
    VECTOR_STORE_DELETE_DOCUMENTS_FAILED_MESSAGE = (
        "vector_store_delete_documents_failed_message"
    )
    VECTOR_STORE_DELETE_DOCUMENTS_FAILED_REASON = (
        "vector_store_delete_documents_failed_reason"
    )
    VECTOR_STORE_DELETE_DOCUMENTS_FAILED_SUGGESTION = (
        "vector_store_delete_documents_failed_suggestion"
    )

    # 长期记忆索引模块消息码
    LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED_MESSAGE = (
        "long_term_memory_save_memories_failed_message"
    )
    LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED_REASON = (
        "long_term_memory_save_memories_failed_reason"
    )
    LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED_SUGGESTION = (
        "long_term_memory_save_memories_failed_suggestion"
    )
    LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_MESSAGE = (
        "long_term_memory_delete_memories_failed_message"
    )
    LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_REASON = (
        "long_term_memory_delete_memories_failed_reason"
    )
    LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_SUGGESTION = (
        "long_term_memory_delete_memories_failed_suggestion"
    )
    LONG_TERM_MEMORY_SEARCH_FAILED_MESSAGE = "long_term_memory_search_failed_message"
    LONG_TERM_MEMORY_SEARCH_FAILED_REASON = "long_term_memory_search_failed_reason"
    LONG_TERM_MEMORY_SEARCH_FAILED_SUGGESTION = (
        "long_term_memory_search_failed_suggestion"
    )
    LONG_TERM_MEMORY_GET_BY_ID_FAILED_MESSAGE = (
        "long_term_memory_get_by_id_failed_message"
    )
    LONG_TERM_MEMORY_GET_BY_ID_FAILED_REASON = (
        "long_term_memory_get_by_id_failed_reason"
    )
    LONG_TERM_MEMORY_GET_BY_ID_FAILED_SUGGESTION = (
        "long_term_memory_get_by_id_failed_suggestion"
    )

    EXTRACT_MEMORY_FAILED_MESSAGE = "extract_memory_failed_message"
    EXTRACT_MEMORY_FAILED_REASON = "extract_memory_failed_reason"
    EXTRACT_MEMORY_FAILED_SUGGESTION = "extract_memory_failed_suggestion"
