/** 对话工作台中浏览器可见的最小 Skill 目录项。 */
export interface ConversationSkillItem {
  skillId: string;
  name: string;
  description: string;
}

/** 浏览器发往对话工作台的本轮请求。 */
export interface ConversationFileReference {
  objectKey: string;
  fileName: string;
  size: number;
  checksum: string;
  progress?: 'loading' | 'succeeded' | 'failed';
  fileId?: string;
}

/** Runtime 已上传并由 Manager 持久化的正式输出产物。 */
export interface ConversationArtifactReference {
  objectKey: string;
  fileName: string;
  size: number;
  mediaType: string;
  checksum: string;
  executionId: string;
  runId: string;
  downloadState?: 'idle' | 'loading' | 'failed';
}

export interface ConversationSendRequest {
  query: string;
  model_deployment_id?: string;
  recommended_skill_ids: string[];
  select_type?: 'SUPERVISOR' | 'APP';
  app_id?: string;
  file_ids?: Array<{
    object_key: string;
    file_name: string;
    size: number;
    checksum: string;
  }>;
}

/** 工作台可执行目标。资源列表复用 AgentCenter 现有接口。 */
export interface ConversationExecutionTarget {
  id: string;
  name: string;
  type: 'SUPERVISOR' | 'SINGLE_AGENT' | 'MULTI_AGENT';
}

/** ConversationEvent wire event names shared by Runtime, Java, and the workspace. */
export enum ConversationEventType {
  RUN_START = 'run_start',
  MESSAGE = 'message',
  REASONING = 'reasoning',
  TOOL_CALL = 'tool_call',
  TOOL_RESULT = 'tool_result',
  RUN_END = 'run_end',
  ERROR = 'error',
  WORKFLOW_NODE = 'workflow_node',
  SKILL_ACTIVATED = 'skill_activated',
  ARTIFACT = 'artifact',
  USAGE = 'usage',
}

export type ConversationExecutionType = 'supervisor' | 'agent' | 'workflow' | 'unknown' | string;

export interface ConversationRunNode {
  runId: string;
  parentRunId?: string | null;
  executionType: ConversationExecutionType;
  agentId?: string;
  workflowId?: string;
  status: string;
  segments: ChatSegment[];
  detailSegments: ChatSegment[];
  workflowNodes: WorkflowNodeSegment[];
  activatedSkills: ConversationSkillActivation[];
  children: ConversationRunNode[];
}

export interface ConversationSkillActivation {
  skillId: string;
  name: string;
  versionId: string;
}

export interface ChatSegment {
  type: 'message' | 'reasoning' | 'tool';
  content: string;
  toolId?: string;
  toolCallId?: string;
  toolName?: string;
  toolStatus?: string;
  arguments?: unknown;
}

export interface WorkflowNodeSegment {
  nodeId?: string;
  nodeName?: string;
  nodeType?: string;
  nodeIndex?: number;
  status?: string;
  input?: unknown;
  output?: unknown;
  content?: string;
  errorCode?: string;
  errorMessage?: string;
}

export interface ConversationEventData {
  runId?: string;
  parentRunId?: string | null;
  executionType?: ConversationExecutionType;
  agentId?: string;
  workflowId?: string;
  nodeId?: string;
  nodeName?: string;
  nodeType?: string;
  nodeIndex?: number;
  toolId?: string;
  toolName?: string;
  arguments?: unknown;
  result?: unknown;
  status?: string;
  delta?: string;
  content?: string;
  text?: string;
  input?: unknown;
  output?: unknown;
  errorCode?: string;
  errorMessage?: string;
  skillId?: string;
  name?: string;
  versionId?: string;
  executionId?: string;
  objectKey?: string;
  fileName?: string;
  size?: number;
  mediaType?: string;
  checksum?: string;
  code?: string | number;
  message?: string;
  [key: string]: unknown;
}

export interface ConversationEvent {
  event: ConversationEventType | string;
  conversationId?: string;
  data?: ConversationEventData;
  index?: number;
  createdTime?: number;
}

/** 对话工作台 SSE 回调。 */
export interface ConversationSseCallbacks {
  onStatus?: (event: unknown) => void;
  onOpen?: () => void;
  onMessage?: (event: MessageEvent) => void;
  onModeration?: (event: unknown) => void;
  onTimeout?: () => void;
  onDone?: () => void;
  onError?: () => void;
  onAbort?: () => void;
  onReadyStateChange?: (event: unknown) => void;
}
