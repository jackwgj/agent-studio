import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { CommonService } from '@services/common.service';

@Injectable({
  providedIn: 'root',
})
export class FlowAsyncTestService {
  /** 是否点击【试运行】按钮。若点击，【开始运行】按钮恢复可编辑状态 */
  private readonly isRunBtnClicked$ = new BehaviorSubject<boolean>(false);

  private readonly taskStatusChange$ = new BehaviorSubject<boolean>(false);

  private readonly EventListChange$ = new BehaviorSubject<Array<any>>([]);
  constructor(
    private ctxServ: ContextService,
    private commonService: CommonService,
  ) {}

  public setRunBtnClicked(isClicked: boolean): void {
    this.isRunBtnClicked$.next(isClicked);
  }

  public runBtnClickedUpdate$(): Observable<boolean> {
    return this.isRunBtnClicked$.asObservable();
  }

  public setTaskStatusChange(isChange: boolean): void {
    this.taskStatusChange$.next(isChange);
  }

  public taskStatusUpdate$(): Observable<boolean> {
    return this.taskStatusChange$.asObservable();
  }

  public setEventListChange(isChange: Array<any>): void {
    this.EventListChange$.next(isChange);
  }

  public eventListUpdate$(): Observable<Array<any>> {
    return this.EventListChange$.asObservable();
  }
}
