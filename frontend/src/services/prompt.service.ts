/**
 * prompt平台
 */

import { Injectable } from '@angular/core';
import { HttpService as NHttpService } from './http.service';
import { Observable, map } from 'rxjs';
import { ContextService } from './context.service';
import { AssertSquareTagType } from '@routes/agent-center/app-agent/common-logic-agent';
import { IBlobResponse } from '@interfaces/prompt/common.interface';
export interface IIndustry {
  id: string;
  name: string;
  name_en: string;
  description: string;
  created_on: string;
  updated_on: string;
}

export interface ITag {
  id: string;
  name: string;
  nameEn: string;
  name_en?: string;
  createdOn: string;
  updatedOn: string;
}

export interface IGetTmplQuery {
  limit: number;
  offset: number;
  template_name?: string;
  tag_list?: string[];
  industry_list?: string[];
  template_id?: string;
  content?: string;
  source?: 'PRESET';
}

export interface ITmpl {
  pt_type: string;
  variables:string;
  content: string;
  created_on: string;
  creator: string;
  industry: IIndustry;
  tag_list: ITag[];
  template_id: string;
  template_name: string;
  updated_on: string;
  description?: string;
}

@Injectable({
  providedIn: 'root',
})
export class PromptService {
  constructor(private http: NHttpService, private ctxServ: ContextService) {
  }

  getTagListAsync(params: any): Promise<{
    count: number;
    data: ITag[];
    has_next_page: boolean;
    total_page: number;
  }> {
    return this.http.getAsync({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt/tag/list`,
      params: params,
      timeout: 30_000,
    });
  }
  getIndustryListAsync(): Promise<IIndustry[]> {
    return this.http.getAsync({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt/industry/list`,
      timeout: 30_000,
    });
  }

  getAssertTagAsync(query: any): Promise<AssertSquareTagType[]> {
    return this.http.getAsync({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt/industry/list`,
      query: query,
      timeout: 30_000,
    });
  }

  getPromptTemplateList(params: IGetTmplQuery, query?: any): Observable<{
    count: number;
    has_next_page: boolean;
    total_page: number;
    data: ITmpl[];
  }> {
    return this.http.post({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt-engineering/template/list`,
      params: params,
      query: query,
      timeout: 30_000,
    });
  }

  getPromptTemplateListAsync(
    params: IGetTmplQuery,
    signal?: AbortSignal,
    query?: any,
  ): Promise<{
    count: number;
    has_next_page: boolean;
    total_page: number;
    data: ITmpl[];
  }> {
    return this.http.postAsync({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt-engineering/template/list`,
      params: params,
      signal,
      query: query,
    });
  }
}
