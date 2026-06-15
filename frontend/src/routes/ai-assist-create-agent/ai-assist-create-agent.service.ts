import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { AiAssistCreateHomeService } from '@routes/ai-assist-create-home/ai-assist-create-home.service';

@Injectable({
  providedIn: 'root',
})
export class AiAssistCreateAgentService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(
    private http: HttpService,
    private ctxServ: ContextService,
    private homeServ: AiAssistCreateHomeService, // 注入 HomeService
  ) {}

  /** 流式接口：创建智能体  */
  public createAgentChat(
    uuid: string,
    params: any,
    lang: string,
    handlers: any = {},
  ): any {
    let seeUrl = `${this.http.prefixPath}${
      this.ctxServ.baseUrl
    }/agents/generator/conversations/${uuid}/chat?workspace_id=${this.http.getWorkspaceId()}&refresh=true`;

    return this.homeServ.configureJiuwenSse(seeUrl, params, lang, handlers);
  }
}
