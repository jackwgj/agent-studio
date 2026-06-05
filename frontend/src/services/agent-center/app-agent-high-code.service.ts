import { Injectable } from '@angular/core';
import {
  StorageService
} from '@shared/services/cfdata.service';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

export interface highCodeAgentInfo {
  agent_id: string;
  name: string;
  type: string;
  description: string;
  icon: string;
  status: string;
  session_id: string;
  creator: string;
  creator_id: string;
  create_time: number;
  update_time: number;
  auto_gen_flag?: number;
}

@Injectable({
  providedIn: 'root',
})
export class AppAgentHighCodeService {
  get runPrefix() {
    return `${this.ctxServ.baseUrl}`;
  }

  get websocketUrlPrefix() {
    let wsPrefixPath = this.http.prefixPath;
    if (this.http.prefixPath.includes('http')) {
      // 说明是本地环境，默认用appWebPath
      wsPrefixPath = window.AppWebPath.endsWith('/') ? window.AppWebPath.slice(0, -1) : window.AppWebPath;
    }
    return `${wsPrefixPath}${this.runPrefix}/relay-agent-service/build/`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  getWebSockUrlSuffix() {
    return `?workspace_id=${this.http.getWorkspaceId()}`;
  }

  /**
   * 构建 WebSocket 认证协议（使用 Sec-Websocket-Protocol）
   * WebSocket 不支持自定义请求头，只能通过子协议传递认证信息
   *
   * 获取的认证信息包括：
   * cftk: Cookie token
   * cf2-cftk: Cookie token 2
   * X-Language: 语言设置
   * AgencyId: 代理 ID
   * ProjectName: 项目名称
   * region: 区域
   * const language = FrameData.getLanguage() || '';
    const agencyId = UtilService.getUrlParameter('agencyId', true);
    const projectName = UtilService.getProjectName();
    const region = UtilService.getRegion();
   *    // if (projectName) {
    //   protocols.push(`projectname|${encodeURIComponent(projectName)}`);
    // }
    // if (region) {
    //   protocols.push(`region|${encodeURIComponent(region)}`);
    // }
    // if (agencyId) {
    //   protocols.push(`agencyid|${encodeURIComponent(agencyId)}`);
    // }
    // if (language) {
    //   protocols.push(`language|${encodeURIComponent(language)}`);
    // }
   * @returns WebSocket 子协议数组，格式：['key|value', ...]
   */
  buildWebSocketProtocols(): string[] {
    const cftk =
      StorageService.getCookie(`${window.app_cookie_prefix || ''}cftk`) || '';
    const cf2Cftk =
      StorageService.getCookie(`${window.app_cookie_prefix || ''}cf2_cftk`) ||
      '';

    const protocols: string[] = [];

    // 将认证信息转换为子协议格式: key|value
    if (cftk) {
      protocols.push(`cftk|${encodeURIComponent(cftk)}`);
    }
    if (cf2Cftk) {
      protocols.push(`cf2-cftk|${encodeURIComponent(cf2Cftk)}`);
    }

    return protocols;
  }

  /** 获取单个高代码agent详情 */
  public getHighAgentDetail(agentId: string): Promise<any> {
    return this.http.getAsync({
      url: `${this.runPrefix}/relay-agent-builder/agents/${agentId}`,
    });
  }

  /** 创建高代码agent */
  public createHighAgent(params: any): Promise<any> {
    return this.http.postAsync({
      url: `${this.runPrefix}/relay-agent-builder/agents`,
      params,
    });
  }

  /** 删除一个高代码agent */
  public deleteHighAgent(agent_id: string): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.runPrefix}/relay-agent-builder/agents/${agent_id}`,
    });
  }

  /** 获取高代码agent列表 */
  public getHighAgentList(
    params: any,
    offset: number,
    limit: number,
  ): Promise<any> {
    const { id, name, type, status, description, creator } = params;
    return this.http.getAsync({
      url: `${this.runPrefix}/relay-agent-builder/agents`,
      query: {
        ...(offset !== undefined && { offset }),
        ...(limit !== undefined && { limit }),
        ...(name !== undefined && { name }),
        ...(type !== undefined && { type }),
        ...(status !== undefined && { status }),
        ...(creator !== undefined && { creator }),
        ...(description !== undefined && { description }),
      },
    });
  }

  /** 修改 一个高代码agent */
  public changeHighAgent(agent_id: string, params: any): Promise<any> {
    return this.http.putAsync({
      url: `${this.runPrefix}/relay-agent-builder/agents/${agent_id}`,
      params,
    });
  }
}
