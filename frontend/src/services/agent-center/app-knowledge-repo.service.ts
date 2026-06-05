import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { Observable } from 'rxjs';
import { ContextService } from '@services/context.service';

@Injectable({
  providedIn: 'root',
})
export class AppKnowledgeRepoService {
  constructor(
    private http: HttpService,
    private readonly ctxServ: ContextService,
  ) {}

  public processDocs(formatData: FormData): Observable<any> {
    return this.http.post({
      url: `${this.ctxServ.baseUrl}/knowledges/knowledge/doc`,
      body: formatData,
      timeout: 60_000,
    });
  }

  public processSites(params: any): Observable<any> {
    return this.http.post({
      url: `${this.ctxServ.baseUrl}/knowledges/knowledge/url`,
      params,
    });
  }
}
