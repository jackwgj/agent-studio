import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { Observable, catchError, map, of } from 'rxjs';
import {
  IModelAnswerReq,
  IModelAnswerRes,
  IQueryHistoryReq,
  IQueryHistoryRes,
  ITaskInfo,
  ITaskInfoQueryRes,
  ITaskNumData,
} from '@interfaces/prompt/prompt-writing-repo.interface';
@Injectable({
  providedIn: 'root',
})
export class PromptWritingRepoService {
  constructor(
    private http: HttpService,
    private ctxServ: ContextService
  ) {}

  public isOpAccount(): Observable<boolean> {
    return this.http.get({
      url: `${this.ctxServ.baseUrl}/agent-builder/prompt-engineering/template/is-admin`,
    });
  }

  public uploadFile(formatData: FormData, signal?): Promise<any> {
    return this.http.postAsync({
      url: `${this.ctxServ.baseUrl}/agent-builder/upload-file`,
      body: formatData,
      signal,
    });
  }

  public getTaskInfo(task_id: string): Observable<ITaskInfo> {
    return this.http
      .get<ITaskInfoQueryRes>({
        url: `${this.ctxServ.baseUrl}/agent-builder/prompt/task/list`,
        query: {
          task_ids: [task_id],
          limit: 1,
          offset: 0,
        },
      })
      .pipe(map(res => res.data[0]));
  }
}
