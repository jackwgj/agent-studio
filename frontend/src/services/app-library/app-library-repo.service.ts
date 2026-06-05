/**
 * 应用百宝箱
 */
import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { ContextService } from '@services/context.service';

@Injectable({
  providedIn: 'root',
})
export class AppLibraryRepoService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  /** 获取应用百宝箱列表 */
  public getAppCenterList(
    query: {
      source?: string;
      tag_id?: string;
      name?: string;
      workflow_type?: string;
      type?: string;
      selected?: boolean;
    },
    offset: number,
    limit: number,
    signal?: AbortSignal,
  ): Promise<any> {
    const { source, tag_id, name, workflow_type, type, selected } = query;
    return this.http.getAsync({
      url: `${this.prefix}/apps`,
      query: {
        ...(offset !== undefined && { offset }),
        ...(limit !== undefined && { limit }),
        ...(source !== undefined && { source }),
        ...(tag_id !== undefined && { tag_id }),
        ...(name !== undefined && { name }),
        ...(workflow_type !== undefined && { workflow_type }),
        ...(type !== undefined && { type }),
        ...(selected === true && { selected }),
      },
      signal,
    });
  }

  /** 获取标签列表。用于区分场景型应用和知识型应用属于哪几类标签 */
  public getTagList(): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/tags`,
    });
  }

  /** 获取工作流的基本信息 */
  public getKnowledgeInfo(agentId: any): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/workflows/scene/${agentId}`,
    });
  }

  /** 获取知识型应用的基本信息 */
  public getKnowledgeBot(agentId: any): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/agent-apps/${agentId}`,
    });
  }

  /** 获取场景型应用的基本信息 */
  getScenarioBot(workflowId: any): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/workflows/scene/${workflowId}`,
    });
  }

  /** 复制当前应用/工作流到工作台 */
  copyToWorkbench(appId: string, source: string): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/apps/${appId}/copy`,
      query: {
        target_workspace_id: this.ctxServ.currentWorkspaceId,
      },
      params: {
        source,
      },
    });
  }
}
