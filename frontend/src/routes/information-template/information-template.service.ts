import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class InformationTemplateService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  // 查询结构化信息列表
  public getStructuredMessagesList(params): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/structured-messages`,
      query: {
        offset: params.offset,
        limit: params.limit,
        ...(params?.name && { name: params.name }),
      },
    });
  }

  // 批量删除结构化信息
  public deleteStructuredMessages(params): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/structured-messages`,
      params: params,
    });
  }

  // 添加结构化信息
  public createStructuredMessages(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/structured-messages`,
      params,
    });
  }

  // 修改结构化信息
  public editStructuredMessages(params): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/structured-messages`,
      params,
    });
  }

  // 导入结构化信息
  public importStructuredMessages(formatData): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/structured-messages/upload-file`,
      body: formatData,
    });
  }

  // 导出结构化信息
  public exportStructuredMessages(params): Promise<any> {
    return this.http.postBlobAsync({
      url: `${this.prefix}/structured-messages/export-file`,
      params,
      dataType: 'blob',
    });
  }

  // 下载结构化信息模板
  public downloadTemplate(): Promise<any> {
    return this.http.postBlobAsync({
      url: `${this.prefix}/structured-messages/export-template`,
      dataType: 'blob',
    });
  }

  // 查询结构化信息列表
  public getStructuredMessagesListAsync(
    params,
    signal?: AbortSignal,
  ): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/structured-messages`,
      query: {
        offset: params.offset,
        limit: params.limit,
        ...(params?.name && { name: params.name }),
      },
      signal,
    });
  }
}
