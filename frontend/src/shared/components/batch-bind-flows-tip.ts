import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ClickOutsideDirective } from '@shared/directives/click-outside.directive';
import { MODULES } from '@shared/modules';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { ActivatedRoute } from '@angular/router';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { PublishedFlowSelectBaseComponent } from '@shared/base/published-flow-select-base.service';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { FormsModule } from '@angular/forms';

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
          #select
          style="width: 100%"
          [nzOptions]="flowList"
          [(ngModel)]="flowSelected"
          [nzPlaceHolder]="'select_placeholder' | i18nextEager"
          nzShowSearch
          nzAllowClear
          [nzServerSearch]="true"
          (nzOnSearch)="onBeforeSearch($event)"
          (nzScrollToBottom)="loadMore($event, select)"
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
          nzDanger
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

  @Output() flowInfoSelected = new EventEmitter<string>();

  constructor(
    protected override appFlowRepoServe: AppFlowRepoService,
    protected override i18n: I18NextEagerPipe,
    private route: ActivatedRoute,
  ) {
    super(appFlowRepoServe, i18n);

    this.route.queryParams.subscribe((params) => {
      this.workflowId = params.id;
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
