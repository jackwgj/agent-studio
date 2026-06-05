import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { LineClampDirective } from '@shared/directives/line-clamp.directive';
import { I18NEXT_NAMESPACE, I18NextEagerPipe, I18NextModule } from 'angular-i18next';
import { IWorkflowField } from '../../node.type';
import { NodeUtils } from '../utils';
import { ValueWarnDirective } from '@shared/directives/value-warn.directive';

@Component({
  selector: 'meta-output-params',
  template: `
    <div class="flex flex-col gap-2" *ngIf="outputs?.length">
      <div *ngFor="let param of outputs" class="flex items-center">
        <div class="flex w-[calc(100%-120px)] items-center gap-[30px]">
          <div class="node-param-shallow w-[54px]" lineClamp valueWarn [valueWarn]="{ textContent: param.name, isParamName: true, noRequired: !param.required}">
            {{ getFieldName(param) }}
          </div>
          <div class="node-param-tag" lineClamp>
            {{ getTypeView(param) }}
          </div>
        </div>
        <div class="node-param-shallow w-[120px]" lineClamp [valueWarn]="{ textContent: param.description, noRequired: true}">
          {{ getFieldDesc(param) }}
        </div>
      </div>
    </div>

    <div *ngIf="!outputs?.length" class="text-desc">
      {{ 'not_configured' | i18nextEager }}
    </div>
  `,
  styleUrls: ['../common-styles.less'],
  standalone: true,
  imports: [CommonModule, I18NextModule, LineClampDirective, ValueWarnDirective],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class OutputParamsComponent {
  @Input() outputs: IWorkflowField[];

  constructor(private i18n: I18NextEagerPipe) {}

  getFieldName(param: IWorkflowField) {
    return param.name || this.i18n.transform('not_configured');
  }

  getTypeView = NodeUtils.getFieldTypeView;

  getFieldDesc(param: IWorkflowField) {
    return param.description || this.i18n.transform('not_configured');
  }
}
