import json
import logging
from typing import List, Dict, Optional

import yaml
from jiuwen.context.memory_engine.config.config import (
    MemoryEngineConfig,
    ProfileTopicTag,
    ProfileTopicConfig,
)

# 创建logger实例
logger = logging.getLogger(__name__)
from jiuwen.context.memory_engine.engine.memory_engine import MemoryEngine
from memory_service.memory.embedding.base_embedding import Embedding
from memory_service.memory.vector_store.base_vector_store import BaseVectorStore
from memory_service.memory.kv_store_redis_impl import KVStoreRedisImpl
from memory_service.memory.llm.agentBuilder_memory_llm import AgentBuilderMemoryLLM
from memory_service.memory.embedding.embedding_model import (
    initialize_embedding,
    get_embedding_instance,
)
from memory_service.memory.vector_store.opensearch_vector_store import (
    OpenSearchVectorStore,
)
from memory_service.memory.long_term_memory_index import LongTermMemoryIndex
from memory_service.config.settings import (
    MEM_LLM_MODE,
    MEM_LLM_MODEL_NAME,
    MEM_LLM_MODEL_API,
    MEM_LLM_MODEL_API_KEY,
    MEM_LLM_MODEL_HEADER,
    MEM_LLM_MODEL_TEMPERATURE,
    MEM_LLM_MODEL_TOP_P,
    MEM_LLM_MODEL_MAX_TOKENS,
    MEM_LLM_MODEL_TIMEOUT,
    PRESET_USER_PROFILE_FILE_PATH,
    EMBEDDING_TYPE,
    EMBEDDING_DIMENSION,
    EMBEDDING_MODEL_ENDPOINT,
    EMBEDDING_MODEL_API_KEY,
    EMBEDDING_MODEL_HEADER,
    EMBEDDING_MODEL_NAME,
    EMBEDDING_MODEL_TIMEOUT,
    VECTOR_STORE_TYPE,
)


