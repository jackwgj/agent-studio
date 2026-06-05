import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { ContextService } from '@services/context.service';
import { CommonUtils } from '../../utils/common.util';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SessioMgnService {
  public lang: string;

  get prefix() {
    return this.ctxServ.baseUrl;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {
    this.lang = CommonUtils.getLanguage();
  }

  // 浏览会话列表
  public getSessionList(params: any): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/sessions`,
      params,
    });
  }

  // 查询会话详情
  public getSessionDeatil(session_id: any): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/sessions/${session_id}`,
    });
  }

  // 查询会话对话详情
  public getSessionDeatilList(session_id: any): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/sessions/${session_id}/traces`,
    });
  }

  // 查询会话下的trace
  public getTraceSessionDeatil(params: any): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/sessions/${params.session_id}/traces`,
      params,
    });
  }

  // 创建标签
  public addTag(params: any): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/tag`,
      params,
    });
  }

  // 不分页查询标签列表
  public getTagList(): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/tag/list`,
    });
  }

  // 分页查询标签列表
  public getTagListPage(params: any): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/tag/listPage`,
      query: { ...params },
    });
  }

  // 查询标签详情
  public getTagDetail(tagId: any): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/tag/${tagId}`,
    });
  }

  // 更新标签
  public updateTagDetail(params: any): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/ops/tag/${params.id}`,
      params,
    });
  }

  // 数据添加标签
  public addTagMark(params: any): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/tag/${params.span_Id}/mark`,
      dataType: 'text',
      params,
    });
  }

  // 数据更新标签
  public updateTagMark(params: any): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/ops/tag/${params.span_Id}/mark/${params.id}`,
      params,
    });
  }

  // 数据删除标签
  public daleteTagMark(params: any): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/ops/tag/${params.span_Id}/mark/${params.id}`,
      query: { ...params },
    });
  }

  // 点赞点踩
  public vote(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/tag/${params.span_Id}/vote`,
      params,
    });
  }

  // 查询数据标注标签
  public getTagMarkList(params: any): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/tag/${params.span_Id}/mark/list`,
      query: { ...params },
    });
  }
}
