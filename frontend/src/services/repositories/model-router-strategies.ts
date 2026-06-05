import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class ModelRouterStrategiesService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/model-router-strategies`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  public createModelRouterStrategies(params) {
    return this.http.postAsync({
      url: `${this.prefix}`,
      params,
    });
  }

  public getModelRouterStrategiesDetail(strategy_id) {
    return this.http.getAsync({
      url: `${this.prefix}/${strategy_id}`,
    });
  }

  public getModelRouterStrategiesList(query,signal?: AbortSignal) {
    return this.http.getAsync({
      url: `${this.prefix}`,query,
      signal
    });
  }

  public deleteModelRouterStrategies(strategy_id) {
    return this.http.deleteAsync({
      url: `${this.prefix}/${strategy_id}`,
    });
  }

  public modifyModelRouterStrategies(strategy_id, params) {
    return this.http.putAsync({
      url: `${this.prefix}/${strategy_id}`,
      params,
    });
  }

}
