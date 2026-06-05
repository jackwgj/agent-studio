import { Injectable } from '@angular/core';
import { IPublishedAgentsListQueryResp } from '@routes/agent-center/app-flow/app-flow.types';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { BehaviorSubject, Observable } from 'rxjs';
@Injectable({
  providedIn: 'root',
})

export class SubControllerService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}
  /** 子agnets升级新版本调用put接口的请求体 */
  private subAgentsBody$ = new BehaviorSubject<any>('');

  private isResNodesUpdated$ = new BehaviorSubject<any>(false)

  /** 升级后的内容 */
  public setSubAgentsBody(body: any): void {
    this.subAgentsBody$.next(body);
  }

  public subAgentsBodyUpdate$(): Observable<any> {
    return this.subAgentsBody$.asObservable();
  }

  /** 记录资源节点列表是否升级新版本的结果 */
  public setAgentsNodes(childFlows: any): void {
    this.isResNodesUpdated$.next(childFlows);
  }

  public agentsNodesUpdate$(): Observable<any> {
    return this.isResNodesUpdated$.asObservable();
  }

  /*得到多智能的详情*/
  public getPublishedMutilAgentDetails(
    agents_id:string,version_id:string
  ):Promise<IPublishedAgentsListQueryResp>{
    const url = `${this.ctxServ.baseUrl}/agent-manager/agents/${agents_id}/versions/${version_id}`;
    return this.http.getAsync({
      url,
    });
  }
  /*得到多智能体是否更新*/
  public getMutilAgentLastVersion(
    agents_id:string
  ):Promise<IPublishedAgentsListQueryResp>{
    const url = `${this.ctxServ.baseUrl}/agent-manager/relation/apps/${agents_id}?showlatest=true&version=latest`;
    return this.http.getAsync({
      url,
    });
  }
}
