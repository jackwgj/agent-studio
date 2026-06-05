import {Component, Input} from '@angular/core';
import {CommonModule} from "@angular/common";
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from "angular-i18next";
import {NzIconModule} from "ng-zorro-antd/icon";
import {NzTooltipModule} from "ng-zorro-antd/tooltip";
import {NzModalModule, NzModalRef, NzModalService} from "ng-zorro-antd/modal";

import {MODULES} from "@shared/modules";
import {MigrateCompareRowComponent} from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/migrate-compare-row/migrate-compare-row.component";
import {I18nNamespace} from "@i18n";
import type {IFlowTrigger, IWorkflowInfo} from "@routes/agent-center/app-flow/app-flow.types";
import {
  TriggerHalfmodalComponent
} from "@routes/agent-center/app-agent/components/trigger-modal/trigger-modal.component";
import {AssetDetailsIconComponent} from "@shared/components/assets/asset-details-icon/asset-details-icon.component";
import {getDayOfWeekLabel} from "@routes/agent-center/app-agent/common-logic-agent";
import {ToolListService} from "@routes/agent-center/app-agent/components/tool-list/tool-list.service";

@Component({
  selector: 'trigger-compare',
  templateUrl: './trigger-compare.component.html',
  styleUrls: ['../common-compare.less', './trigger-compare.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    NzIconModule,
    NzTooltipModule,
    NzModalModule,
    MigrateCompareRowComponent,
    AssetDetailsIconComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class TriggerCompareComponent {
  @Input() oldTrigger: IFlowTrigger;
  @Input() workFlowDsl: IWorkflowInfo;

  public curTrigger: IFlowTrigger;

  private frequencyMap = {
    'hourly': this.i18n.transform('trigger_hourly'),
    'daily': this.i18n.transform('trigger_hourly'),
    'weekly': this.i18n.transform('trigger_weekly'),
    'monthly': this.i18n.transform('trigger_monthly'),
  }

  constructor(
    private i18n: I18NextEagerPipe,
    private nzModal: NzModalService,
    private toolListService: ToolListService,
  ) { }

  ngOnInit(): void {
  }

  public getFrequencyText(frequency: string): string {
    return this.frequencyMap[frequency] || frequency;
  }

  public getTipText(item: IFlowTrigger) {
    const { cron, hook_url, prompt, invocation } = item || {};
    if (item.type === 'TIMER') {
      return {
        cron: this.toolListService.convertFromCronExpression(cron),
        prompt,
        invocation,
      };
    } else {
      return { hook_url, prompt };
    }
  }

  public getTriggerType(type: string): string {
    if (type === 'EVENT') {
      return this.i18n.transform('toollistcomponent_66');
    } else {
      return this.i18n.transform('toollistcomponent_67');
    }
  }

  public onCreateTrigger() {
    const modal: NzModalRef = this.nzModal.create({
      nzContent: TriggerHalfmodalComponent,
      nzWidth: '800px',
      nzFooter: null,
      nzMaskClosable: false,
      nzClosable: true,
    });
    Object.assign(modal.componentInstance, {
      showCallMethod: true,
    });
    modal.componentInstance.onCreate = (trigger: IFlowTrigger) => {
      this.curTrigger = trigger;
      this.updateTrigger(trigger);
      modal.close();
    };
  }

  public editOneTrigger() {
    const modal: NzModalRef = this.nzModal.create({
      nzContent: TriggerHalfmodalComponent,
      nzWidth: '800px',
      nzFooter: null,
      nzMaskClosable: false,
      nzClosable: true,
    });
    Object.assign(modal.componentInstance, {
      edit_data: [{...this.curTrigger}],
      showCallMethod: true,
    });
    modal.componentInstance.onUpdate = (trigger: IFlowTrigger) => {
      this.curTrigger = trigger;
      this.updateTrigger(trigger);
      modal.close();
    };
  }

  public deleteOneTrigger() {
    this.curTrigger = null;
    this.workFlowDsl.workflow_details.configs.trigger = null;
  }

  private updateTrigger(trigger: IFlowTrigger){
    this.curTrigger = trigger;
    this.workFlowDsl.workflow_details.configs.trigger = trigger;
  }
}
