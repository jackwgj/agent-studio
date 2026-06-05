import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { ModelType } from '@enums/jiuwen-model.enum';
import { ModelSquareListRes } from './model-management.interface';
import { catchError, map, Observable, of } from 'rxjs';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { CommonUtils } from 'src/utils/common.util';
import { CommonService } from '@services/common.service';

@Injectable({
  providedIn: 'root',
})
export class ModelManagementService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/model-manager`;
  }

  get observatoryPrefix() {
    return this.ctxServ.baseUrl;
  }

  private controller = new AbortController();
  private subscribeMap: Map<string, boolean> = new Map();

  constructor(private http: HttpService, private ctxServ: ContextService, private configServ: AgentConfigService, private commonService: CommonService) {}

  public getPublisherList(query, source): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/${source}/providers`,
      query,
    });
  }

  public getModelPublisherInfo(id, source): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/${source}/providers/${id}`,
    });
  }

  public createModelProviders(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/integration/providers`,
      params,
    });
  }

  public updateModelProviders(id, params): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/integration/providers/${id}`,
      params,
    });
  }

  public deleteProvider(id): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/integration/providers/${id}`,
    });
  }

  public getModelList(query): Promise<ModelSquareListRes> {
    return this.http.getAsync({
      url: `${this.prefix}/model-services`,
      query,
    });
  }

  public getModelSubscribeSetting(): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/user-subscribe-settings`,
    });
  }

  public subscribeModels(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/model-services/subscribe`,
      params,
    }).then(() => this.getMaaSModalSubscribe(true));
  }

  public getModelService(model_type: string = ModelType.LLM): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/available/model-services?model_type=${model_type}`,
    });
  }
  public getAvailableModelList(
    query?: any,
    signal?: AbortSignal,
  ): Promise<any> {
    return Promise.all([
      this.getMaaSModalSubscribe(),
      this.http.getAsync<any>({
        url: `${this.prefix}/available/model-services`,
        query,
        signal,
      })
    ]).then(([map, res]) => {
      return res;
    });
  }

  public publishModelInfo(model_id, status, query): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/model-services/${model_id}/${status}`,
      query: query,
    });
  }

  public createModel(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/model-services`,
      params,
    });
  }

  public updateModel(model_id, params): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/model-services/${model_id}`,
      params,
    });
  }


  public checkModelName(query): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/model-services/model-name/check`,
      query,
    });
  }
  public deleteModel(model_id) {
    return this.http.deleteAsync({
      url: `${this.prefix}/model-services/${model_id}`,
    });
  }

  public getProviderAuths(query) {
    return this.http.getAsync({
      url: `${this.prefix}/provider/auths`,
      query,
    });
  }

  public postProviderAuths(params, query) {
    return this.http.postAsync({
      url: `${this.prefix}/provider/auths`,
      params,
      query,
    });
  }

  public deleteProviderAuths(id) {
    return this.http.deleteAsync({
      url: `${this.prefix}/provider/auths/${id}`,
    });
  }

  public modifyModel(model_id, params) {
    return this.http.putAsync({
      url: `${this.prefix}/custom-models/${model_id}`,
      params,
    });
  }

  public getModelInfo(model_id): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/model-services/${model_id}`,
    });
  }

  public getModels(query, params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/custom-models/search`,
      query,
      params,
    });
  }

  public getProviders(): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/custom-models/provider`,
    });
  }

  public getAPIType(provider_id): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/custom-models/${provider_id}/api-type`,
    });
  }

  //删除路由策略
  public deleteModelRouterStrategies(strategy_id) {
    return this.http.deleteAsync({
      url: `${this.ctxServ.baseUrl}/model-router-strategies/${strategy_id}`,
    });
  }

  public getAvailableProvidersModelList(): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/available/providers/models`,
    });
  }

  //删除部署应用
  public deleteAgentDeployment(agent_id) {
    return this.http.deleteAsync({
      url: `${this.observatoryPrefix}/ops/deploy/${agent_id}`,
    });
  }

  // 删除评测集
  public deleteDatasets(id) {
    return this.http.deleteAsync({
      url: `${this.observatoryPrefix}/ops/evaluation-sets/${id}`,
    });
  }

  // 删除评测任务
  public deleteExperiment(params): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.observatoryPrefix}/ops/evaluation/experiment/delete`,
      params: {
        experiment_id_list: params,
      },
    });
  }

  // 删除评测算子
  public deleteEvaluators(id): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.observatoryPrefix}/ops/evaluators/${id}`,
    });
  }


  public getInterfaceProtocoList( query?:any): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/interface-protocol`,
      query,
    });
  }

  public postApiAuth(params) {
    return this.http.postAsync({
      url: `${this.observatoryPrefix}/api-auth/api-keys`,params
    });
  }

  public getApiAuthLists(query) {
    return this.http.getAsync({
      url: `${this.observatoryPrefix}/api-auth/api-keys/list`,
      query
    });
  }

  public deleteApiAuth(id) {
    return this.http.deleteAsync({
      url: `${this.observatoryPrefix}/api-auth/api-keys/${id}`,
    })
  }
  public getSynchronizeModelService(provider_id): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/model-services/sync?provider_id=${provider_id}`,
    });
  }

  /**
   * 查询MaaS模型的开通状态
   * @returns
   */
  public getMaaSModalSubscribe(isRefresh = false): Promise<any> {
    return Promise.resolve(new Map());
  }
}
