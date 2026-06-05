import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class AppAnalyzeRepoService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agents`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  public getDashboard(workflowId: string, params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${workflowId}/analytics/dashboard`,
      params,
    });
  }

  public exportDashboard(workflowId: string, params): Promise<any> {
    return this.http.getBlobAsync({
      method: 'POST',
      url: `${this.prefix}/${workflowId}/analytics/dashboard/export`,
      dataType: 'blob',
      params,
    });
  }
}
