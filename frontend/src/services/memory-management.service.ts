import { Injectable } from '@angular/core';

import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import {
  IChangeMemoryParams,
  IMemoryBasicQuery,
  IMemoryQueryParams,
  IMemoryRes,
} from '@shared/components/memory-management/memory-management.interface';

@Injectable({
  providedIn: 'root',
})
export class MemoryManagementService {
  isSupportUerPersona = false;

  get prefixAgentRuntimeV1() {
    return `${this.ctxServ.baseUrl}/agent-runtime`;
  }

  constructor(
    private configServ: AgentConfigService,
    private ctxServ: ContextService,
    private http: HttpService
  ) {
    this.isSupportUerPersona = Boolean(this.configServ.getConfigs().memory_user_profile_enable);
  }

  /**
   * persona  start
   */
  /**
   * 查看画像
   */
  queryMemory(query: IMemoryQueryParams): Promise<IMemoryRes> {
    return this.http.getAsync({
      url: `${this.prefixAgentRuntimeV1}/memories/long-terms`,
      query,
    });
  }

  /**
   * 修改画像值
   */
  changeMemoryContent(memoryData: IChangeMemoryParams, query: IMemoryBasicQuery) {
    return this.http.putAsync({
      url: `${this.prefixAgentRuntimeV1}/memories/long-terms`,
      params: memoryData,
      query,
    });
  }

  /**
   * 删除画像
   */
  deleteMemory(memoryIds: string[], query: IMemoryBasicQuery) {
    return this.http.postAsync({
      url: `${this.prefixAgentRuntimeV1}/memories/long-terms/batch-delete`,
      params: {
        memory_ids: memoryIds,
      },
      query,
    });
  }

  /**
   * 清空画像
   */
  clearMemory(query: IMemoryBasicQuery) {
    return this.http.deleteAsync({
      url: `${this.prefixAgentRuntimeV1}/memories/long-terms`,
      query,
    });
  }
}
