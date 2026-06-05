import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { IRefInfo } from './app-flow.types';
import { type IExeField } from './components/node-exe/node-exe.component';

@Injectable({
  providedIn: 'root',
})
export class NodeService {
  private readonly currSelectedNode$ = new BehaviorSubject<string>('');

  private readonly isReadonlyFlow$ = new BehaviorSubject<boolean>(false);

  private readonly isAllExpand$ = new BehaviorSubject<boolean>(true);

  private readonly isConfirmLoading$ = new BehaviorSubject<boolean>(false);

  private readonly refInfo$ = new BehaviorSubject<IRefInfo[]>([]);

  private readonly parentRefInfo$ = new BehaviorSubject<IRefInfo[]>([]);

  private readonly batchUpdateRefInfoTrigger$ = new Subject<void>();

  private readonly closeAllSetDefaultTip$ = new Subject<void>();

  private readonly isolatedTestNodesCtx$ = new BehaviorSubject<
    Record<string, Record<string, IExeField>>
  >({});

  private readonly updateView$ = new Subject<void>();

  public setCloseAllSetDefaultTip() {
    this.closeAllSetDefaultTip$.next();
  }

  public closeAllSetDefaultTipUpdate() {
    return this.closeAllSetDefaultTip$.asObservable();
  }

  public setUpdateView() {
    this.updateView$.next();
  }

  public updateViewUpdate$() {
    return this.updateView$.asObservable();
  }

  public getIsolatedTestNodeCtx(
    id: string,
  ): Record<string, IExeField> | undefined {
    return this.isolatedTestNodesCtx$.getValue()[id];
  }

  public setIsolatedTestNodeCtx(
    id: string,
    fieldsMap: Record<string, IExeField>,
  ) {
    const val = this.isolatedTestNodesCtx$.getValue();
    val[id] = fieldsMap;
    this.isolatedTestNodesCtx$.next(val);
  }

  public cleanIsolatedTestNodesCtx() {
    this.isolatedTestNodesCtx$.next({});
  }

  public setRefInfo(info: IRefInfo[]) {
    this.refInfo$.next(info);
  }

  public refInfoUpdate$() {
    return this.refInfo$.asObservable();
  }

  public setParentRefInfo(info: IRefInfo[]) {
    this.parentRefInfo$.next(info);
  }

  public fireBatchSetRefInfo() {
    this.batchUpdateRefInfoTrigger$.next();
  }

  public batchUpdateRefInfo() {
    return this.batchUpdateRefInfoTrigger$.asObservable();
  }

  public parentRefInfoUpdate$() {
    return this.parentRefInfo$.asObservable();
  }

  public setCurrSelectedNode(nodeId: string) {
    this.currSelectedNode$.next(nodeId);
  }

  public currSelectedNodeUpdate$() {
    return this.currSelectedNode$.asObservable();
  }

  public getCurrSelectedNodeId() {
    return this.currSelectedNode$.getValue();
  }

  public setIsReadonlyFlow(isReadonlyFlow: boolean) {
    this.isReadonlyFlow$.next(isReadonlyFlow);
  }

  public isReadonlyFlowUpdate$() {
    return this.isReadonlyFlow$.asObservable();
  }

  public setIsAllExpand(isAllExpand: boolean) {
    this.isAllExpand$.next(isAllExpand);
  }

  public isAllExpandUpdate$() {
    return this.isAllExpand$.asObservable();
  }

  public setIsConfirmLoading(isLoading: boolean) {
    this.isConfirmLoading$.next(isLoading);
  }

  public isConfirmLoadingUpdate$() {
    return this.isConfirmLoading$.asObservable();
  }
}
