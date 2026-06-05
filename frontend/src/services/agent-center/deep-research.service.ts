import { Injectable } from '@angular/core';
import { IMappings } from '@routes/agent-center/types/common.types';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { SSE } from "@shared/services/sse";
import { AgentConfigService } from "@routes/agent-center/agent-config.service";

@Injectable({
  providedIn: 'root',
})
export class DeepResearchService {
  get prefix() {
    return `/v1/agentspace`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService, private configServ: AgentConfigService,) {}

  // 查询任务列表
  public getTaskList(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/task/list`,
      params,
    });
  }
  // 创建任务
  public creatTask(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/task/create`,
      params
    });
  }

  public renameTask(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/task/rename`,
      params
    });
  }

  public operateTask(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/task/operate`,
      params,
    });
  }

  // 查询任务详情
  public getTaskDetail(query): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/task/detail`,
      query
    });
  }
  // 第一次用户输入
  public creatChat(workflowId, params, query): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/chat`,
      params,
      query
    });
  }

  /** 调试预览 和 知识型应用 的流式接口 */
  deepResearchChatSSE(
    params: any,
    agent_id: string,
    conversation_id: string,
    invokeMode: 'DEBUG' | 'PUBLISHED',
    lang: string,
    {
      onStatus,
      onOpen,
      onMessage,
      onError,
      onAbort,
      onReadyStateChange,
      onModeration,
      onTimeout,
      onDone,
    }: any = {},
    version_id = '',
    app_type: 'agents' | 'workflows' = 'agents',
  ): any {
    const nilFunc = () => void 0;

    let seeUrl = `${this.http.prefixPath}${this.prefix}/chat?workspace_id=${this.http.getWorkspaceId()}`;

    const { stream_first_chunk_timeout, stream_interval_timeout } =
      this.configServ.getConfigs();

    const source = new SSE(seeUrl, {
      headers: {
        'Content-Type': 'application/json',
        'X-Invoke-Mode': invokeMode,
        stream: 'true',
        'X-Language': lang ?? 'zh-cn',
        projectname: JSON.parse(sessionStorage.getItem('cfCurrentRegion')),
        region: JSON.parse(sessionStorage.getItem('cfCurrentRegion')),
      },
      payload: JSON.stringify({ ...params }),
      method: 'POST',
      withCsrf: true,
      timeout: 3600000,
      streamFirstChunkTimeout: stream_first_chunk_timeout ?? 180000,
      streamTimeout: stream_interval_timeout ?? 180000,
    });
    source.addEventListener('open', onOpen ?? nilFunc);
    source.addEventListener('message', onMessage ?? nilFunc);
    source.addEventListener('status', onStatus ?? nilFunc);
    source.addEventListener('abort', onAbort ?? nilFunc);
    source.addEventListener('readystatechange', onReadyStateChange ?? nilFunc);
    source.addEventListener('moderation', onModeration ?? nilFunc);
    source.addEventListener('timeout', onTimeout ?? nilFunc);
    source.addEventListener('error', onError ?? nilFunc);
    source.addEventListener('done', onDone ?? nilFunc);

    source.stream();

    return source;
  }

  //查询任务详情
  public getFullMessageDetail(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/task/detail`,
      params,
    });
  }

  //查看完整会话历史消息 &恢复第一次对话（获取历史消息）&恢复第二次对话（获取历史消息）
  public getFullMessageList(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/message/list`,
      params,
    });
  }


  //下载文件
  public downloadFile(query): Promise<any> {
    return this.http.getBlobAsync({
      url: `${this.prefix}/file/download`,dataType: 'blob',
      query,
    });
  }


  public uploadFileTemplate(formData: FormData): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/file/upload`,
      body: formData,
    });
  }
}
