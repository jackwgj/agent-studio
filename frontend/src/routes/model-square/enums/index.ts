// model evaluation list
export enum ModelEvaluationListColumns {
  TASK_NAME = 'name',
  TASK_STATUS = 'status',
  TASK_PROGRESS = 'progress',
  MODEL_TYPE = 'model_type',
  CREATE_TIME = 'create_time',
  OPERATION_FIELD = 'operation',
}

export enum ModelEvaluationModelType {
  TEXT_CREATE = 'Text Generation',
}

export enum ModelEvaluationType {
  AUTO_EVALUATION = 'auto',
}

// 创建评测任务规则
export enum CreateEvaluationRuleType {
  RULE = 'RULE', // 基于规则
}

// 创建评测任务规则标题
export const CreateEvaluationRuleTitle = {
  [CreateEvaluationRuleType.RULE]: 'maas_model_create_evaluation_setting_evaluation_rule_base_title',
};

// 创建评测任务数据集类型
export enum CreateEvaluationDatasetType {
  MMLU = 'MMLU', // AI大模型评测基准
  CEVAL = 'CEVAL', // 中文理解能力
  BOOLQ = 'BOOLQ', // 判断能力
  AGI = 'AGI', // 客观题数据集
  GSM8K = 'GSM8K', // 小学数学推理集
  ARC_CHALLENGE = 'ARC_CHALLENGE', // 逻辑推理评测集
  BBH = 'BBH', // 困难任务表现评测集
  LONGBENCH = 'LONGBENCH', // 长文本评测基准
  CMMLU = 'CMMLU', // MMLU的中文版本
  CFBENCHMARK = 'CFBENCHMARK', // 中文金融数据集
  FINANCEIQ = 'FINANCEIQ', // 金融信息处理评测集
}

// 创建评测任务数据集图片ID
export enum CreateEvaluationPictureId {
  GENERAL_KNOWLEDGE = 'general_knowledge', // 通用知识与技能
  MATHEMATICAL_ABILITY = 'mathematical_ability', // 数学能力
  LOGICAL_INFERENCE = 'logical_inference', // 逻辑推理
  LONG_TEXT = 'long_text', // 长文本
  CHINESE_LANGUAGE_ABILITY = 'chinese_language_ability', // 中文能力
  FINANCIAL_FIELD = 'financial_field', // 金融领域
}

// 操作单个评测任务的操作类型
export enum OperateEvaluationJobActionType {
  TERMINATE = 'terminate', // 停止
  RESTART = 'restart', // 重启
}

// 创建评测任务传参中datasets单个数据类型-子数据集来源
export enum EvaluationDatasetSource {
  PRESET = 'PRESET',
}

// 模型评测结果总览
export enum ModelEvaluationResultOverviewListColumns {
  SERVICE_NAME = 'service_name',
  SERVICE_TYPE = 'service_type',
  MODEL_NAME = 'model_name',
  SERVICE_SCORE = 'service_score',
}

// 模型评测结果分析
export enum ModelEvaluationResultAnalysisListColumns {
  SERVICE_NAME = 'service_name',
  SERVICE_TYPE = 'service_type',
  CASE_ID = 'case_id',
  DATASET_TYPE = 'dataset_type',
  DATASET_EN_NAME = 'dataset_en_name',
  CONTEXT = 'context',
  TARGET = 'target',
  PREDICTION = 'prediction',
  PROCESSED_PREDICTION = 'processed_prediction',
}

// 评测结果导出记录列表
export enum ModelEvaluationResultDownloadListColumns {
  EXPORT_RECORD_ID = 'export_record_id',
  STATUS = 'status',
  CREATE_TIME = 'create_time',
  UPDATE_TIME = 'update_time',
  OPERATION_FIELD = 'operation',
}

// 评测结果导出状态
export enum DownloadStatusEnum {
  RUNNING = 'RUNNING',
  FAILED = 'FAILED',
  COMPLETED = 'COMPLETED',
}

// 评测报告预览服务来源
export enum EvaluationDetailServiceSource {
  PRESET = 'PRESET',
  CUSTOM = 'CUSTOM',
  API = 'API',
}

// 评测任务详情tab的ID
export enum EvaluationDetailTabId {
  EVALUATION_DETAIL = 'evaluationDetail',
  EVALUATION_RESULT = 'evaluationResult',
  EVALUATION_LOG = 'evaluationLog',
}

