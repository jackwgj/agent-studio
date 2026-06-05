import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { ContextService } from '@services/context.service';
import { CommonUtils } from '../../utils/common.util';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AppAgentDeploymentService {
  public lang: string;

  get prefix() {
    return this.ctxServ.baseUrl;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {
    this.lang = CommonUtils.getLanguage();
  }

  // 查看部署列表
  public getAgentDeploymentList(params: any): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/deploy`,
      query: { ...params },
    });
  }

  // 创建智能体实例
  public addAgentDeployment(params: any): Observable<any> {
    return this.http.post({
      url: `${this.prefix}/ops/deploy`,
      params,
    });
  }

  // 更新智能体实例
  public updateAgentDeployment(params: any): Observable<any> {
    return this.http.put({
      url: `${this.prefix}/ops/deploy/${params.deployId}`,
      params,
    });
  }

  // 获取部署详情
  public getAgentDeploymentDeatail(deployId): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/deploy/${deployId}`,
    });
  }

  // 获取环境列表
  public getEnvironmentList(): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/deploy/environment`,
    });
  }

  // 获取eip地址
  public geEipsList(): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/deploy/eip/eips`,
    });
  }

  // 获取网关地址
  public geNatsList(): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/deploy/nat/nats`,
    });
  }

  // 获取弹性策略详情
  public getFlexibleStrategy(elasticStrategyId): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/deploy/elasticstrategy/${elasticStrategyId}`,
    });
  }

  // 设置弹性策略详情
  public setFlexibleStrategy(params: any): Observable<any> {
    return this.http.post({
      url: `${this.prefix}/ops/deploy/elasticstrategy`,
      params,
    });
  }

  // 更新弹性策略详情
  public updateFlexibleStrategy(params: any): Observable<any> {
    return this.http.put({
      url: `${this.prefix}/ops/deploy/elasticstrategy/${params.elasticStrategyId}`,
      params,
    });
  }

  // 启动停止部署
  public stopOrStart(params: any): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/deploy/${params.deployId}/operate`,
      params,
    });
  }

  // 获取总数
  public getAgentTotal(): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/ops/deploy/total`,
    });
  }

  //删除部署应用
  public deleteAgentDeployment(agent_id) {
    return this.http.deleteAsync({
      url: `${this.prefix}/ops/deploy/${agent_id}`,
    });
  }

  // 运营查看指标
  public getOverall(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/statistic/indicator_operation`,
      signal: params.signal,
      params,
    });
  }
  // 查看Top10指标
  public getTopOverall(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/statistic/indicator_top`,
      signal: params.signal,
      params,
    });
  }

  // 运营查看指标
  public getDomain(params): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/ops/statistic/indicator_domain`,
      signal: params.signal,
      params,
    });
  }
}
