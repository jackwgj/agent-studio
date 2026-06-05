export interface IMemoParam {
  name: string;
  default_value: string;

  /** user 长期  conversation 单次会话 */
  life_cycle: 'user' | 'conversation';
  description: string;
  updated_on?: number | string; // 单智能体put接口body体，多了整形的updated_on
}

export interface IMemoModalInput {
  agentId: string;
  memos: IMemoParam[];
  selectedName?: string;
}

export interface IMemoModalUpdate {
  isUpdated: boolean;
  memos?: IMemoParam[];
}