// 创建评测任务选择数据集后显示的列表的column的Key
export enum CreateEvaluationSelectedDatasetCol {
  DATASET_ID = 'dataset_id', // 数据集id
  DATASET_NAME = 'dataset_name', // 数据集中文名称
  DATASET_EN_NAME = 'dataset_en_name', // 数据集英文名称
  DATASET_SOURCE = 'dataset_source',
  RADIO = 'dataset_ratio', // 配比（%）
  USE_CASE_COUNT = 'dataset_usecase_count', // 使用数据量
  TOTAL_CASE_COUNT = 'dataset_totalcase_count', // 总数据量
  FIRST_PARENT_ID = 'first_parent_id',
  FIRST_PARENT_NAME = 'first_parent_name',
  FIRST_PARENT_EN_NAME = 'first_parent_en_name',
  SECOND_PARENT_ID = 'second_parent_id', // 预置数据集
  SECOND_PARENT_NAME = 'second_parent_name', // 预置数据集
  SECOND_PARENT_EN_NAME = 'second_parent_en_name', // 预置数据集
  USE_AND_TOTAL_CASE_COUNT = 'use_and_total_case_count',
  OPERATION_FIELD = 'operation', // 操作
}

// 创建评测任务选择服务后显示的列表的column的Key
export enum CreateEvaluationSelectedServicesCol {
  SERVICE_NAME = 'service_name', // 服务名称
  SERVICE_ID = 'service_id',
  MODEL_NAME = 'model_name', // 模型名称
  SERVICE_TYPE = 'service_type', // 服务来源
  TEMPERATURE = 'temperature',
  TOP_P = 'top_p',
  TOP_K = 'top_k',
  OPERATION_FIELD = 'operation', // 操作
}

// model evaluation detail dataset
export enum ModelEvaluationDatasetColumns {
  DATASET_TYPE = 'second_parent_name',
  DATASET_NAME = 'dataset_name',
  DATASET_RADIO = 'dataset_ratio',
  DATASET_COUNT = 'dataset_usecase_count',
}

// model evaluation detail select service
export enum ModelEvaluationServiceColumns {
  SERVICE_NAME = 'service_name',
  SERVICE_TYPE = 'service_type',
  MODEL_NAME = 'model_name',
  TEMPERATURE = 'temperature',
  TOP_P = 'top_p',
  TOP_K = 'top_k',
}

// model deployment resident list
export enum ModelDeploymentResidentColumns {
  SERVICE_NAME = 'model_name',
  SERVED_MODEL_NAME = 'served_model_name',
  FREE_QUOTA = 'free_quota',
  QUOTA_EXPIRE_TIME = 'quota_expire_time',
  PAY_STATUS = 'enable_premium',
  DISPLAY_STATUS = 'displayStatus',
  PAY_MODEL = 'pay_model',
  MODEL_TYPE_KEY = 'model_type_key',
  INPUT_PRICING = 'inputAmount',
  OUTPUT_PRICING = 'outputAmount',
  PROMOTION_DISCOUNT = 'promotionDiscount',
  USAGE_MONITOR = 'usage_monitor',
  CALLED_STATISTIC = 'calledStatistic',
  OPERATION_FIELD = 'operation', // 操作
  MODEL_LIMIT = 'modelLimit',
  DESCRIPTION = 'description',
  PARENT_SERVICE_NAME = 'parent_model_name',
  RESOURCE_ID = 'resource_id',
}

export enum ResidentDisplayStatus {
  FROZEN = 'frozen',
  OPEN = 'open',
  CLOSE = 'close',
  INVALID = 'invalid',
}

export enum CustomiseStatus {
  BYTOKEN = 'byToken',
  SUSPENDED = 'suspended',
  FROZEN = 'frozen',
  NORMAL = 'normal',
  DEPLOYING = 'deploying',
  FAILED = 'failed',
  STOPPED = 'stopped',
  RUNNING = 'running',
  DELETING = 'deleting',
  STOPPING = 'stopping',
  CONCERNING = 'concerning',
  DELETED = 'deleted',
  RESTARTING = 'restarting',
  UPGRADING = 'upgrading',
  ERROR = 'error',
  INTERRUPTING = 'interrupting',
  OFFLINE = 'offline',
  INVALID = 'invalid',
}

export enum CustomEndpointAction {
  ACTIVE = 'activate',
  DEACTIVE = 'deactivate',
}

export const FROZEN_PREFIX = 'frozen';

// 模型体验的用户类型
export enum USER_TYPE {
  AGENT_ROLE = 'assistant',
  USER_ROLE = 'user',
}

// 模型体验多模态类型
export enum CHAT_MULTI_TYPE {
  TEXT = 'text',
  IMAGE_URL = 'image_url',
}

// 模型体验的对话状态
export enum ChatStatusListEnum {
  INIT = 'init',
  RUNNING = 'run',
  WAITING = 'wait',
  DONE = 'done',
  ABNORMAL = 'Abnormal',
  STOP = 'stop',
}

