import { Injectable, Input } from '@angular/core';
import { I18NextEagerPipe } from 'angular-i18next';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { LAZY_LOAD_LIMIT } from '@routes/agent-center/app-flow/flow.const';

@Injectable()
export abstract class PublishedFlowSelectBaseComponent {
  @Input() workflowType = '';

  protected flowList: any[] = [];

  protected workflowId = '';

  protected totalIntentBranches: number = 0;

  constructor(
    protected appFlowRepoServe: AppFlowRepoService,
    protected i18n: I18NextEagerPipe,
  ) {}

  protected onBeforeOpenFlowList(selectComp: any) {
    selectComp.setLoading(true);

    this.appFlowRepoServe
      .getPublishedFlows({
        offset: 0,
        limit: LAZY_LOAD_LIMIT,
      })
      .then((result) => {
        this.flowList =
          result?.workflow_version_list.filter(
            (item) => item.workflow_id !== this.workflowId,
          ) ?? [];
        if (this.workflowType === 'task') {
          this.flowList = this.disableTaskFlowOptions(this.flowList);
        }
        this.totalIntentBranches = result.count;
        selectComp.setLoading(false);
        selectComp.open();
      })
      .catch(() => {
        this.flowList = [];
        this.totalIntentBranches = 0;
        selectComp.setLoading(false);
        selectComp.open();
      });
  }

  /** 后台搜索 */
  protected onBeforeSearch(selectComp: any) {
    const name: string = selectComp.getSearchWord();
    selectComp.setLoading(true);

    this.appFlowRepoServe
      .getPublishedFlows({
        offset: 0,
        limit: LAZY_LOAD_LIMIT,
        name,
      })
      .then((result) => {
        selectComp.setLoading(false);
        this.flowList = result.workflow_version_list.filter(
          (item) => item.workflow_id !== this.workflowId,
        );
        if (this.workflowType === 'task') {
          this.flowList = this.disableTaskFlowOptions(this.flowList);
        }
        this.totalIntentBranches = result.count;
      });
  }

  /** 懒加载已发布工作流 */
  protected loadMore(
    scrollLoadInfo: any,
    selectComp: any,
  ) {
    const currentOptions = selectComp.getSearchResult();
    if (currentOptions.length >= this.totalIntentBranches) {
      return;
    }

    const offset = this.flowList.length;
    const name: string = selectComp.getSearchWord();
    scrollLoadInfo.loading = true;

    this.appFlowRepoServe
      .getPublishedFlows({
        offset,
        limit: LAZY_LOAD_LIMIT,
        name,
      })
      .then((result) => {
        this.flowList = [
          ...currentOptions,
          ...result.workflow_version_list,
        ].filter((item) => item.workflow_id !== this.workflowId);
        if (this.workflowType === 'task') {
          this.flowList = this.disableTaskFlowOptions(this.flowList);
        }
        this.totalIntentBranches = result.count;
        scrollLoadInfo.loading = false;
      });
  }

  protected disableTaskFlowOptions(flowList: any[]) {
    return flowList.map((item) => {
      if (item.type === 'chat') {
        return {
          ...item,
          disabled: true,
          tip: this.i18n.transform('task_wf_not_support_add_conversational_wf'),
        };
      }
      return item;
    });
  }
}
