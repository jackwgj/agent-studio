import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";

import { NzModalModule } from "ng-zorro-antd/modal";
import { NzIconModule } from "ng-zorro-antd/icon";

import { I18nNamespace } from "@i18n";
import { MODULES } from "@shared/modules";
import type { IFlowTrigger, IWorkflowInfo } from "../../app-flow.types";
import { MigrateCompareRowComponent } from "./components/migrate-compare-row/migrate-compare-row.component";
import { LlmCompareComponent } from "./components/llm-compare/llm-compare.component";
import { MigrateNodeType } from "./dify-migrate-modal.interface";
import { NotCompatibleNodeComponent } from "./components/not-compatible-node/not-compatible-node.component";
import {
  IntentDetectionCompareComponent
} from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/intent-detection-compare/intent-detection-compare.component";
import {
  KnowLedgeRepoCompareComponent
} from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/know-ledge-repo-compare/know-ledge-repo-compare.component";
import {
  SetVariableCompareComponent
} from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/set-variable-compare/set-variable-compare.component";
import {
  AgentCompareComponent
} from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/agent-compare/agent-compare.component";
import { HttpCompareComponent } from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/http-compare/http-compare.component";
import { TriggerCompareComponent } from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/trigger-compare/trigger-compare.component";
import { CodeCompareComponent } from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/code-compare/code-compare.component";
import { GlobalConfigCompareComponent } from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/global-config-compare/global-config-compare.component";
import { DifyMigrateService } from "@routes/agent-center/app-flow/components/dify-migrate-modal/dify-migrate.service";
import { NzDrawerRef } from "ng-zorro-antd/drawer";

@Component({
  selector: "dify-migrate-modal",
  templateUrl: "./dify-migrate-modal.component.html",
  styleUrl: "./dify-migrate-modal.component.less",
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    NzModalModule,
    NzIconModule,
    MigrateCompareRowComponent,
    NotCompatibleNodeComponent,
    LlmCompareComponent,
    TriggerCompareComponent,
    IntentDetectionCompareComponent,
    KnowLedgeRepoCompareComponent,
    SetVariableCompareComponent,
    AgentCompareComponent,
    HttpCompareComponent,
    CodeCompareComponent,
    GlobalConfigCompareComponent
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.GUIDE, I18nNamespace.TOOL]
    }
  ]
})
export class DifyMigrateModalComponent {

  public parsedDSL: IWorkflowInfo;
  public workFlowDsl: IWorkflowInfo;
  public nodeType = MigrateNodeType;
  public oldDifyTrigger: IFlowTrigger;

  public migrateRes;
  public fullCompatible;
  public halfCompatible;
  public notCompatible;

  constructor(
    private i18n: I18NextEagerPipe,
    private difyMigrateService: DifyMigrateService,
    public nzDrawerRef: NzDrawerRef
  ) {
  }

  ngOnInit(): void {
    this.parsedDSL = this.difyMigrateService.parsedDSL;
    this.workFlowDsl = this.difyMigrateService.workFlowDsl;
    this.migrateRes = this.difyMigrateService.migrateRes;
    this.fullCompatible = this.difyMigrateService.fullCompatible;
    this.halfCompatible = this.difyMigrateService.halfCompatible;
    this.notCompatible = this.difyMigrateService.notCompatible;
  }

  public dismiss() {
    this.nzDrawerRef.close();
  }

  public confirm() {
    this.difyMigrateService.createWorkFlow(() => {
      this.dismiss();
    });
  }

  public changeCollapsedState(groups): void {
    groups.collapsed = !groups.collapsed;
  }
}