export enum CfCurrentLanType {
  EN_US = 'en-us',
  ZH_CN = 'zh-cn',
}

export enum ModelExperienceFinishReasonStatus {
  STOP = 'stop', // 对话停止
  CONTENT_FILTER = 'content_filter', // 触发风控
  LENGTH = 'length', // 超长截断,因为 max_tokens 参数或 token 限制，导致不完整的模型输出
  NULL = 'null', // API 响应还在进行中或未完成
}

export enum ModelExperienceChatType {
  TEXT = 'text',
  IMAGE_AND_TEXT = 'imageText',
}

export enum MaxModelLenType {
  K128 = '128K',
  K64PLUS = '>=64K',
  K64 = '64K',
  K32 = '32K',
  K16 = '16K',
  K8 = '8K',
  K8MINUS = '<=8K',
}

export enum MaxModelLenTypeMap {
  K128 = '128k',
  K64PLUS = '>=64K',
  K64 = '64k',
  K32 = '32k',
  K16 = '16k',
  K8 = '8k',
  K8MINUS = '<=8k',
}

export enum StatisticsFormColumns {
  SERVICE_NAME_VERSION = 'service_name_version', // 服务名称-版本，用来展示在列里
  SERVICE_NAME = 'service_name', // 服务名称
  VERSION_NAME = 'version_name', // 版本
  CALL_COUNT = 'request_count', // 调用次数
  CALL_FAIL_COUNT = 'error_count', // 调用失败次数
  CALL_FAIL_RATE = 'error_rate', // 调用失败率（%）
  CALL_TOTAL_TOKENS = 'total_token', // 调用总Tokens数
  INPUT_TOKENS = 'prompt_token', // 输入Tokens数
  OUTPUT_TOKENS = 'completion_token', // 输出Tokens数
  AVERAGED_RESPONSE_DELAY = 'avg_latency', // 端到端时延（ms）
  AVERAGED_FIRST_TOKEN_DELAY = 'avg_ttft', // 平均首Token时延（秒）
  AVERAGED_INCREMENT_TOKEN_DELAY = 'avg_tpot', // 平均增量Tokens时延（秒）
  AVERAGE_GENERATION_TIME = 'avg_generation_time', // 平均生成时长（秒）
  CACHE_TOKEN = 'cache_token', // 缓存命中数
  CACHE_HIT_RATIO = 'cache_hit_ratio', // 缓存命中数
  OPERATION_FIELD = 'operation', // 操作
  TPM = 'tpm', // TPM
  RPM = 'rpm', // RPM
  SCC_COUNT = 'scc_count', // 推理成功（次）
  AVG_CONSUME_TIME = 'avg_consume_time', // 平均任务处理时长(分钟)
  COMPLETION_TIMES = 'completion_times', // 推理成功(次)
  INFER_TIMES = 'infer_times', // 推理次数(次)
  BATCH_ERROR_RATE = 'batch_error_rate', // 推理失败率(%)
  COMPLETION_TASKS_COUNT = 'completion_tasks_count', // 已完成任务数量（个）
  QPS = 'qps',
  SUCC_COUNT = 'succ_count',
  P99_LATENCY = 'p99_latency',
  P90_LATENCY = 'p90_latency',
  P80_LATENCY = 'p80_latency',
  P50_LATENCY = 'p50_latency',
  MAX_LATENCY = 'max_latency',
  MAX_TTFT = 'max_ttft',
  P50_TTFT = 'p50_ttft',
  P80_TTFT = 'p80_ttft',
  P90_TTFT = 'p90_ttft',
  P99_TTFT = 'p99_ttft',
  MAX_TPOT = 'max_tpot',
  P50_TPOT = 'p50_tpot',
  P80_TPOT = 'p80_tpot',
  P90_TPOT = 'p90_tpot',
  P99_TPOT = 'p99_tpot',
  AVG_PROMPT_TOKEN = 'avg_prompt_token',
  MAX_PROMPT_TOKEN = 'max_prompt_token',
  P50_PROMPT_TOKEN = 'p50_prompt_token',
  P80_PROMPT_TOKEN = 'p80_prompt_token',
  P90_PROMPT_TOKEN = 'p90_prompt_token',
  P99_PROMPT_TOKEN = 'p99_prompt_token',
  AVG_COMPLETION_TOKEN = 'avg_completion_token',
  MAX_COMPLETION_TOKEN = 'max_completion_token',
  P50_COMPLETION_TOKEN = 'p50_completion_token',
  P80_COMPLETION_TOKEN = 'p80_completion_token',
  P90_COMPLETION_TOKEN = 'p90_completion_token',
  P99_COMPLETION_TOKEN = 'p99_completion_token',
}

