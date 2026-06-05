/* eslint-disable eslint-comments/disable-enable-pair */

/* eslint-disable @typescript-eslint/naming-convention */

export interface sceneMessageType {
  plans: Plan[];
  role: 'assistant' | 'user';
  status: string,
  content?: string;
  time_consumption?: object;
  event?:string;
  cards?:any[]
}

export interface waitNodeType {
  index: number;
  sceneMessage: sceneMessageType;
}

interface Plan {
  id?: string;
  title: string;
  status: string;
  steps?: Step[];
  actionSteps?:any[]
}
interface Step {
  actions?:any
  // 这里可以定义步骤的具体属性
}

export interface SummaryResponse {
  event: "summary_response";
  role: 'assistant';
  isCard?:boolean;
  cards?:any[]
}
