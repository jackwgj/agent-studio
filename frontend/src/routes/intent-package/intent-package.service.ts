import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class IntentPackageService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  /** 查询意图包列表 */
  public getIntentGroupList(params: any): Promise<any> {
    const { offset, limit, name } = params;

    return this.http.getAsync({
      url: `${this.prefix}/complex-intent`,
      query: {
        ...(offset !== undefined && { offset }),
        ...(limit !== undefined && { limit }),
        ...(name !== undefined && { name }),
      },
    });
  }

  public createIntentPackage(params: any): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/complex-intent`,
      params,
    });
  }

  public updateIntentPackage(id: string, name: string): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/complex-intent/${id}`,
      params: {
        name,
      },
    });
  }

  public getIntentPackageDetail(params: string): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/complex-intent/${params}`,
    });
  }

  public deleteIntentPackage(params: string): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/complex-intent/${params}`,
    });
  }

  /** 查询某个意图包里的分支列表 */
  public getIntentBranchesList(params: any, intent_id: string): Promise<any> {
    const { offset, limit, name } = params;

    return this.http.getAsync({
      url: `${this.prefix}/complex-intent/${intent_id}/branch`,
      query: {
        ...(offset !== undefined && { offset }),
        ...(limit !== undefined && { limit }),
        ...(name !== undefined && { name }),
      },
    });
  }

  public createIntent(params: any): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/complex-intent`,
      params: params,
    });
  }

  public updateIntent(params: any): Promise<any> {
    const { id } = params;
    return this.http.putAsync({
      url: `${this.prefix}/complex-intent/${id}`,
      params: params,
    });
  }

  public deleteIntent(package_id: string, intent_id: string): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/complex-intent/${package_id}/branch/${intent_id}`,
    });
  }

  public downloadTemlate(): Promise<any> {
    return this.http.postBlobAsync({
      method: 'POST',
      url: `${this.prefix}/complex-intent/export-template`,
      dataType: 'blob',
    });
  }

  public exportIntent(params: any): Promise<any> {
    return this.http.postBlobAsync({
      method: 'POST',
      url: `${this.prefix}/complex-intent/export`,
      dataType: 'blob',
      params,
    });
  }

  // 上传意图包
  public uploadFile(formatData: FormData): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/complex-intent/import`,
      body: formatData,
      timeout: 120_000,
    });
  }

  // 上传意图分支
  public uploadIntent(
    intent_id: string,
    intent_name: string,
    formatData: FormData,
  ): Promise<any> {
    let url = intent_id
      ? `${this.prefix}/complex-intent/${intent_id}/branch/import`
      : `${this.prefix}/complex-intent/import`;
    const query = {
      intent_name,
    };
    return this.http.postAsync({
      url: url,
      body: formatData,
      query: intent_id ? {} : query,
    });
  }
}
