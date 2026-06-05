import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class ModelManagementService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/agent-manager`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  public getModels(query): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/custom-models`,
      query,
    });
  }

  public deleteModel(model_id) {
    return this.http.deleteAsync({
      url: `${this.prefix}/custom-models/${model_id}`,
    });
  }

  public createModel(params) {
    return this.http.postAsync({
      url: `${this.prefix}/custom-models`,
      params,
    });
  }
}
