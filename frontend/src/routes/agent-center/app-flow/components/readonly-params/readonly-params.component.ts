import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MODULES } from '@shared/modules';
import { type IWorkflowField } from '../../node.type';
import { NodeUtils } from '../utils';

@Component({
  selector: 'readonly-params',
  template: `
    <div class="mt-2 flex flex-col gap-[8px]" *ngFor="let param of params">
      <div class="flex items-center gap-[16px]">
        <div class="flex items-center gap-[8px]">
          <ng-container
            *ngIf="['array', 'object'].includes(param.type) && !isEmpty(param)"
          >
            <div (click)="onSwitchFold()">
              <div *ngIf="!isParamFold" class="readonly-params-unfold"></div>
              <div *ngIf="isParamFold" class="readonly-params-fold"></div>
            </div>
          </ng-container>
          <div class="text-[#595959]">{{ param.name }}</div>
        </div>

        <div class="rounded-[2px] bg-[#f5f5f5] px-[8px] text-[#191919]">
          {{ getType(param) }}
        </div>
      </div>

      <ng-container
        *ngIf="isObjectParam(param) && !isParamFold && !isEmpty(param)"
      >
        <div
          class="flex items-center gap-[16px] pl-[40px]"
          *ngFor="let subParam of getSubParams(param)"
        >
          <div class="text-[#595959]">{{ subParam.name }}</div>
          <div class="rounded-[2px] bg-[#f5f5f5] px-[8px] text-[#191919]">
            {{ getType(subParam) }}
          </div>
        </div>
      </ng-container>
    </div>
  `,
  styles: [
    `
      .readonly-params-unfold {
        cursor: pointer;
        width: 16px;
        height: 16px;
        background-image: url('src/assets/agent-center/flow/down.svg');
      }

      .readonly-params-fold {
        cursor: pointer;
        width: 16px;
        height: 16px;
        background-image: url('src/assets/agent-center/flow/up.svg');
      }
    `,
  ],
  standalone: true,
  imports: [MODULES],
})
export class ReadonlyParamsComponent {
  @Input() params: IWorkflowField[];

  @Output() switchFold = new EventEmitter<void>();

  public isParamFold = false;

  public getType = NodeUtils.getFieldTypeView;

  public isObjectParam(param: IWorkflowField) {
    return (
      (param.type === 'array' &&
        (param.schema as IWorkflowField).type === 'object') ||
      param.type === 'object'
    );
  }

  public getSubParams(param: IWorkflowField) {
    if (param.type === 'array') {
      return (param?.schema as IWorkflowField)?.schema as IWorkflowField[];
    }

    return param?.schema as IWorkflowField[];
  }

  public onSwitchFold() {
    this.isParamFold = !this.isParamFold;
    this.switchFold.emit();
  }

  /** 判断对象或数组是否为空 */
  public isEmpty(param: IWorkflowField) {
    if (param.type === 'object' || param.type === 'array') {
      const subParams = this.getSubParams(param);
      return !subParams || subParams.length === 0;
    }
    return false;
  }
}
