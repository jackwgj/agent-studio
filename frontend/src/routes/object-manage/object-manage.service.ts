import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class ObjectManageService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(
    private http: HttpService,
    private ctxServ: ContextService
  ) {}

  // 查询对象列表
  public getObjectsList(params): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/objects`,
      query: {
        offset: params.offset,
        limit: params.limit,
        ...(params?.name && { name: params.name }),
      },
    });
  }

  // 删除对象
  public deleteObjects(params): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/objects/${params}`,
    });
  }

  // 新建对象
  public createObject(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/objects`,
      params,
    });
  }

  // 修改结构化信息
  public editObjects(params): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/objects/${params.id}`,
      params,
    });
  }

  // 查询结构化信息列表
  public getStructuredMessagesListAsync(params, signal?: AbortSignal): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/objects`,
      query: {
        offset: params.offset,
        limit: params.limit,
        ...(params?.name && { name: params.name }),
      },
      signal,
    });
  }
}
