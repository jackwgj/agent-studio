import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { ContextService } from '@services/context.service';
import { CommonUtils } from '../../utils/common.util';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DatasetsService {
  public lang: string;

  get prefix() {
    return this.ctxServ.baseUrl;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {
    this.lang = CommonUtils.getLanguage();
  }

  // 查询评测集列表
  public getevaluationSetsList(params: any): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/evaluation-sets/list`,
      params,
    });
  }

  // 创建评测集
  public addEvaluationSets(params: any): Observable<any> {
    return this.http.post({
      url: `${this.prefix}/ops/evaluation-sets`,
      params,
    });
  }

  // 更新评测集
  public putEvaluationSets(params: any): Observable<any> {
    return this.http.put({
      url: `${this.prefix}/ops/evaluation-sets/${params.id}`,
      params,
    });
  }

  // 查询评测集详情
  public getEvaluationSetDetail(id): Observable<any> {
    return this.http.get({
      url: `${this.prefix}/ops/evaluation-sets/${id}`,
    });
  }

  // 查询版本评测集详情
  public getEvaluationSetVersionDetail(param): Observable<any> {
    return this.http.get({
      url: `${this.prefix}/ops/evaluation-sets/${param.evaluationSetId}/versions/${param.version_id}`,
    });
  }

  // 提交评测集新版本
  public updateEvaluationSetVersion(params): Observable<any> {
    return this.http.post({
      url: `${this.prefix}/ops/evaluation-sets/${params.evaluation_set_id}/versions`,
      params,
    });
  }
  // 查询评测集版本列表
  public queryEvaluationSetVersions(params): Observable<any> {
    return this.http.post({
      url: `${this.prefix}/ops/evaluation-sets/${params.evaluationSetId}/versions/list`,
      params,
    });
  }

  // 查询评测集数据项列表
  public getEvaluationSetItemsList(params): Observable<any> {
    return this.http.get({
      url: `${this.prefix}/ops/evaluation-sets/${params.evaluationSetId}/items`,
      query: { ...params },
    });
  }

  // 批量创建数据集数据项接口
  public batchAddEvaluationSetItems(params): Observable<any> {
    return this.http.post({
      url: `${this.prefix}/ops/evaluation-sets/${params.evaluationSetId}/items/batch-add`,
      params,
    });
  }

  // 一键添加到评测集
  public batchAddTraceEvaluationSetItems(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/trace/evaluation-sets/items/batch-add`,
      dataType: 'text',
      params,
    });
  }

  // 批量删除数据集数据项接口
  public batchDelEvaluationSetItems(params): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/ops/evaluation-sets/${params.evaluationSetId}/items/batch-delete`,
      params,
    });
  }

  // 下载评测集导入模板
  public downDatasetTemplate(evaluationSetId): Promise<any> {
    return this.http.getAsync({
      dataType: 'blob',
      url: `${this.prefix}/ops/evaluation-sets/${evaluationSetId}/items/template`,
    });
  }

  // 查询数据项详情
  public getEvaluationSetItemsDetail(params): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/evaluation-sets/${params.evaluationSetId}/items/${params.groupId}`,
      query: { ...params },
    });
  }

  getEvaluationSetItemsDetailByObservable(params): Observable<any> {
    return this.http.get({
      url: `${this.prefix}/ops/evaluation-sets/${params.evaluationSetId}/items/${params.groupId}`,
      query: { ...params },
    });
  }
  // 更新数据项组信息
  public putEvaluationSetItemsDetail(params): Observable<any> {
    return this.http.put({
      url: `${this.prefix}/ops/evaluation-sets/${params.evaluationSetId}/items/${params.groupId}`,
      params,
    });
  }
}
