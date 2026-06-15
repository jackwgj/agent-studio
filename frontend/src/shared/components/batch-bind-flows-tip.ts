import { Component, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { ClickOutsideDirective } from '@shared/directives/click-outside.directive';
import { MODULES } from '@shared/modules';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { ActivatedRoute } from '@angular/router';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { PublishedFlowSelectBaseComponent } from '@shared/base/published-flow-select-base.service';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { FormsModule } from '@angular/forms';
import { LAZY_LOAD_LIMIT } from '@routes/agent-center/app-flow/flow.const';

@Component({
  selector: 'model-settings',
  template: `
    <div (clickOutside)="close()" class="flex w-[400px] flex-col gap-[16px]">
      <div class="text-sm font-semibold">
        {{ 'batch_redirect_conf' | i18nextEager }}
      </div>
      <div class="flex flex-col gap-[8px]">
        <span>{{ 'bind_go_to' | i18nextEager }}</span>
        <nz-select
          style="width: 100%"
          [nzOptions]="flowSelectOptions"
          [(ngModel)]="flowSelected"
          [nzPlaceHolder]="'select_placeholder' | i18nextEager"
          nzShowSearch
          nzAllowClear
          nzServerSearch
          [nzLoading]="isLoading"
          (nzOpenChange)="onOpenChange($event)"
          (nzOnSearch)="onSearch($event)"
          (nzScrollToBottom)="onLoadMore()"
        >
        </nz-select>
      </div>
      <div class="flex justify-end gap-[8px]">
        <button type="button" nz-button (click)="dismiss()">
          {{ 'cancel' | i18nextEager }}
        </button>
        <button
          type="button"
          nz-button
          nzType="primary"
          [disabled]="!flowSelected"
          (click)="onConfirm()"
        >
          {{ 'ok' | i18nextEager }}
        </button>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        padding: 8px;
      }
    `,
  ],
  imports: [MODULES, ClickOutsideDirective, NzSelectModule, FormsModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
  standalone: true,
})
export class batchBindFlowsComponent extends PublishedFlowSelectBaseComponent {
  @Input() override workflowType = '';

  public flowSelected = '';

  public isLoading = false;

  public searchWord = '';

  @Output() flowInfoSelected = new EventEmitter<string>();

  constructor(
    protected override appFlowRepoServe: AppFlowRepoService,
    protected override i18n: I18NextEagerPipe,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
  ) {
    super(appFlowRepoServe, i18n);

    this.route.queryParams.subscribe((params) => {
      this.workflowId = params.id;
    });
  }

  get flowSelectOptions() {
    return (this.getFlowList || []).map(item => ({
      label: item.name ?? '',
      value: item,
    }));
  }

  get getFlowList() {
    return (this as any).flowList as any[];
  }

  onOpenChange(isOpen: boolean) {
    if (!isOpen) return;
    this.isLoading = true;
    this.appFlowRepoServe
      .getPublishedFlows({ offset: 0, limit: LAZY_LOAD_LIMIT })
      .then(result => {
        (this as any).flowList = (result?.workflow_version_list || []).filter(
          (item) => item.workflow_id !== this.workflowId,
        );
        this.isLoading = false;
        this.cdr.detectChanges();
      })
      .catch(() => {
        (this as any).flowList = [];
        this.isLoading = false;
        this.cdr.detectChanges();
      });
  }

  onSearch(searchValue: string) {
    this.searchWord = searchValue;
    this.isLoading = true;
    this.appFlowRepoServe
      .getPublishedFlows({ offset: 0, limit: LAZY_LOAD_LIMIT, name: searchValue })
      .then(result => {
        (this as any).flowList = (result?.workflow_version_list || []).filter(
          (item) => item.workflow_id !== this.workflowId,
        );
        this.isLoading = false;
        this.cdr.detectChanges();
      })
      .catch(() => {
        (this as any).flowList = [];
        this.isLoading = false;
        this.cdr.detectChanges();
      });
  }

  onLoadMore() {
    const currentList = this.getFlowList || [];
    if (currentList.length >= (this as any).totalIntentBranches) return;
    this.isLoading = true;
    this.appFlowRepoServe
      .getPublishedFlows({
        offset: currentList.length,
        limit: LAZY_LOAD_LIMIT,
        name: this.searchWord,
      })
      .then(result => {
        (this as any).flowList = [
          ...currentList,
          ...result.workflow_version_list,
        ].filter((item) => item.workflow_id !== this.workflowId);
        this.isLoading = false;
        this.cdr.detectChanges();
      })
      .catch(() => {
        this.isLoading = false;
        this.cdr.detectChanges();
      });
  }

  public dismiss() {
    this.flowInfoSelected.emit('');
  }

  public close() {
    this.flowInfoSelected.emit('');
  }

  public onConfirm() {
    this.flowInfoSelected.emit(this.flowSelected);
  }
}
