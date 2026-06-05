import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { SSE } from '@shared/services/sse';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class AppConversationRepoService {
  constructor(
    private ctx: ContextService,
    private configServ: AgentConfigService,
    private http: HttpService,
  ) {}

  /** 试运行整条工作流 或 运行场景型应用的流式接口 */
  public debugWorkflowSSE(
    params: any,
    workflow_id: string,
    conversation_id: string,
    invokeMode: 'DEBUG' | 'PUBLISHED',
    lang: string,
    { onMessage, onError, onTimeout, onDone }: any = {},
    version_id = '',
    assets = false,
  ): any {
    const nilFunc = () => void 0;
    const assetsSuffix = assets ? '-assets' : ''; //百宝箱页面接口需要添加-assets
    let seeUrl = `${this.http.prefixPath}${
      this.ctx.baseUrl
    }/agent-manager/workflows${assetsSuffix}/${workflow_id}/conversations/${conversation_id}?workspace_id=${this.http.getWorkspaceId()}`;
    if (version_id) {
      seeUrl = `${this.http.prefixPath}${
        this.ctx.baseUrl
      }/agent-manager/workflows${assetsSuffix}/${workflow_id}/conversations/${conversation_id}?version=${version_id}&workspace_id=${this.http.getWorkspaceId()}`;
    }
    if (params.environment_id && typeof params.environment_id === 'string') {
      seeUrl += `&environment_id=${params.environment_id}`;
    }
    const headers: any = {
      'Content-Type': 'application/json',
      stream: 'true',
      'X-Invoke-Mode': invokeMode,
      'X-Language': lang ?? 'zh-cn',
      projectname: JSON.parse(sessionStorage.getItem('cfCurrentRegion')),
      region: JSON.parse(sessionStorage.getItem('cfCurrentRegion')),
    };

    const { stream_first_chunk_timeout, stream_interval_timeout } =
      this.configServ.getConfigs();

    const source = new SSE(seeUrl, {
      headers,
      payload: JSON.stringify({ ...params }),
      method: 'POST',
      withCsrf: true,
      timeout: 3600000,
      streamFirstChunkTimeout: stream_first_chunk_timeout ?? 180000,
      streamTimeout: stream_interval_timeout ?? 180000,
    });
    source.addEventListener('message', onMessage ?? nilFunc);
    source.addEventListener('error', onError ?? nilFunc);
    source.addEventListener('timeout', onTimeout ?? nilFunc);
    source.addEventListener('done', onDone ?? nilFunc);

    source.stream();
    return source;
  }
}
