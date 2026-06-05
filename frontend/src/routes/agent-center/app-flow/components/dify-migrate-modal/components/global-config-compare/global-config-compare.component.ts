import {Component, Input} from '@angular/core';
import {CommonModule} from "@angular/common";
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from "angular-i18next";
import {NzAlertModule} from "ng-zorro-antd/alert";

import {LLMSelectComponent} from "@routes/agent-center/app-flow/components/llm-select/llm-select.component";
import {MigrateCompareRowComponent} from "@routes/agent-center/app-flow/components/dify-migrate-modal/components/migrate-compare-row/migrate-compare-row.component";
import {MODULES} from "@shared/modules";
import {I18nNamespace} from "@i18n";
import type {IWorkflowInfo} from "@routes/agent-center/app-flow/app-flow.types";

@Component({
  selector: 'global-config-compare',
  templateUrl: './global-config-compare.component.html',
  styleUrls: ['../common-compare.less', './global-config-compare.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    NzAlertModule,
    MigrateCompareRowComponent,
    LLMSelectComponent
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class GlobalConfigCompareComponent {
  @Input() workFlowDsl: IWorkflowInfo;

  public oldModelName = '';
  public showLlmSelectTip = false;
  public selected = null;

  public modelValidation = {
    errorMessage: {
      required: this.i18n.transform('select_model'),
    },
    type: 'changeAlert',
  };

  private globalConfigs;

  constructor(
    private i18n: I18NextEagerPipe,
  ) { }

  ngOnInit(): void {
    this.globalConfigs = this.workFlowDsl.workflow_details.configs;
    this.oldModelName = this.globalConfigs.default_model?.model?.model_name;
    this.showLlmSelectTip = this.globalConfigs?.additional_questions_config?.enable;
    this.updateModel(null);
  }

  public updateModel(model:any){
    const questionConfigs = this.globalConfigs?.additional_questions_config;

    if(!model) {
      if(questionConfigs) {
        questionConfigs.enable = false;
      }
      return;
    }

    if(questionConfigs?.prompt) {
      questionConfigs.enable = true;
    }

    this.globalConfigs.default_model = {
      model: model.id,
      model_deployment_id: model.id,
      model_name: model.modelInfo.model_name,
      model_type: model.modelInfo.model_type
    }

    this.globalConfigs.model = {
      model: model.id,
      model_deployment_id: model.id,
      model_name: model.modelInfo.model_name,
      model_type: model.modelInfo.model_type
    }
  }
}
