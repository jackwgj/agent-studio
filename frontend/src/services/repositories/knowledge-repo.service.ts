import { Injectable } from '@angular/core';
import {
  IAssistantList,
  IAssistantToolQuery,
} from '@interfaces/tool/tool.interface';
import {
  ICreateKbReq,
  IKbListQuery,
  IKnowledge,
  IListFile,
  IListKnowledgeResp,
} from '@routes/knowledge/knowledge.types';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class KnowledgeRepoService {
  constructor(private http: HttpService, private ctxServ: ContextService) { }

  public getKnowledges(params: IKbListQuery): Observable<IListKnowledgeResp> {
    return this.http.post({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/knowledge/search`,
      query: {
        offset: params?.offset,
        limit: params?.limit,
      },
      params: {
        id: params?.id,
        name: params?.name,
        type: params.type,
      },
    });
  }

  public deleteKnowledge(id: string): Observable<void> {
    return this.http.delete({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/knowledge/${id}`,
    });
  }

  public getFileUploadUrl(knowledgeRepoId = '111') {
    return `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/knowledge/${knowledgeRepoId}/files`;
  }

  public createKnowledge(params: ICreateKbReq): Observable<IKnowledge> {
    return this.http.post({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/knowledge`,
      params,
    });
  }

  public modifyKnowledge(params: IKnowledge): Observable<IKnowledge> {
    return this.http.put({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/knowledge/${params.knowledge_repo_id}`,
      params,
    });
  }

  public getKnowledge(id: string): Observable<IKnowledge> {
    return this.http.get({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/knowledge/${id}`,
    });
  }

  public getFiles(id: string): Observable<IListFile> {
    return this.http.post({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/knowledge/${id}/files/search`,
      query: {
        offset: 0,
        limit: 1000,
      },
    });
  }

  public uploadFile(params: FormData, id: string) {
    return this.http.post({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/knowledge/${id}/files`,
      body: params,
    });
  }

  public downloadFile(fileId: string, knowledgeId: string) {
    return this.http.getBlobAsync({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/knowledge/${knowledgeId}/files/${fileId}/content`,
      dataType: 'blob',
    });
  }

  public deleteFile(fileId: string, knowledgeId: string) {
    return this.http.delete({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/knowledge/${knowledgeId}/files/${fileId}`,
    });
  }

  public getRefKnowledges(): Observable<IListKnowledgeResp> {
    return this.http.post({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/reference-knowledge`,
    });
  }

  public getLakesearchKbs(params: {
    endpoint: string;
    authorization: string;
  }): Observable<IListKnowledgeResp> {
    return this.http.post({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/reference-knowledge`,
      params,
      timeout: 10_000
    });
  }

  public getKnowledgeAssistants(params: {
    toolId: string;
    offset?: number;
    limit?: number;
  }): Observable<IAssistantList> {
    const { offset, limit } = params;
    return this.http.get({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/relations/knowledge/${params.toolId}/assistants`,
      query: {
        offset,
        limit,
      },
    });
  }

  /** 列出Assistant的所有工具 */
  public getAssistantKbs(params: IAssistantToolQuery) {
    return this.http.get<IListKnowledgeResp>({
      url: `${this.ctxServ.baseUrlWithWorkspace}/app-dev-api/relations/assistants/${params.assistantId}/knowledge`,
      query: {
        offset: params?.offset,
        limit: params?.limit,
      },
    });
  }
}
