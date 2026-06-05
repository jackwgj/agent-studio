import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from '@shared/services/cfdata.service';

@Injectable({
  providedIn: 'root',
})
export class RouteParamsService {
  /** 单个候选模板进入编辑状态，存储该候选模板的id */
  private readonly editSingleTmpl$: BehaviorSubject<string>;

  /** 2个候选模板进入横向评估 */
  private readonly horizontalTmpls$: BehaviorSubject<string>;

  /** 单个候选模板名称 */
  private readonly templateName$: BehaviorSubject<string>;

  /** 单个工程任务下，历史新建的候选模板数量 */
  private readonly iterationNumPerProject$: BehaviorSubject<string>;

  constructor () {
    // 存入 localStorage 中
    const savedEditSingleTmpl = localStorage.getItem('editSingleTmpl');
    this.editSingleTmpl$ = new BehaviorSubject<string>(savedEditSingleTmpl || '');
    const savedHorizontalTmpls = localStorage.getItem('horizontalTmpls');
    this.horizontalTmpls$ = new BehaviorSubject<string>(savedHorizontalTmpls || '');
    const savedTemplateName = localStorage.getItem('templateName');
    this.templateName$ = new BehaviorSubject<string>(savedTemplateName || '');
    const savedIterationNum = localStorage.getItem('iterationNumPerProject');
    this.iterationNumPerProject$ = new BehaviorSubject<string>(savedIterationNum || '');
  }
  /** 单个候选模板进入编辑状态，存储该候选模板的id */
  public setEditSingleTmpl(tmpl: string): void {
    this.editSingleTmpl$.next(tmpl);
    StorageService.setLocalStorage('editSingleTmpl', tmpl);
  }

  public editSingleTmplUpdate$(): Observable<string> {
    return this.editSingleTmpl$.asObservable();
  }

  public getEditSingleTmpl(): string {
    return this.editSingleTmpl$.getValue();
  }

  public clearEditSingleTmpl(): void {
    this.editSingleTmpl$.next('');
    localStorage.removeItem('editSingleTmpl');
  }

  /** 2个候选模板进入横向评估 */
  public setHorizontalTmpl(tmpls: string): void {
    this.horizontalTmpls$.next(tmpls);
    StorageService.setLocalStorage('horizontalTmpls', tmpls);
  }

  public HorizontalTmplUpdate$(): Observable<string> {
    return this.horizontalTmpls$.asObservable();
  }

  public getHorizontalTmpl(): string {
    return this.horizontalTmpls$.getValue();
  }

  /** 单个候选模板名称 */
  public setTemplateName(tmplName: string): void {
    this.templateName$.next(tmplName);
    StorageService.setLocalStorage('templateName', tmplName);
  }

  public getTemplateName(): string {
    return this.templateName$.getValue();
  }

  /** 单个工程任务下，历史新建的候选模板数量。若某个候选模板已删除，该变量也不会减1 */
  public setIterationNumPerProject(num: string): void {
    this.iterationNumPerProject$.next(num);
    StorageService.setLocalStorage('iterationNumPerProject', num);
  }

  public getIterationNumPerProject(): string {
    return this.iterationNumPerProject$.getValue();
  }
}

