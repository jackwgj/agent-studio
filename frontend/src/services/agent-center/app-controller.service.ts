import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

export enum CONVERSATION_TYPE {
  AGENT = 'agent',
  CONTROLLER = 'controller',
}

@Injectable({
  providedIn: 'root',
})
export class AppControllerRepoService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  /** 查询日志的conversations列表 */
  public getConversationList(
    agentId: string,
    params: {
      start_time?: number;
      end_time?: number;
      limit?: number;
      offset?: number;
      type?: CONVERSATION_TYPE;
    } = {},
  ): Promise<any> {
    const { start_time, end_time, limit, offset, type } = params;

    const query: any = {};
    if (start_time !== undefined) {
      query.start_time = start_time;
    }
    if (end_time !== undefined) {
      query.end_time = end_time;
    }
    if (offset !== undefined) {
      query.offset = offset;
    }
    if (limit !== undefined) {
      query.limit = limit;
    }
    if (type !== undefined) {
      query.type = type;
    }
    return this.http.getAsync({
      url: `${this.prefix}/agents/${agentId}/conversations`,
      query,
    });
  }

  /** 查询日志的execution列表 */
  public getExecList(agentId: string, conversationId: string): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/controller/${agentId}/conversations/${conversationId}/executions`,
    });
  }

  /** 查询单次execution的信息 */
  public getSingleExecInfo(
    agentId: string,
    executionId: string,
  ): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/controller/${agentId}/executions/${executionId}`,
    });
  }
}
