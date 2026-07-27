import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';

/**
 * 新建任务服务：对接平台后端 task-management 接口（/platform/v1/tasks*）。
 *
 * 注意：平台接口不属于 agent-studio 的 /v1（manager/service）体系，
 * 因此全部使用 overrideUrl 直连相对路径，由部署层（console nginx 或平台网关）转发：
 * - 裸 console(:80)：nginx location /platform/v1/ 提取 access_token Cookie 注入 Bearer 转发 :8004
 * - 平台网关(:8004)：同源直达 FastAPI，HttpOnly Cookie 自动携带
 */
@Injectable({ providedIn: 'root' })
export class NewTaskService {
  private readonly prefix = '/platform/v1/tasks';

  constructor(private http: HttpService) {}

  /** 创建任务（创建即拉起 agent loop 执行） */
  public createTask(params: {
    agent_id: string;
    name: string;
    params: Record<string, any>;
    project_id?: string;
    workspace_id?: string;
  }): Promise<any> {
    return this.http.postAsync({
      url: this.prefix,
      params,
      overrideUrl: true,
    });
  }

  /** 多轮追加重跑：在已有任务上追加一条对话并重新执行（new-task-chat） */
  public appendTurn(taskId: string, content: string, assets: any[] = []): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${taskId}/turn`,
      params: { content, assets },
      overrideUrl: true,
    });
  }

  /** 任务列表 */
  public listTasks(page = 1, pageSize = 20): Promise<any> {
    return this.http.getAsync({
      url: this.prefix,
      query: { page, page_size: pageSize },
      overrideUrl: true,
    });
  }

  /** 任务详情 */
  public getTask(taskId: string): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/${taskId}`,
      overrideUrl: true,
    });
  }

  /** 取消任务 */
  public cancelTask(taskId: string): Promise<any> {
    return this.http.patchAsync({
      url: `${this.prefix}/${taskId}`,
      params: {},
      overrideUrl: true,
    });
  }

  /** 进度 SSE 地址（EventSource 同源自动带 Cookie） */
  public streamUrl(taskId: string): string {
    return `${this.prefix}/${taskId}/stream`;
  }
}
