import { Injectable } from '@angular/core';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { I18NextEagerPipe } from 'angular-i18next';
import { PublishedFlowSelectBaseComponent } from '../base/published-flow-select-base.service';
import { PublishedWorkflow } from '@routes/agent-center/app-flow/app-flow.types';

@Injectable({
  providedIn: 'root',
})
export class FlowSelectService extends PublishedFlowSelectBaseComponent {
  constructor(
    protected override appFlowRepoServe: AppFlowRepoService,
    protected override i18n: I18NextEagerPipe,
  ) {
    super(appFlowRepoServe, i18n);
  }

  /** 提供外部访问父类的 protected flowList */
  public get getFlowList(): PublishedWorkflow[] {
    return this.flowList;
  }

  public invokeOnBeforeOpenFlowList(selectComp: any): void {
    this.onBeforeOpenFlowList(selectComp);
  }

  public invokeOnBeforeSearch(selectComp: any): void {
    this.onBeforeSearch(selectComp);
  }

  public invokeLoadMore(
    scrollLoadInfo: any,
    selectComp: any,
  ): void {
    this.loadMore(scrollLoadInfo, selectComp);
  }

  /** 由于依赖注入的公共服务不会自动接收angular组件中的变量值，需要显式调用setter传值 */
  public set flowIdSetter(id: string) {
    this.workflowId = id;
  }

  /** 由于依赖注入的公共服务不会自动接收angular组件中的@Input输入属性，需要显式调用setter传值 */
  public set flowTypeSetter(value: string) {
    this.workflowType = value;
  }

  public updateFlowList(list: any[]) {
    (this as any).flowList = list;
  }
}
