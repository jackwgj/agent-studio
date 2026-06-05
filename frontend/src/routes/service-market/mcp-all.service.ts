import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class McpAllService {
  tabPageType = 'official';
  get prefix() {
    return `${this.ctxServ.baseUrl}/mcp`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  // 查询MCP广场官方预置
  public getOfficialList(query): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/server/list`,
      query,
    });
  }

  // 查询MCP广场第三方
  public getMcpSquareThird(params, query): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/external-server/list`,
      query: query,
      params: params,
    });
  }

  // 查询MCP广场第三方详情
  public getMcpSquareThirdDetail(id,resource,version_id,org_type?): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/external-server/detail/${id}`,
      query: {
        resource: resource,
        version_id: version_id,
        ...(org_type && { org_type: org_type }),
      },
    });
  }
  /*第三方部署*/
  public thirdDeployBatch(queryObj, paramsObj): Promise<any> {
    return this.http.postAsync({
      url: `${this.ctxServ.baseUrl}/mcp/deploy/batch`,
      query: queryObj,
      params: paramsObj,
    });
  }
  /*第三方详情*/
  public getThirdDetail(queryObj, paramsObj,id): Promise<any> {
    return this.http.postAsync({
      url: `${this.ctxServ.baseUrl}/mcp/external-server/detail/${id}`,
      query: queryObj,
      params: paramsObj,
    });
  }
  public installThirdCommonParams(selectedRows:any){
   return  selectedRows.map((item:any) => {
      return {
        third_id: item.id,
        third_version_id: item.version_id,
        name: item.name,
        name_en: item.name,
        description: item.description,
        description_en: item.description,
        org_type: item.org_type,
        server_config: item.server_config,
        readme: item.readme,
        icon:item.icon,
      };
    });
  }
}
