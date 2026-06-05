export interface ITaskId {
  task_id: string;
  type: number;
}

export interface IPromptResponse {
  data: IPromptList[];
  count: number;
  total_page: number;
  has_next_page: boolean;
}
// 候选集列表
export interface IPromptList {
  id: string;
  name: string;
  content: string;
  answer: string;
  model: string;
  model_config: IMulModelConfig;
  variable_vo: IVariableList[];
  manual_score: number;
  creator: string;
  updater: string;
  created_on: string;
  updated_on: string;
}

export interface IVariableList {
  id: string;
  name: string;
  key: string;
  value: any;
  pe_task_id: string;
}

export interface IModelConfig {
  n: number;
  top_p: number;
  max_tokens: number;
  temperature: number;
  presence_penalty: number;
}
export interface IDiscrepancyList {
  label: string;
  id: string;
  content: string;
  model: string;
  model_config: IMulModelConfig;
  variable_vo: IVariableList[];
  manual_score: number;
  creator: string;
  updater: string;
  created_on: string;
  updated_on: string;
}
// 更新分数
export interface IUpdateScore {
  id: string;
  manual_score: number;
  name: string;
}

// 查看效果
export interface IViewEffect {
  task_id: string;
  source: string;
  question: string;
  variables: IVariable[];
  model: string;
  model_config: IMulModelConfig;
  model_type: string;
}

export interface IVariable {
  key: string;
  name: string;
  value: string;
}

export interface IViewResponse {
  result: string;
  model: string;
  model_type: string;
  token: number;
  time: number;
}
/** 变量集返回数据 */
export interface TDataSetResponse {
  datasets: IDataset[];
}

export interface IDataset {
  description: string;
  dataset_id: string;
  dataset_name: string;
  dataset_type: string;
  project_id: string;
  obs_path: string;
  create_time: string;
}

export interface IDatasetId {
  dataset_id: string;
}

export interface IVariant {
  id: string;
  key: string;
  value: string;
  name: string;
}
export interface ITmplPrompt {
  prompt: string;
  variants: IVariant[];
}

// 多模型参数
interface IMulModelConfig extends IModelConfig {
  deployment_id: string;
  project_id: string;
  model_id: string;
  endpoint: string;
  metadata: Metadata;
  model_type: string;
}

export interface Metadata {
  max_tokens: number;
}