export enum StatisticsDataType {
  CALL_ALL_COUNT = 'total_request_count', // 总调用次数
  CALL_FAIL_ALL_COUNT = 'total_error_count', // 总调用失败次数
  CALL_TOTAL_TOKENS = 'total_token', // 调用总Tokens数
  INPUT_TOKENS = 'total_prompt_token', // 输入Tokens数
  OUTPUT_TOKENS = 'total_completion_token', // 输出Tokens数
  TOTAL_COMPLETION_TASKS = 'total_completion_tasks', // 已完成任务数量
  TOTAL_INFER_COUNT = 'total_infer_count', // 推理总量
  TOTAL_TOKEN = 'total_token', // 总推理Token数
}

export enum StatisticsDetailType {
  ERROR_CODE = 'error_code', // 错误码
  ERROR_COUNT = 'error_count', // 错误次数
  PERCENTAGE = 'ratio', // 占比（%）
  ERROR_DESC = 'error_desc', // 错误信息
}

// model evaluation list
export enum BatchInferenceListColumns {
  TASK_NAME = 'task_name', // 任务名称/ID
  TASK_STATUS = 'status', // 状态
  TASK_ID = 'task_id',
  MODEL_NAME = 'model_name', // 模型
  CREATE_TIME = 'create_time', // 创建时间
  DESCRIPTION = 'description', // 描述
  OPERATION_FIELD = 'operation', // 操作
  TOTAL_REQUEST_CNT = 'total_request_cnt', // 总请求条数
  DISPLAY_MODEL_NAME = 'displayModelName', // 展示模型名称
}

// 批量推理获取OBS类型
export enum EGetObsPathType {
  UPLOAD_DATA_FILE = 'uploadDataFile',
  SAVE_RESULT_FILE = 'saveResultFile',
}

// 接入点类型
export enum CustomEndpointEnum {
  SYSTEM = 'system',
  CUSTOM_FROM_MAAS = 'custom_from_maas',
  CUSTOM_FROM_MA = 'custom_from_modelarts_v2',
}

// 需要处理显示名称的数据来源
export enum FormatDataOrigin {
  MODEL_TEMPLATE = 'modelTemplate',
  MY_MODEL = 'myModel',
}

export enum FROZEN_TYPE {
  ILLEGAL = 'frozen_{ILLEGAL', // 违规冻结
  POLICE = 'frozen_{POLICE', // 公安冻结
  ARREAR = 'ARREAR', // 欠费冻结
}

/**
 * 排序方式
 */
export enum SortByField {
  CREATED_AT = 'created_at',
  UPDATED_AT = 'updated_at',
  TRANSITION_AT = 'transition_at',
  DISPLAY_INDEX = 'display_index',
  CREATE_TIME = 'create_time',
}

/**
 * 状态枚举
 */
export enum StatusListEnum {
  CREATING_MODEL = 'CREATING',
  CREATE_SUCCEEDED = 'CREATE_SUCCEEDED',
  CREATE_FAILED = 'CREATE_FAILED',
  CREATING = 'Creating',
  COMPLETED = 'Completed',
  RUNNING = 'Running',
  PENDING = 'Pending',
  STOPPED = 'Stopped',
  FAILED = 'Failed',
  STOPPING = 'Stopping',
  UNKNOWN = 'Unknown',
  DELETING = 'Deleting',
  DELETED = 'Deleted',
  TERMINATED = 'Terminated',
  ABNORMAL = 'Abnormal',
  CONCERNING = 'Concerning',
  RESTARTING = 'Restarting',
  TERMINATING = 'Terminating',
  WAITING = 'Waiting',
  DEPLOYING = 'Deploying',
  UPGRADING = 'Upgrade',
  SCALING = 'Scale',
  MODEL_DELETING = 'MODEL_DELETING',
  DELETE_FAILED = 'DELETE_FAILED',
  INITING = 'Initing', // 评测初始化
  PUBLISHED = 'PUBLISHED', // agent的状态-已发布
  DRAFT = 'DRAFT', // agent的状态草稿
  DEPLOYED = 'Deployed', // 自定义mcp部署状态
  UNDEPLOYED = 'UnDeployed', // 自定义mcp停用状态
  ANOMALIES = 'Failed', // 异常-批量推理
  VALIDATING = 'Validating', // 验证中-批量推理
  VALIDATION_FAILED = 'ValidationFailed', // 验证失败-批量推理
  INITIALIZING = 'Initializing', // 初始化-批量推理
}

export enum AlgorithmEnum {
  CREATE = '/algorithms/create',
  ASC = 'asc',
  DESC = 'desc',
}
