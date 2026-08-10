import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { ContextService } from '@services/context.service';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { SSE } from '@shared/services/sse';

/**
 * 对话工作台服务：会话 CRUD API + 发送消息 SSE 薄封装
 * （SSE 事件注册模式与平台 webPageChatSSE 一致，URL 指向对话工作台新端点，不改共享 service）
 */
@Injectable({
  providedIn: 'root',
})
export class ConversationWorkspaceService {
  constructor(
    private http: HttpService,
    private ctxServ: ContextService,
    private configServ: AgentConfigService,
  ) {}

  private get sessionsUrl(): string {
    // 相对路径：HttpService.mergeConfig 会自动前置 prefixPath，传绝对路径会双重前缀导致 URL 非法
    return `${this.ctxServ.baseUrl}/conversation/sessions`;
  }

  /** 创建会话 */
  public createSession(params: any = {}): Promise<any> {
    return this.http.postAsync({
      url: this.sessionsUrl,
      params,
      query: { workspace_id: this.http.getWorkspaceId() },
    });
  }

  /** 会话列表（updated_on 倒序，分页） */
  public listSessions(page = 0, size = 100): Promise<any> {
    return this.http.getAsync({
      url: this.sessionsUrl,
      query: {
        workspace_id: this.http.getWorkspaceId(),
        page,
        size,
      },
    });
  }

  /** 会话详情（含全部消息） */
  public detailSession(conversationId: string): Promise<any> {
    return this.http.getAsync({
      url: `${this.sessionsUrl}/${conversationId}`,
      query: { workspace_id: this.http.getWorkspaceId() },
    });
  }

  /** 删除会话（软删除） */
  public deleteSession(conversationId: string): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.sessionsUrl}/${conversationId}`,
      query: { workspace_id: this.http.getWorkspaceId() },
    });
  }

  /**
   * 发送消息（多轮对话入口，SSE 流式返回）
   * 薄封装复用 webPageChatSSE 的完整事件注册模式
   */
  public chatSSE(
    conversationId: string,
    params: any,
    {
      onStatus,
      onOpen,
      onMessage,
      onModeration,
      onTimeout,
      onDone,
      onError,
      onAbort,
      onReadyStateChange,
    }: any = {},
  ): any {
    const nilFunc = () => void 0;
    const url = `${this.http.prefixPath}/v1/${this.ctxServ.projectId}/conversation/sessions/${conversationId}/messages?workspace_id=${this.http.getWorkspaceId()}`;

    const { stream_first_chunk_timeout, stream_interval_timeout } =
      this.configServ.getConfigs();

    const source = new SSE(url, {
      headers: {
        'Content-Type': 'application/json',
        stream: 'true',
        'X-Language': 'zh-cn',
        'X-Invoke-Mode': 'PUBLISHED',
        projectname: JSON.parse(sessionStorage.getItem('cfCurrentRegion')),
        region: JSON.parse(sessionStorage?.getItem('cfCurrentRegion')),
      },
      payload: JSON.stringify({ ...params }),
      method: 'POST',
      withCsrf: true,
      timeout: 3600000,
      streamFirstChunkTimeout: stream_first_chunk_timeout ?? 180000,
      streamTimeout: stream_interval_timeout ?? 180000,
    });
    source.addEventListener('status', onStatus ?? nilFunc);
    source.addEventListener('open', onOpen ?? nilFunc);
    source.addEventListener('message', onMessage ?? nilFunc);
    source.addEventListener('error', onError ?? nilFunc);
    source.addEventListener('abort', onAbort ?? nilFunc);
    source.addEventListener('readystatechange', onReadyStateChange ?? nilFunc);
    source.addEventListener('moderation', onModeration ?? nilFunc);
    source.addEventListener('timeout', onTimeout ?? nilFunc);
    source.addEventListener('done', onDone ?? nilFunc);

    source.stream();

    return source;
  }
}
