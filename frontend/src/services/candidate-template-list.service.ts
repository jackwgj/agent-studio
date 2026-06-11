import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { ContextService } from './context.service';
import { map, Observable } from 'rxjs';
import {
  IQueryParams,
  ICandidateTmplResponse,
  ICreateFormalTmpl,
  IUpdateCandidateTmplTitle,
  ICandidateTmplData,
  IQuerySingleTmplAssemble,
  IEvalState,
} from '@interfaces/prompt/candidate-template-list.interface';
@Injectable({
  providedIn: 'root',
})
export class CandidateTemplateListService {
  constructor(
    private http: HttpService,
    private ctxServ: ContextService
  ) {}

  /** 编辑候选模板名称 */
  public updateCandidateTmplTitle(params: any): Observable<any> {
    return this.http.post({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt-engineering/prompt/update-samll-prompt`,
      params,
      timeout: 30_000,
    });
  }

  /** 保存候选模板到模板库，作为正式模板 */
  public createFormalTmpl(params: ICreateFormalTmpl): Observable<any> {
    return this.http.post({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt-engineering/template/create`,
      params,
      timeout: 30_000,
    });
  }

  /** 删除单个候选模板 */
  public deleteCandidateTmpl(params: string): Observable<any> {
    return this.http.delete({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt-engineering/prompt/delete/${params}`,
      timeout: 30_000,
    });
  }

  /** 检查当前工程任务下，是否存在进行中的自动评估任务 */
  public queryEvaluationState(params: string): Observable<IEvalState> {
    return this.http.get({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt/evaluation/query-evaluation?id=${params}`,
      timeout: 30_000,
    });
  }

  /** 获取候选模板列表 */
  public getCandidateTmplList(params: IQueryParams): Observable<ICandidateTmplResponse> {
    return this.http.post({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt-engineering/prompt/list`,
      params,
      timeout: 30_000,
    });
  }

  /** 保存候选模板到模板库，作为正式模板 */
  public saveFormalTmpl(params: ICreateFormalTmpl): Observable<any> {
    return this.http.post({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt-engineering/template/save`,
      params,
      timeout: 30_000,
    });
  }
}
