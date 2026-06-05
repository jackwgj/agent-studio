import { EModelQuerySource } from '@enums/prompt-editor.enum';
import { IModelParam, IModelServInfo } from './model.interface';
import { IVariant } from './prompt-editor.interface';
import { IFileInfo } from "@interfaces/prompt/candidate-template-list.interface";

export type IModelParamExtend = IModelParam & IModelServInfo;

export interface IModelAnswerReq {
  task_id: string;

  /** 模型调用请求来源 */
  source: EModelQuerySource;

  question: string;
  file_id: string;
  variables: IVariant[];
  model: string;
  model_config: IModelParamExtend;
  model_type: string;
}

export interface IModelAnswerRes {
  result: string;
  token: number;
}

export interface ITaskNumData {
  prompt_num: number;
  [key: string]: number;
}

export interface IQueryHistoryReq {
  type: number;
  task_id: string;
}

export interface IQueryHistoryRes {
  data: IQueryHistoryResData[];
}

export interface IQueryHistoryResData {
  id: string;
  content: string;
  model: string;
  answer: string;
  model_config: IModelParam;
  variable_vo: IVariant[];
  updated_on: string;
  file_info:IFileInfo
}

export interface IWritingQuery {
  taskID: string;
  candidateId?: string;
}

export interface ITaskInfo {
  description: string;
  id: string;
  name: string;
  tag_list: {
    id: string;
    name: string;
  }[];
}

export interface ITaskInfoQueryRes {
  data: ITaskInfo[];
}

export interface INewVarsInfo {
  count: number;
  newVars: IVariant[];
}

export interface IModelListQueryRes {
  model_service_info_list: IModelServInfo[];
}
