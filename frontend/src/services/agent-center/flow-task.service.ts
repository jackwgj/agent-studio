import { Injectable } from '@angular/core';
import {
  FlowCancelTaskRes,
  FlowCreateTaskParams,
  FlowCreateTaskQuery,
  FlowExecutionInfo,
  FlowExecutionListQuery,
  FlowRunTaskReq,
  FlowRunTaskRes,
  FlowTask,
  FlowTaskDetailRes,
  FlowTaskHistoryRes,
  FlowTaskListRes
} from '@routes/agent-center/app-flow/flow-async-test/flow-async-test.interface';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class FlowTaskService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager/workflows`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  public getTaskList(workflowId: string): Promise<FlowTaskListRes> {
    return this.http.getAsync({
      url: `${this.prefix}/${workflowId}/tasks`
    });
  }

  /** 创建工作流任务 */
  public createTask(workflowId: string, params: FlowCreateTaskParams, query: FlowCreateTaskQuery): Promise<FlowTaskDetailRes> {
    return this.http.postAsync({
      url: `${this.prefix}/${workflowId}/tasks`,
      params,
      query
    });
  }

  /** 修改工作流任务 */
  public putTask(workflowId: string, taskId: string, params: FlowTask): Promise<FlowTaskDetailRes> {
    return this.http.putAsync({
      url: `${this.prefix}/${workflowId}/tasks/${taskId}`,
      params
    })
  }

  /** 查询工作流任务详情 */
  public getTaskDetail(workflowId: string, taskId: string): Promise<FlowTaskDetailRes> {
    return this.http.getAsync({
      url: `${this.prefix}/${workflowId}/tasks/${taskId}`
    })
  }

  /** 继续/开始执行任务 */
  public runTask(workflowId: string, taskId: string, params: FlowRunTaskReq): Promise<FlowRunTaskRes> {
    return this.http.postAsync({
      url: `${this.prefix}/${workflowId}/tasks/${taskId}`,
      params
    })
  }

  public deleteTask(workflowId: string, taskId: string): Promise<FlowCancelTaskRes> {
    return this.http.deleteAsync({
      url: `${this.prefix}/${workflowId}/tasks/${taskId}`,
    })
  }

  public cancelTask(workflowId: string, taskId: string): Promise<FlowCancelTaskRes> {
    return this.http.deleteAsync({
      url: `${this.prefix}/${workflowId}/tasks/${taskId}/cancel`,
    })
  }

  public getExecutionsList(workflowId: string, id: string, params: FlowExecutionListQuery): Promise<FlowExecutionInfo[]> {
    return this.http.getAsync({
      url: `${this.prefix}/${workflowId}/conversations/${id}/executions`,
      params
    })
  }

  public getExecutionsDetail(workflowId: string, id: string): Promise<FlowTaskHistoryRes> {
    return this.http.getAsync({
      url: `${this.ctxServ.baseUrl}/agent-manager/agents/${workflowId}/conversations/${id}/history`,
    })
  }
}