/** 对话工作台中浏览器可见的最小 Skill 目录项。 */
export interface ConversationSkillItem {
  skillId: string;
  name: string;
  description: string;
}

/** 浏览器发往对话工作台的本轮请求。 */
export interface ConversationSendRequest {
  query: string;
  model_deployment_id: string;
  recommended_skill_ids: string[];
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
