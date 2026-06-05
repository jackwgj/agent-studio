import { IBaseSelectResp } from './common.interface';

export interface ITemplateList {
  data: any[];
  count: number;
}

export interface ITemplateData {
  creator: string;
  offset: number;
  limit: number;
  query_self: boolean;
  model: string;
  source: string;
}

export interface ILabel {
  id: string;
  label: string;
}

export interface IFormDetail {
  sourceInput: string;
  sourceOptions: ILabel[];
  sourceSelected: ILabel;
  otherInput: string;
  otherOptions: ILabel[];
  otherSelected: ILabel;
}

export interface ITmplCard {
  [key: string]: any;
  content: string;
  variables:string;
  pt_type: string;
  template_name: string;
  template_id: string;
  description: string;
  created_on:string;
}

interface IPaginationListBase {
  total_page: number;
  count: number;
}

export interface IPaginationListResp extends IPaginationListBase {
  [key: string]: any;
  data: IBaseSelectResp[];
}

export interface ITmplCardListResp extends IPaginationListBase {
  data: ITmplCard[];
}
