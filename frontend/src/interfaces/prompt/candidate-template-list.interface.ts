import { IModel, IModelServInfo } from './model.interface';
import { ITmplPrompt, IVariant } from './prompt-editor.interface';

/** 获取候选模板列表的相关接口类型 */
export interface IQueryParams {
  offset: number;
  limit: number;
  task_id: string;
  name: string;
  content: string;
  source: string;
  model: string;
  creator: string;
  query_self: boolean;
  is_good_rating: boolean;
}

export interface ICandidateTmplResponse {
  data: ICandidateTmplData[];
  count: number;
  total_page: number;
  has_next_page: boolean;
}

export interface IFileInfo {
  file_id: string;
  tempUrl: string;
}

export interface ICandidateTmplData {
  id: string;
  name: string;
  content: string;
  answer?: string;
  model: string;
  manual_score: number;
  model_config: IModelConfigOutput & IModelServInfo;
  creator: string;
  updater: string;
  created_on: string;
  updated_on: string;
  evaluation_avg: number;
  variable_vo?: IVariable[];
  file_info?: IFileInfo;
  is_workflow?: boolean;
}

export interface IModelConfigOutput {
  user: string;
  temperature: number;
  top_p: number;
  max_tokens: number;
  n: number;
  presence_penalty: number;
}

/** 保存候选模板作为正式模板的相关接口类型 */
export interface ICreateFormalTmpl {
  id: string;
  name: string;
  content: string;
  source: string;
  variables: string;
}

/** 编辑候选模板名称的相关接口类型 */
export interface IUpdateCandidateTmplTitle {
  id: string;
  manual_score?: string;
  name: string;
}

/** 撰写页面，设为候选弹窗的相关接口类型 */
export interface IAddCandidateTmpl {
  id: string;
  name: string;
  model: string;
  model_config: IModelConfigInput;
  task_id: string;
  source: string;
  type: string;
  question: string;
  answer: string;
  variables: IVariable[];
  manual_score: number;
  creator: string;
  updater: string;
}

export interface IVariable {
  id: string;
  name: string;
  key: string;
  value: string;
  pe_task_id: string;
}

export interface IModelConfigInput {
  n: number;
  topP: number;
  maxTokens: number;
  temperature: number;
  presencePenalty: number;
}

/** 查询单个候选模板接口，返回的组装好的回显属性 */
export interface IQuerySingleTmplAssemble {
  varPoolOrVariants: IVariant[];
  tmplPrompt: ITmplPrompt;
  content: string;
  editSingleTmpl: string;
  modelParams: IModel;
}

export interface IEvalState {
  id: string;
  name: string;
  description: string;
  method: string;
  task_id: string;
  test_set_id: string;
  test_set_name: any;
  case_num: number;
  created_on: string;
  status: string;
  prompt_list: IPromptList[];
  prompt_best_name: any;
  prompt_num: number;
  best_score: any;
  creator: string;
}

export interface IPromptList {
  id: string;
  name: string;
  content: string;
  answer: any;
  model: string;
  manual_score: number;
  model_config: IModelServInfo;
  evaluation_avg: any;
  variable_vo: any;
  creator: string;
  updater: string;
  created_on: string;
  updated_on: string;
}
