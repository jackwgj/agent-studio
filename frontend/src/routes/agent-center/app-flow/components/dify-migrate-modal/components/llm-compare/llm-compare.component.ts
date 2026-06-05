import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';

import { NzAlertModule } from 'ng-zorro-antd/alert';

import { MODULES } from '@shared/modules';
import { MigrateCompareRowComponent } from '../migrate-compare-row/migrate-compare-row.component';
import { I18nNamespace } from '@i18n';
import type { ILLMNode } from '@routes/agent-center/app-flow/node.type';
import { LLMSelectComponent } from '../../../llm-select/llm-select.component';

@Component({
  selector: 'llm-compare',
  templateUrl: './llm-compare.component.html',
  styleUrls: ['../common-compare.less', './llm-compare.component.less'],
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
export class LlmCompareComponent {
  @Input() node: ILLMNode;

  public oldModelName: string;
  public showVisionModelTip = false;
  public selected;

  public modelValidation = {
    errorMessage: {
      required: this.i18n.transform('select_model'),
    },
    type: 'changeAlert',
  };

  constructor(
    private i18n: I18NextEagerPipe,
  ) { }

  ngOnInit(): void {
    this.oldModelName = this.node.configs.model.model_name;
    this.showVisionModelTip = Boolean(this.node.configs.vision);
  }

  public updateModel(model:any){
    this.node.configs.model = {
      model_id: model.id,
      model_deployment_id: model.id,
      model_name: model.modelInfo.model_name,
      model_type: model.modelInfo.model_type
    }
  }
}
