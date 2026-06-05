import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { LineClampDirective } from '@shared/directives/line-clamp.directive';
import {
  I18NEXT_NAMESPACE,
  I18NextEagerPipe,
  I18NextModule,
} from 'angular-i18next';
import { isNil } from 'lodash';
import { IRefContentType, IWorkflowField } from '../../node.type';
import { ValueWarnDirective } from '@shared/directives/value-warn.directive';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'meta-input-params',
  template: `
    <div class="flex flex-col gap-2" *ngIf="inputs?.length">
      <div *ngFor="let param of inputs" class="flex items-center">
        <div class="flex w-[calc(100%-120px)] items-center gap-[30px]">
          <div class="node-param-shallow w-[54px]" lineClamp valueWarn [valueWarn]="{ textContent: param.name, isParamName: nameValid, noRequired: !param.required}">
            {{ getFieldName(param) }}
          </div>
          <div class="node-param-tag" lineClamp>
            {{ getType(param) }}
          </div>
        </div>
        <div class="node-param-shallow w-[120px]" nz-tooltip [nzTooltipTitle]="$any(getFieldContentValue(param))" lineClamp  valueWarn [valueWarn]="{ textContent: param?.value?.content, refWithout: param.refWithout, noRequired: !param.required, isInteger: param?.type === 'integer' && param.value.type !=='ref' }">
          {{ getFieldContentValue(param) }}
        </div>
      </div>
    </div>

    <div *ngIf="!inputs?.length" class="text-desc">
      {{ 'not_configured' | i18nextEager }}
    </div>
  `,
  styleUrls: ['../common-styles.less'],
  imports: [CommonModule, I18NextModule, LineClampDirective, ValueWarnDirective, NzToolTipModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
  standalone: true,
})
export class InputParamsComponent {
  @Input() inputs: IWorkflowField[];
  // 名字需要校验，插件的请求头参数不需要校验
  @Input() nameValid = true;

  constructor(private i18n: I18NextEagerPipe) {}

  getFieldName(param: IWorkflowField) {
    return param.name || this.i18n.transform('not_configured');
  }

  getType(param: IWorkflowField) {
    return param.value?.type ? this.i18n.transform(param.value.type) : param.type;
  }

  getFieldContentValue(param: IWorkflowField) {
    if (param.value?.type === 'ref') {
      return (
        (param.value.content as IRefContentType).ref_var_name ||
        this.i18n.transform('not_selected')
      );
    }

    if (isNil(param?.value?.content) || param.value?.content === '') {
      return this.i18n.transform('not_configured');
    }

    return param.value?.content;
  }
}
