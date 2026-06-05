export interface obj {
  [key: string]: any;
}

export interface ContentMessage {
  role: string;
  content: string;
}

export interface Invoke {
  name: string;
  invokeId: string;
  parentInvokeId?: string;
  invokeType: string;
  startTime: string;
  endTime: string;
  elapsedTime: string;
  inputs: string | ContentMessage[] | obj;
  outputs: string | ContentMessage[] | obj;
  error: string | null;
  executionOrderIndex: number;
  childInvokes?: Invoke[];
  metaData: obj;
  tokens?: obj | null;
  node_id?: string;
  node_uuid?: string;
}

export enum TimelineInvokeType {
  Start = 'Start',
  Code = 'Code',
  Branch = 'Branch',
  IntentDetection = 'IntentDetection',
  Questioner = 'Questioner',
  Plugin = 'Plugin',
  Mcp = 'Mcp',
  mcp = 'mcp',
  End = 'End',
  Message = 'Message',
  LLM = 'LLM',
  Knowledge = 'Knowledge',
  KnowledgeRepo = 'KnowledgeRepo',
  Aggregation = 'Aggregation',
  Workflow = 'Workflow',
  Input = 'Input',
  Loop = 'Loop',
  LoopInput = 'LoopInput',
  LoopOutput = 'LoopOutput',
  Http = 'Http',
  Agent = 'Agent',
  SetVariable = 'SetVariable',
  LTM = 'LTM',
  ComplexIntentDetection = 'ComplexIntentDetection',
  IntentDetectionContainer = 'IntentDetectionContainer',
  QA = 'QA',
  Sql = 'Sql',
  StreamTransform = 'StreamTransform',
}
