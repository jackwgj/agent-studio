import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { ContextService } from '@services/context.service';

@Injectable({
  providedIn: 'root',
})
export class ModalMgnService {
  get prefix() {
    return this.ctxServ.baseUrl;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}

  // 保存调优任务
  public saveModalTask(projectIdValue, params: any): Promise<any> {
    return this.http.postAsync({
      url: `/v1/${projectIdValue}/api/tuning/task/save`,
      params,
      dataType: 'text',
    });
  }

  // 保存并提交调优任务
  public saveAndSubmitModalTask(projectIdValue, params: any): Promise<any> {
    return this.http.postAsync({
      url: `/v1/${projectIdValue}/api/tuning/task/save-submit`,
      params,
      dataType: 'text',
    });
  }

  // 提交调优任务
  public submitModalTask(projectIdValue, task_id: any): Promise<any> {
    return this.http.postAsync({
      url: `/v1/${projectIdValue}/api/tuning/task/submit/${task_id}`,
    });
  }

  // 查询调优任务详情
  public getModalTaskDetail(projectIdValue, task_id: any): Promise<any> {
    return this.http.getAsync({
      url: `/v1/${projectIdValue}/api/tuning/task/${task_id}`,
    });
  }

  // 删除调优任务
  public delModalTaskDetail(projectIdValue, task_id: any): Promise<any> {
    return this.http.deleteAsync({
      url: `/v1/${projectIdValue}/api/tuning/task/${task_id}`,
    });
  }

  // 修改调优任务
  public updateModalTaskDetail(projectIdValue, params: any): Promise<any> {
    return this.http.putAsync({
      url: `/v1/${projectIdValue}/api/tuning/task/${params.task_id}`,
      params,
    });
  }

  // 查询部署列表查询
  public getModalTuningDeployList(projectIdValue, params: any): Promise<any> {
    return this.http.postAsync({
      url: `/v1/${projectIdValue}/api/tuning/deploy/list`,
      params,
    });
  }

  // 查询调优任务列表
  public getModalTuningList(projectIdValue, params: any): Promise<any> {
    return this.http.postAsync({
      url: `/v1/${projectIdValue}/api/tuning/task/list`,
      params,
    });
  }

  // 查询调优过程指标
  public getModalTaskProcess(projectIdValue, task_id: any): Promise<any> {
    return this.http.getAsync({
      url: `/v1/${projectIdValue}/api/tuning/process/${task_id}`,
    });
  }

  // 查询调优产物列表
  public getModalTaskProduct(
    projectIdValue,
    params: any,
    task_id,
  ): Promise<any> {
    return this.http.postAsync({
      url: `/v1/${projectIdValue}/api/tuning/product/${task_id}`,
      params,
    });
  }

  // 查询公共资源池
  public getPublicPool(projectIdValue): Promise<any> {
    return this.http.getAsync({
      url: `/v1/${projectIdValue}/api/tuning/public-pool`,
    });
  }

  // 查询专属资源池
  public getPrivatePool(projectIdValue): Promise<any> {
    return this.http.getAsync({
      url: `/v1/${projectIdValue}/api/tuning/private-pool`,
    });
  }

  // 查询Obs桶列表
  public getObsList(projectIdValue): Promise<any> {
    return this.http.getAsync({
      url: `/v1/${projectIdValue}/api/tuning/obs/bucketList`,
    });
  }

  // 查询Obs某个桶对象
  public getBucketObject(projectIdValue, params): Promise<any> {
    return this.http.postAsync({
      url: `/v1/${projectIdValue}/api/tuning/obs/bucketObject`,
      params,
    });
  }

  // 保存部署与接入
  public saveDeploy(projectIdValue, params): Promise<any> {
    return this.http.postAsync({
      url: `/v1/${projectIdValue}/api/tuning/deploy`,
      params,
    });
  }

  // 停止服务
  public stopDeploy(projectIdValue, deploy_id): Promise<any> {
    return this.http.postAsync({
      url: `/v1/${projectIdValue}/api/tuning/deploy/stop/${deploy_id}`,
    });
  }
}
