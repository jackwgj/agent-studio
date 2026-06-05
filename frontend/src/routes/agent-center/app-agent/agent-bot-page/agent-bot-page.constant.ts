export enum AGENT_MODE_CODE {
  GENERAL_MODE = 'agent',
  PLANNING_MODE = 'planexecute',
  DEEP_MODE = 'deepresearch',
}

export enum DISPATCH_MODE {
  MODEL_PRIORITY = 'ReAct',
  KNOWLEDGE_PRIORITY = 'RAG',
  TOOL_PRIORITY = 'ToolUse',
}

export const URL_FROM_MULTI_AGENT = 'multiAgent';
