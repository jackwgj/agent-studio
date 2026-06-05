export interface IRetrievePolicy {
  enabled?: boolean;
  type: string;
  rewriting_model: string;
  rewriting_model_name?: string;
  rewriting_model_endpoint?: string;
  top_n?: number;
  embedding_recall_threshold?: number;
  embedding_recall_break?: boolean;
}

export interface IHistoryMessagesPolicy {
  enabled?: boolean;
  type?: string;
  truncation_window?: number;
}

export interface IAssistantToolRef {
  tool_id: string;
  is_resident?: boolean;
}

export interface IKnowledgeRef {
  knowledge_repo_id: string;
  type?: string;
}

export interface IAssistant {
  assistant_id: string;
  name: string;
  url?: string;
  description?: string;
  model_deployment_id: string;
  model_endpoint?: string;
  model_name?: string;
  model_version?: string;
  opening?: string;
  recommendQs?: string[];
  instructions: string;
  tools?: IAssistantToolRef[];
  knowledge?: IKnowledgeRef[];
  tool_retrieve_policy?: IRetrievePolicy;
  history_messages_policy?: IHistoryMessagesPolicy;
  key_messages?: string[];
  created_on?: string;
  updated_on?: string;
}

export interface ICreateAssistant
  extends Omit<IAssistant, 'assistant_id' | 'tools'> {
  tools: IAssistantToolRef[];
}

export interface ITool {
  tool_id?: string;
  tool_display_name?: string;
  tool_desc: string;
  type?: 'inner' | 'custom';
  input_schema: Record<string, any> | string;
  output_schema: Record<string, any> | string;
  metadata?: Record<string, any> | string;
  created_on?: string;
  updated_on?: string;
  assistants?: IAssistant[];
}

export interface IViewTool {
  id?: string;
  name: string;
  code: string;
  modelUri: any;
  label?: string;

  // 新建未保存的状态为 saved，已创建的为 new
  state: 'saved' | 'new';

  disabled?: boolean;
}

interface IListBase {
  total_count: number;
}

export interface IToolList extends IListBase {
  tool_list: ITool[];
}

export interface IAssistantList extends IListBase {
  assistant_list: IAssistant[];
}

export interface IPaginationQuery {
  offset?: number;
  limit?: number;
}

/** 请求相关 */
export interface IListQuery extends IPaginationQuery {
  id?: string;
  name?: string;
}

export interface ICommonListItem {
  id: string;
  name: string;
}

export interface IModel extends ICommonListItem {
  // id 为 model_deployment_id
  version?: string;
  model_endpoint?: string;
}

export interface IBuiltInTool {
  id: string;
  name: string;
  description: string;
  isUse: boolean;
}

export interface IToolFormValue {
  code: string;
  name: string;
  isValid: boolean;
}

export interface IAssistantCard {
  type: 'edit' | 'copy' | 'delete' | 'detail';
  id: string;
  name: string;
}

export interface IAssistantToolQuery extends IPaginationQuery {
  assistantId: string;
}

export interface IToolAssistantsQuery extends IPaginationQuery {
  toolId: string;
}

export interface IToolInvoking {
  tool_invoking_id: string;
  action_id: string;
  tool_id: string;
  tool_input: string;
  tool_output: string;
}

export interface IAssistantAction {
  action_id: string;
  message_id: string;
  thought: string;
  usage: {
    input_tokens: number;
    output_tokens: number;
    cost: number;
  };
}

export interface IRequiredAction extends IAssistantAction {
  tool_invoking_list?: IToolInvoking[];
  user_feed_back?: string;
}

export interface IAssistantRunningParam {
  instructions?: string;
  resident_tools?: string[];
  additional_instructions: string;
}

export interface IRunAssistantMsgContent {
  type: 'text';
  content: string;
}

export interface IRunAssistantMsg {
  message_id?: string;
  assistant_session_id?: string;
  role: 'user' | 'assistant';
  contents: IRunAssistantMsgContent[];
  actions?: IRequiredAction[];
}

export interface IRunAssistantBase {
  assistantId: string;
  sessionId: string;
}

export interface IRunAssistantReq extends IRunAssistantBase {
  messages: IRunAssistantMsg[];
  assistant_running_param?: IAssistantRunningParam;
}

export interface IAssistantSession {
  assistant_session_id: string;
  assistant_id: string;
  required_action?: IRequiredAction;

  // 当前 assistant 返回的信息
  assistant_messages: IRunAssistantMsg[];

  // 历史 assistant 返回信息
  history_messages?: IRunAssistantMsg[];

  status:
    | 'running'
    | 'requires_action'
    | 'finished'
    | 'expired'
    | 'failed'
    | 'pending';
}

export interface IToolOutputReq {
  assistantId: string;
  sessionId: string;
  toolInvokings: IToolInvoking[];
}

export interface IUserFeedbackReq {
  assistantId: string;
  sessionId: string;
  feedback: {
    action_id: string;
    feedback: string;
  };
}

export interface IStreamRunResp {
  content?: string;
  assistantSession?: IAssistantSession;
  object: string;
  [key: string]: any;
}
