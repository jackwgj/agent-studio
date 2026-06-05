import { Injectable } from '@angular/core';
import {
  IPlugin,
  PluginListReq,
  PluginListResponse,
  IDebugPluginResponse,
} from '@routes/agent-center/app-plugin/app-plugin.interface';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { MessageComponent } from '@shared/services/cfdata.service';
import { ShareListReq } from "@routes/app-center/components/edit-share-modal/edit-share-modal.interface";

@Injectable({
  providedIn: 'root',
})
export class ShareService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager/resource/share`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) { }

  /** 列表 别人分享给我的 */
  public getShareList(
    params: ShareListReq,
    signal?: AbortSignal,
  ): Promise<any> {

    return this.http.getAsync({
      url: `${this.ctxServ.baseUrl}/agent-manager/resource/shared`,
      query: params,
      signal,
    });
  }

  /** 列表 我分享给别人的 */
  public getMyShareList(
    params: any,
    signal?: AbortSignal,
  ): Promise<any> {

    return this.http.getAsync({
      url: `${this.prefix}`,
      query: params,
      signal,
    });
  }

  public getWorkspace(
    params: any,
    signal?: AbortSignal,
  ): Promise<any> {

    return this.http.getAsync({
      url: `${this.ctxServ.baseUrl}/agent-manager/workspace`,
      query: params,
      signal,
    });
  }

  // 修改
  public postShare(
    params: any,
    signal?: AbortSignal,
  ): Promise<any> {

    return this.http.postAsync({
      url: `${this.prefix}`,
      params: params,
      signal,
    });
  }


  public getShareDetail(
    id: string,
    params,
    signal?: AbortSignal,
  ): Promise<any> {

    return this.http.getAsync({
      url: `${this.prefix}/${id}`,
      query:params,
      signal,
    });
  }

  // 删除
  public deleteShare(
    id: string,
    params: any,
  ): Promise<any> {

    return this.http.deleteAsync({
      url: `${this.prefix}/${id}`,
      query:params,
    });
  }
}
