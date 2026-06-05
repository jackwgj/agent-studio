import os

from memory_service.common.constant import DeploymentMode

# 服务配置
SERVER_HOST = os.environ.get("MEMORY_SERVICE_HOST", "127.0.0.1")
SERVER_PORT = int(os.environ.get("MEMORY_SERVICE_PORT", 8010))

## SSL配置
SSL_ENABLE = os.environ.get("MEMORY_SSL_ENABLE", "True").lower() == "true"
CERT_BASE_PATH = os.environ.get("MEMORY_SSL_ROOT_PATH", "/opt/cloud/sec/https/")
SSL_CERT_FILE = os.path.join(CERT_BASE_PATH, "cert.pem")
SSL_KEY_FILE = os.path.join(CERT_BASE_PATH, "pri_key.pem")
SSL_PASSWORD = os.environ.get("SSL_KEY_PASSWORD", "")
REQ_TIME_OUT = int(os.environ.get("MEMORY_REQ_TIME_OUT", 600))

## 日志配置
LOG_LEVEL = os.environ.get("MEMORY_LOG_LEVEL", "INFO")
LOG_PATH = os.environ.get("MEMORY_LOG_PATH", "/opt/cloud/logs/memory-service/logs")

# 部署环境，hc/hcs/simple，临时默认值使用simple，当后续记忆功能上HC时再将默认值改成hc
ENV_TYPE = DeploymentMode.get_deployment_mode(os.environ.get("env_type", "simple"))

## 使用的KV库，支持redis
KV_STORE_TYPE = os.environ.get("MEMORY_KV_STORE_TYPE", "redis")

## Redis配置（注意环境变量和agent-base-runtime的redis环境变量保持一致）
## Redis的host+端口，单机模式下需要配置
REDIS_HOST = os.environ.get("REDIS_HOST", "127.0.0.1")
REDIS_PORT = int(os.environ.get("REDIS_PORT", 6379))
## Redis中使用的DB库，单机模式和哨兵模式需要配置
REDIS_DATABASE = int(os.environ.get("redis_database", "0"))
## Redis服务密码，三种模式都需要配置
REDIS_PASSWORD = os.environ.get("REDIS_PASSWORD", "")
## Redis连接池的最大连接数，三种模式都需要配置
REDIS_MAX_CONNECTIONS = int(os.environ.get("REDIS_MAX_CONNECTIONS", 30))
## Redis集群节点，集群模式下需要配置，使用英文逗号分开
REDIS_CLUSTER_NODES = os.environ.get("cluster_node_list", "")
## Redis哨兵节点，哨兵模式下需要配置，使用英文逗号分开（哨兵模式的配置跟agent-service等服务暂不保持一致，agent-service中的哨兵模式仅适配了HCS的RDS配置，未针对开源Redis的连接配置进行完全适配，此处暂时以开源Redis的连接配置进行处理）
REDIS_SENTINEL_NODES = os.environ.get("sentinel_nodes_list", "")
## Redis主节点名称，哨兵模式下需要配置
REDIS_MASTER_NAME = os.environ.get("redis_master_name", "mymaster")
## Redis哨兵密码，哨兵模式下需要配置（哨兵模式的配置跟agent-service等服务暂不保持一致，agent-service中的哨兵模式仅适配了HCS的RDS配置，未针对开源Redis的连接配置进行完全适配，此处暂时以开源Redis的连接配置进行处理）
REDIS_SENTINEL_PASSWORD = os.environ.get("sentinel_password", "")

## SCC配置
SCC_CONF_PATH = os.environ.get("MEMORY_SCC_CONF_PATH", "/opt/cloud/sec/conf/scc.conf")

## IAM配置
IAM_ENDPOINT = os.environ.get("IAM_ADDRESS", "")

## 无需IAM Token认证的接口白名单，支持通配符，使用英文逗号分开
IAM_AUTH_WHITE_LIST = os.environ.get("IAM_AUTH_WHITE_LIST", "")

## 记忆引擎初始化相关配置
## 记忆提取使用的模型的注册方式，default表示使用系统预置模型，custom-表示使用各个智能体中单独设置的模型
MEM_LLM_MODE = os.environ.get("MEMORY_LLM_MODE", "default")
## 记忆抽取使用的模型名称
MEM_LLM_MODEL_NAME = os.environ.get("MEMORY_LLM_MODEL_NAME")
## 记忆抽取使用的模型API地址
MEM_LLM_MODEL_API = os.environ.get("MEMORY_LLM_MODEL_API_ENDPOINT")
## 记忆抽取使用的模型API Key
MEM_LLM_MODEL_API_KEY = os.environ.get("MEMORY_LLM_MODEL_API_KEY")
## 记忆抽取使用的模型调用时需要传入的header信息，配置格式为json，如{"cust-header":"xxxxxx"}
MEM_LLM_MODEL_HEADER = os.environ.get("MEMORY_LLM_MODEL_HEADER")
## 模型调用的参数设置
MEM_LLM_MODEL_TEMPERATURE = float(os.environ.get("MEMORY_LLM_MODEL_TEMPERATURE", 0.7))
MEM_LLM_MODEL_TOP_P = float(os.environ.get("MEMORY_LLM_MODEL_TOP_P", 0.8))
MEM_LLM_MODEL_MAX_TOKENS = int(os.environ.get("MEMORY_LLM_MODEL_MAX_TOKENS", 2048))
MEM_LLM_MODEL_TIMEOUT = int(os.environ.get("MEMORY_LLM_MODEL_TIMEOUT", 60))

