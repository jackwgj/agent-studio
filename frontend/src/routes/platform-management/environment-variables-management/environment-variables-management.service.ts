import {Injectable} from '@angular/core';
import {ContextService} from '@services/context.service';
import {HttpService} from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class EnvironmentVariablesManagementService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  // 查询环境变量列表
  public getEnvVariablesList(params): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/environments-variables`,
      query: {
        offset: params.offset,
        limit: params.limit,
      },
    });
  }

  // 创建和更新环境变量
  public createEnvVariables(params, environment_id): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/environments/${environment_id}/variables`,
      params: {
        variables: params,
      },
    });
  }

  // 查询变量详情
  public getEnvVariablesDetail(environment_id): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/environments/${environment_id}/variables`,
    });
  }

  // 导出变量
  public exportEnvVariables(params): Promise<any> {
    return this.http.postBlobAsync({
      url: `${this.prefix}/environments-variables/export`,
      params: params,
      dataType: 'blob',
    });
  }

  // 导入变量
  public importEnvVariables(file): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/environments-variables/import`,
      body: file
    });
  }

  // 校验导入
  public verifyImport(file): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/environments-variables/import-validation`,
      body: file,
    });
  }
}
