import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { AiAssistCreateHomeService } from '@routes/ai-assist-create-home/ai-assist-create-home.service';

@Injectable({
  providedIn: 'root',
})
export class AiAssistCreateWorkflowService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(
    private http: HttpService,
    private ctxServ: ContextService,
    private homeServ: AiAssistCreateHomeService, // 注入 HomeService
  ) {}

  /** 流式接口：生成工作流  */
  public createWorkflowChat(
    uuid: string,
    params: any,
    lang: string,
    handlers: any = {},
  ): any {
    let seeUrl = `${this.http.prefixPath}${this.ctxServ.baseUrl}/workflows/generator/conversations/${uuid}/chat?workspace_id=${this.http.getWorkspaceId()}&refresh=true`;

    return this.homeServ.configureJiuwenSse(seeUrl, params, lang, handlers);
  }
}
