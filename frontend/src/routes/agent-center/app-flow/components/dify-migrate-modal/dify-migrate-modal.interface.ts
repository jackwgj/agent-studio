export enum CompatibilityType {
  FULL_COMPATIBLE = 'fullCompatible',
  HALF_COMPATIBLE = 'halfCompatible',
  NOT_COMPATIBLE = 'notCompatible',
}

export enum MigrateNodeType {
  START = 'Start',
  END = 'End',
  LLM = 'LLM',
  IntentDetection = 'IntentDetection',
  Loop = 'Loop',
  LoopInput = 'LoopInput',
  LoopOutput = 'LoopOutput',
  Branch = 'Branch',
  Code = 'Code',
  Aggregation = 'Aggregation',
  KnowledgeRepo = 'KnowledgeRepo',
  SetVariable = 'SetVariable',
  Agent = 'Agent',
  Http = 'Http',
  Mcp = 'Mcp',
  Plugin = 'Plugin',
  Message = 'Message',
  Empty = 'Empty',

  // 虚拟节点
  Trigger = 'Trigger',
  GlobalConfig = 'GlobalConfig',
}
