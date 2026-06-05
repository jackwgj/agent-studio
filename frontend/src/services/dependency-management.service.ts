import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DependencyManagementService {

  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }
  get cur_workspace_id() {
    return this.ctxServ.currentWorkspaceId;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  // 查询依赖包
  public queryDependencyPackages(params: any):any {
    return this.http.getAsync({
      url: `${this.prefix}/dependencies`,
      query: {
        ...params,
        workspace_id: this.cur_workspace_id,
      },
    });
  }
  // 删除依赖包
  public deleteDependencyPackages(dependency_id: string): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/dependencies/${dependency_id}`,
    });
  }
  // 创建依赖包
  public createDependencyPackages( params: any): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/dependencies`,
      body:params,
    });
  }
  // 修改依赖包
  public editDependencyPackages(dependency_id: string): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/dependencies/${dependency_id}`,
    });
  }
}


