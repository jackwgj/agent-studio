/**
 * i18n 命名空间
 */
export enum I18nNamespace {
  /**
   * 在任何地方重复使用的东西
   * - 类似按钮的“确定”，“取消”
   */
  COMMON = 'common',

  /**
   * 总览
   */
  FEATURE_OVERVIEW = 'feature.overview',

  /**
   * prompt工具平台
   */
  PROMPT_PLATFORM = 'prompt-platform',

  /**
   * agent应用编排
   */
  AGENT = 'agent',

  NODE = 'node',

  STATIC_APP_ORCHESTRATION = 'static-app-orchestration',

  TOOL = 'tool',

  LLM_FLOW = 'flow',

  KNOWLEDGE = 'knowledge',

  AGENT_CENTER = 'agent-center',

  // 模型接入
  MODEL_ACCESS = 'model-access',

  //模型管理
  JIUWEN_MODEL = 'jiuwen-model',

  //平台管理
  PLATFORM_MANAGEMENT = 'platform_management',

  // 依赖管理
  DEPENDENCY = 'dependency',

  ERROR = 'error',

  STATISTICS = 'statistics',

  TRACE = 'trace',

  REVIEW = 'review',

  SUBSCRIPTION = 'subscription',
  // 手机端展示页面
  MOBILE = 'mobile',

  AGENT_CORE = 'agent-core',

  // 指引
  GUIDE = 'guide',

  SPEC_DRIVEN = 'spec-driven',

  // 链接
  LINK = 'link',
  //KMS
  KMS = 'kms',

  // 记忆库
  MEMORY_LIB = 'memory-lib',
}
