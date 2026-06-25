/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/naming-convention */

export interface ModelConfig {
  modelName: string;
  top_p?: number;
  temperature?: number;
  url?: string;
  healthy?: string;
  showUrl?: string;
  default?: string;
  label?: string;
  mode?: string;
}

export interface PluginContent {
  pluginId?: string;
  pluginName: string;
  pluginLogo: string;
  pluginDesc: string;
  pluginAvailable?: boolean;
}

export interface knowledgeContent {
  knowledgeId?: string;
  knowledgeName: string;
  knowledgeCount: number;
  status?: string;
}

export interface sopContent {
  sopId?: string;
  sopName: string;
  status?: string;
}

export interface TimeConsumption {
  total_latency: number;
  overall_latency?: number;
  model_latency: number;
  wait_latency: number;
  plugin_latency?: number;
}

export interface TimeConsumptionStr {
  total_latency_str: string;
  overall_latency_str: string;
  model_latency_str: string;
  wait_latency_str: string;
  plugin_latency_str?: string;
}

export interface ConversationContent {
  role: string;
  image: string;
  function_call?: {
    name: string;
    arguments: string;
  };
  content: any;
  name?: string;
  time_consumption?: TimeConsumption;
  data?: {
    answer: any;
    error_code?: string;
    error_message?: string;
  };
  conversation_id?: string;
  created_time?: string;
  event?: string;
  answer?: Record<string, any>;
}

export interface WorkflowType {
  workflowId: string;
  workflowName: string;
}

export interface DomainItem {
  key: string;
  value: string;
}

export interface PromptTagItem {
  name: string;
  type: string;
}

export interface FuncCallItem {
  pluginName: string;
  request: string;
  response: string;
  role: string;
  title: string;
  isRuning: boolean;
  time_consumption: TimeConsumption & { totalEx?: number };
  error_type?: string;
  error_summary?: string;
}
