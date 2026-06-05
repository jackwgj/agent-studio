import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { CommonService } from '@services/common.service';

@Injectable({
  providedIn: 'root',
})
export class DeepResearchSubService {
  /** 是否点击【试运行】按钮。若点击，【开始运行】按钮恢复可编辑状态 */
  private readonly isRunBtnClicked$ = new BehaviorSubject<boolean>(false);

  private readonly taskStatusChange$ = new BehaviorSubject<boolean>(false);

  private readonly createTask$ = new BehaviorSubject<object>({});

  //开启运行态会话
  private readonly startChat$ = new BehaviorSubject<object>({});

  private readonly changeTask$ = new BehaviorSubject<object>({});

  private readonly taskPage$ = new BehaviorSubject<boolean>(false);
  constructor(
    private ctxServ: ContextService,
    private commonService: CommonService,
  ) {}

  //创建任务
  public setTaskCreate(event: object): void {
    this.createTask$.next(event);
  }

  public taskCreate$() {
    return this.createTask$.asObservable();
  }

  //记录会话
  public setChat(event: object): void {
    this.startChat$.next(event);
  }

  public chatUpdate$() {
    return this.startChat$.asObservable();
  }

  //切换任务
  public setTaskChange(task: object): void {
    this.changeTask$.next(task);
  }

  public chatTaskUpdate$() {
    return this.changeTask$.asObservable();
  }

  //新建页面展示
  public setTaskPage(value: boolean): void {
    this.taskPage$.next(value);
  }

  public taskPageUpdate$() {
    return this.taskPage$.asObservable();
  }



}