## 记忆引擎预置的用户画像定义文件路径
current_dir = os.path.dirname(os.path.abspath(__file__))
default_preset_user_profile_path = os.path.join(
    current_dir, "system_user_profiles.yaml"
)
PRESET_USER_PROFILE_FILE_PATH = os.environ.get(
    "PRESET_USER_PROFILE_FILE_PATH", default_preset_user_profile_path
)

## 记忆存储使用的向量化服务配置
## 记忆存储使用的向量库配置
## 向量库类型，none-不提供任何的向量库存储，OpenSearch-开源OpenSearch
VECTOR_STORE_TYPE = os.environ.get("VECTOR_STORE_TYPE", "none")
## 默认的向量库索引名称
VECTOR_STORE_DEFAULT_INDEX_NAME = os.environ.get(
    "VECTOR_STORE_DEFAULT_INDEX_NAME", "long_term_memory_default"
)
## OpenSearch连接配置
OPENSEARCH_HOST = os.environ.get("OPENSEARCH_HOST", "127.0.0.1")
OPENSEARCH_PORT = int(os.environ.get("OPENSEARCH_PORT", 9200))
## OpenSearch服务端是否启用了SSL
OPENSEARCH_SSL_ENABLE = os.environ.get("OPENSEARCH_USE_SSL", "true").lower() == "true"
## OpenSearch的用户名
OPENSEARCH_USER = os.environ.get("OPENSEARCH_USER", "")
## OpenSearch的密码，密码配置时需加密
OPENSEARCH_PASSWORD = os.environ.get("OPENSEARCH_PASSWORD", "")
## OpenSearch客户端与服务器之间的HTTP 请求/响应是否启用压缩，批量写入大量数据、查询结果数据量大时建议开启，单条小文档频繁操作、客户端/服务器 CPU 资源紧张时建议关闭
OPENSEARCH_HTTP_COMPRESS = (
    os.environ.get("OPENSEARCH_HTTP_COMPRESS", "false").lower() == "true"
)
OPENSEARCH_MAX_RETRIES = int(os.environ.get("OPENSEARCH_MAX_RETRIES", 3))
OPENSEARCH_RETRY_ON_TIMEOUT = (
    os.environ.get("OPENSEARCH_RETRY_ON_TIMEOUT", "false").lower() == "true"
)
## OpenSearch连接超时时间
OPENSEARCH_TIMEOUT = int(os.environ.get("MEMORY_OPENSEARCH_TIMEOUT", 30))

## 文本向量化配置
## 向量化服务类型，none-不提供任何的向量化服务，model-使用向量化模型
EMBEDDING_TYPE = os.environ.get("EMBEDDING_TYPE", "none")
## 向量化维度
EMBEDDING_DIMENSION = int(os.environ.get("EMBEDDING_DIMENSION", 1024))
## 使用向量化模型相关的配置
## 向量化模型服务地址
EMBEDDING_MODEL_ENDPOINT = os.environ.get("EMBEDDING_MODEL_ENDPOINT", "")
## 向量化模型API Key，配置时需要加密
EMBEDDING_MODEL_API_KEY = os.environ.get("EMBEDDING_MODEL_API_KEY", "")
## 调用向量化模型时需要传入的自定义header，配置格式为json，如{"cust-header":"xxxxxx"}
EMBEDDING_MODEL_HEADER = os.environ.get("EMBEDDING_MODEL_HEADER", "")
## 向量化模型名称
EMBEDDING_MODEL_NAME = os.environ.get("EMBEDDING_MODEL_NAME", "")
## 向量化模型服务连接超时时间
EMBEDDING_MODEL_TIMEOUT = int(os.environ.get("EMBEDDING_MODEL_TIMEOUT", 30))

## 向量索引配置
## 分片数量
INDEX_SHARD_COUNT = int(os.environ.get("INDEX_SHARD_COUNT", 3))
## 副本数量
INDEX_REPLICA_COUNT = int(os.environ.get("INDEX_REPLICA_COUNT", 1))

## 每个用户在每个应用中的最大记忆的数量（只计算需要进行向量存储的记忆数量），数量越大，查询性能越低
USER_MAX_MEMORY_COUNT = int(os.environ.get("USER_MAX_MEMORY_COUNT", 100))
