import { Injectable } from '@angular/core';
import { HttpService } from '@services/http.service';
import { ContextService } from '@services/context.service';
import {
  OptimizeTaskDraft,
  OptimizeTaskItem, PromptType,
  UseCase,
} from '@interfaces/prompt/prompt-optimize-task.interface';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

/**
 * 用例导入相关
 */


@Injectable({
  providedIn: 'root',
})
export class UseCaseService {
  constructor() {}

  private importModalCloseSubject = new Subject<any>();
  private importErrMsgSubject = new BehaviorSubject<string>('');


  public importModalClose$: Observable<any> =
    this.importModalCloseSubject.asObservable();

  public closeImportModal(): void {
    this.importModalCloseSubject.next(null);
    this.importErrMsgSubject.next('');
  }
  public importErrMsg$: Observable<string> = this.importErrMsgSubject.asObservable();

  public pushErrorMessages(msg: string): void {
    this.importErrMsgSubject.next(msg);
  }
}
/**
 * 优化任务api接口
 */
@Injectable({
  providedIn: 'root',
})
export class PromptOptimizeService {
  get prefix() {
    return `${this.ctxServ.baseUrl}/prompt/tasks`.replace('v1', 'v2');
  }

  constructor(
    private http: HttpService,
    private readonly ctxServ: ContextService,
  ) {}

  /**
   * 创建草稿
   * @param draft
   */
  public createDraft(draft: OptimizeTaskDraft): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/draft`,
      params: draft,
    });
  }

  /**
   * 更新草稿
   * @param id
   * @param draft
   */
  public updateDraft(id: string, draft: OptimizeTaskDraft): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/${id}`,
      params: draft,
    });
  }

  /**
   * 创建任务
   * @param id
   */
  public createTask(id: string): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${id}`,
    });
  }

  /**
   * 重试任务
   * @param id
   */
  public retryTask(id: string): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${id}`,
    });
  }

  /**
   * 停止任务
   * @param id
   */
  public stopTask(id: string): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${id}/pause`,
    });
  }

  /**
   * 继续任务
   * @param id
   */
  public continueTask(id: string): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${id}/resume`,
    });
  }

  /**
   * 创建副本
   * @param id
   */
  public copyTask(id: string): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${id}/copy`,
    });
  }

  /**
   * 删除任务
   * @param id
   */
  public deleteTask(id: string): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/${id}`,
    });
  }

  public TaskDetail(id: string): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/${id}`,
    });
  }

  /**
   * 获取任务列表
   */
  public listTasks(
    params?: object,
    pageSize = 10,
    pageNum = 1,
  ): Promise<{
    message: string;
    data: {
      list: [OptimizeTaskItem];
      pageNum: number;
      pageSize: number;
      total: number;
    };
  }> {
    return this.http.getAsync({
      url: `${this.prefix}`,
      query: {
        ...params,
        pageSize,
        pageNum,
      },
    });
  }

  /**
   * 下载用例模板
   */
  public downLoadTemplate(promptType: PromptType): Promise<any> {
    return this.http.getBlobAsync({
      method: 'GET',
      url: `${this.prefix}/data/download`,
      query:{
        ptType: promptType,
      },
      dataType: 'blob',
    });
  }

  /**
   * 上传单条用例
   * @param taskId
   * @param useCase
   */
  public uploadOneUseCase(taskId, useCase: UseCase): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${taskId}/data/items`,
      params: useCase,
    });
  }

  /**
   * 批量上传用例
   * @param taskId
   * @param useCases
   */
  public uploadUseCases(taskId, useCases: UseCase[]): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${taskId}/data/batchItems`,
      params: useCases,
    });
  }

  public uploadImage(taskId, formatData: FormData): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${taskId}/upload/picture`,
      body: formatData,
    });
  }

  public uploadUseCaseByFile(taskId, formatData: FormData): Promise<any> {
    return this.http.postAsync({
      url: `${this.prefix}/${taskId}/data/import`,
      body: formatData,
    });
  }

  public updateOneUseCase(taskId, useCase: UseCase): Promise<any> {
    return this.http.putAsync({
      url: `${this.prefix}/${taskId}/data/items/${useCase.id}`,
      params: useCase,
    });
  }

  public deleteOneUseCase(taskId, useCaseId: string): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/${taskId}/data/items/${useCaseId}`,
    });
  }

  public listUseCase(taskId: string, pageNum = 1, pageSize = 10): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/${taskId}/data/list`,
      query: {
        pageNum,
        pageSize,
      },
    });
  }

  public listEvalDetail(taskId: string, iterNum): Promise<any> {
    return this.http.getAsync({
      url: `${this.prefix}/${taskId}/eval/${iterNum}`,
    });
  }

  public savePromptTemplate(params: any) {
    return this.http.postAsync({
      url: `${this.prefix}/prompt/save`,
      params: params,
    });
  }
}



