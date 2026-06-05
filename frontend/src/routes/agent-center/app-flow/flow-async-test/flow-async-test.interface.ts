import { FlowAsyncExecRole } from "./flow-async-test.constant";

export enum FlowTaskStatus {
  INIT = 'INIT',
  PENDING ='PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

export interface FlowCreateTaskParams {
  type: 'chat' | 'task';
  name: string;
  mode: 'ASYNC';
  version?: string;
}

export interface FlowCreateTaskQuery {
  version?: string;
}

export interface FlowTask {
  id: string;
  name: string;
  type?: string;
  is_published: boolean;
  status: FlowTaskStatus;
  create_time: string;
  finish_time?: string;
}

export interface FlowTaskListRes {
  workflow_id: string;
  count: number;
  data: FlowTask[];
}

export interface FlowParsedTask extends FlowTask  {
  isEdit: boolean;
  editName: string;
}

interface FlowNodeStatus {
  code: number;
  desc: string;
}

interface FlowTaskOutput {
  outputs: {
    responseContent: string;
  };
  messages: FlowTaskMessage[];
  metadata: {
    enable_history: boolean;
  };
  status: FlowNodeStatus;
  conversation_id: string;
  start_time: number;
  end_time: number;
  knowledge_base_files_info: FileUploadRsp[];
  node_info: FlowOutputNodeInfo[];
}

interface FlowOutputNodeInfo {
  agent_id: string;
  node_id: string;
  node_status: string;
  parent_workflow_id: string;
  status: FlowNodeStatus;
  node_name: string;
  node_type: string;
  inputs: Record<string, object>;
  outputs: Record<string, object>;
  start_time: number;
  end_time?: number;
  execution_id: string;
  messages?: FlowTaskMessage[];
  metadata?: object;
  startup_time?: number;
  prefill_time?: number;
  raw_output?: string;
  total_tokens?: number;
  firstTokenTime?: string;
  output_tokens?: number;
  model_stats?: object;
  input_tokens?: number;
  requestStartTime?: string;
}

export interface FlowTaskDetailRes {
  id: string;
  name: string;
  conversation_id: string;
  is_published: boolean;
  workflow: {
    id: string;
    type: string;
    version_id: string;
  };
  status: FlowTaskStatus;
  create_time: string;
  update_time: string;
  finish_time?: string;
  message?: string;
  inputs?: {
    query: string;
  };
  outputs?: FlowTaskOutput;
}

export type FlowTaskHistoryRes = FlowTaskHistory[];

export type FlowTaskHistory = FlowTaskUserHistory | FlowTaskAssistantHistory;

interface FlowTaskUserHistory {
  role: FlowAsyncExecRole.USER;
  /** JSON字符串，格式："{\"query\":\"测试\"}" */
  content: string;
  create_time: number;
}

interface FlowTaskAssistantHistory {
  role: FlowAsyncExecRole.ASSISTANT;
  /** 历史消息，可能为json字符串或普通字符串 */
  content: string;
  create_time: number;
  execution_id: string;
  node_id: string;
}

export interface FlowRunTaskReq {
  // value类型与开始节点入参的类型挂钩
  inputs?: Record<string, any>;
  timeout?: number;
}

export interface FlowRunTaskRes {
  id: string;
  name: string;
  conversation_id?: string;
  is_published: boolean;
  workflow: {
    id: string;
    type: string;
    version_id: string;
  };
  status: FlowTaskStatus;
  message?: string;
  create_time: string;
  start_time?: string;
  update_time?: string;
  finish_time?: string;
  inputs?: Record<string, object>;
}

export interface FlowCancelTaskRes {
  id: string;
  name: string;
  workflow_id: string;
  status: FlowTaskStatus;
  message: string;
}

export interface FlowExecutionListQuery {
  offset?: number;
  limit?: number;
  version?: string;
}

export interface FlowExecutionInfo {
  parent_workflow_id: string;
  status: FlowExecutionStatus;
  error_code: string;
  error_message: string;
  outputs: Record<string, object>;
  metadata: object;
  start_time: number;
  end_time: number;
  execution_id: string;
}

interface FlowExecutionStatus {
  code: number;
  desc: string;
}

interface FileUploadRsp {
  url: string;
  headers: Record<string, string>;
  file_name: string;
}

interface FlowTaskMessage {
  role: string;
  content: string;
  create_time: number;
  name: object;
  function_call: object;
  tool_calls: object;
  tool_call_id: object;
  enable_history: boolean;
  intent: string[];
  files: object[];
  agent_id: string;
}