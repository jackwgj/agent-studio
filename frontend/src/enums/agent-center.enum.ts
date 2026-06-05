export enum ApplicationType {
  SINGLE_AGENT = 'agent',
  WORKFLOW = 'workflow',
  MULTI_AGENT = 'controller',
  CHAT_WORKFLOW = 'chat',
  TASK_WORKFLOW = 'task',
  PLUGIN = 'Plugin',
  STATISTICS='statistics ',
  PROMPT='prompt',
  AGENT_NEW='agent_new',
}

export enum PluginCredentialStatus {
  UNAUTHENTICATED = 'UNAUTHENTICATED',
  AUTHENTICATED = 'AUTHENTICATED',
  NONE = 'NONE',
}