class MemoryEngineUtil:
    _instance = None
    _initialized = False
    engine_instance: MemoryEngine = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        # 防止多次初始化
        if not self._initialized:
            logger.info("Start init memory engine.")
            kv_store = KVStoreRedisImpl()
            self.engine_instance = MemoryEngine()
            self.engine_instance.register_store(kv_store)
            from memory_service.scc.crypto_utils import get_crypto_util

            # 如果使用了系统预置的记忆抽取模型，需要使用系统配置项中的模型信息
            if MEM_LLM_MODE == "default":
                logger.info("Init default llm model for memory engine.")
                required_configs = {
                    "MEM_LLM_MODEL_NAME": MEM_LLM_MODEL_NAME,
                    "MEM_LLM_MODEL_API": MEM_LLM_MODEL_API,
                }

                missing_configs = [
                    name
                    for name, value in required_configs.items()
                    if not value or str(value).strip() == ""
                ]

                if missing_configs:
                    missing_str = ", ".join(missing_configs)
                    logger.error(
                        f"Missing required configurations when MEM_LLM_MODE is default: {missing_str}"
                    )
                    raise RuntimeError(
                        f"Missing required configurations when MEM_LLM_MODE is default: {missing_str}"
                    )
                decrypt_api_key = (
                    get_crypto_util().decrypt(MEM_LLM_MODEL_API_KEY)
                    if MEM_LLM_MODEL_API_KEY
                    else ""
                )
                custom_header = (
                    json.loads(MEM_LLM_MODEL_HEADER) if MEM_LLM_MODEL_HEADER else {}
                )
                self.engine_instance.init_base_llm(
                    AgentBuilderMemoryLLM(
                        model_name=MEM_LLM_MODEL_NAME,
                        api_key=decrypt_api_key,
                        api_base=MEM_LLM_MODEL_API,
                        temperature=float(MEM_LLM_MODEL_TEMPERATURE),
                        top_p=float(MEM_LLM_MODEL_TOP_P),
                        max_tokens=int(MEM_LLM_MODEL_MAX_TOKENS),
                        timeout=int(MEM_LLM_MODEL_TIMEOUT),
                        custom_header=custom_header,
                    )
                )
                logger.info("Init llm model for memory engine finished.")

            # 初始化系统默认的topic
            engine_config = MemoryEngineConfig()
            if PRESET_USER_PROFILE_FILE_PATH:
                logger.info("Init preset user profile for memory engine.")
                # 加载预置用户画像
                preset_user_profiles = load_preset_user_profiles(
                    PRESET_USER_PROFILE_FILE_PATH
                )
                forbidden_tags_config = []
                preset_topics_config = []
                for preset_user_profile in preset_user_profiles:
                    _preset_user_profile_topic.add(preset_user_profile.topic)
                    if "_forbidden_" == preset_user_profile.topic:
                        for forbidden_tag in preset_user_profile.tags:
                            tag = ProfileTopicTag(
                                name=forbidden_tag["name"],
                                description=forbidden_tag["description"],
                            )
                            forbidden_tags_config.append(tag)
                    else:
                        preset_tags = []
                        for tags in preset_user_profile.tags:
                            tag = ProfileTopicTag(
                                name=tags["name"], description=tags["description"]
                            )
                            preset_tags.append(tag)
                        preset_topic = ProfileTopicConfig(
                            topic=preset_user_profile.topic, tags=preset_tags
                        )
                        preset_topics_config.append(preset_topic)
                engine_config.user_profile_forbidden_topic = forbidden_tags_config
                engine_config.default_profile_topics = preset_topics_config

            self.engine_instance.set_config(engine_config)

            # 初始化系统预置的文本向量化服务
            embedding_service: Optional[Embedding] = None
            logger.info(f"Init embedding service with config {EMBEDDING_TYPE}.")
            if EMBEDDING_TYPE == "model":
                api_key = (
                    get_crypto_util().decrypt(EMBEDDING_MODEL_API_KEY)
                    if EMBEDDING_MODEL_API_KEY
                    else ""
                )
                custom_header = (
                    json.loads(EMBEDDING_MODEL_HEADER) if EMBEDDING_MODEL_HEADER else {}
                )
                initialize_embedding(
                    EMBEDDING_MODEL_ENDPOINT,
                    EMBEDDING_MODEL_NAME,
                    EMBEDDING_DIMENSION,
                    api_key,
                    EMBEDDING_MODEL_TIMEOUT,
                    custom_header,
                )
                embedding_service = get_embedding_instance()

            vector_store: Optional[BaseVectorStore] = None
            logger.info(f"Init vector store service with config {VECTOR_STORE_TYPE}.")
            # 初始化向量库连接
            if VECTOR_STORE_TYPE.lower() == "opensearch":
                vector_store = OpenSearchVectorStore()

            if embedding_service and vector_store:
                logger.info(
                    "Register embedding service and vector store for natural language memory extract service."
                )
                self.engine_instance.register_plugin(
                    "semantic_index",
                    LongTermMemoryIndex,
                    {
                        "vector_store": vector_store,
                        "embedding_model": embedding_service,
                    },
                )
            else:
                logger.warning(
                    "Embedding service or vector_store is none, can not init natural language memory extract service."
                )
                self.engine_instance.register_plugin(
                    "semantic_index",
                    LongTermMemoryIndex,
                    {"vector_store": None, "embedding_model": None},
                )

            logger.info("Init memory engine success.")
            self._initialized = True


def get_mem_engine() -> MemoryEngine:
    global _engine
    if _engine is None:
        _engine = MemoryEngineUtil()
    return _engine.engine_instance


_engine = None


def get_preset_user_profile_topic() -> set:
    return _preset_user_profile_topic


_preset_user_profile_topic = set()


# 预置用户画像数据结构
class SystemUserProfile:
    def __init__(self, topic: str, tags: List[Dict]):
        self.topic = topic
        self.tags = tags


def load_preset_user_profiles(file_path: str) -> List[SystemUserProfile]:
    """
    从指定路径加载预置用户画像配置文件
    """
    global _preset_user_profile_topic
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            data = yaml.safe_load(file)
            profiles_data = data.get("system_user_profiles", [])

            preset_user_profiles = []
            for profile_data in profiles_data:
                topic = profile_data["topic"]
                profile = SystemUserProfile(
                    topic=topic, tags=profile_data.get("tags", [])
                )
                preset_user_profiles.append(profile)
                _preset_user_profile_topic.add(topic)

            logger.info(
                f"Successfully loaded {len(preset_user_profiles)} preset user profiles from {file_path}"
            )
            return preset_user_profiles

    except Exception as e:
        logger.error(
            f"Failed to load preset user profiles from {file_path}: %s",
            e,
            exc_info=True,
        )
        return []
