import {
  ChangeDetectionStrategy,
  Component,
} from '@angular/core';

import { COMMON_MODULES } from '@shared/modules';
import { VariableInsertComponent } from '@routes/prompt/prompt-editor/variable-insert/variable-insert.component';
import { VariableComponent } from '@routes/prompt/prompt-editor/variable/variable.component';

@Component({
  selector: 'meta-param-variable',
  standalone: true,
  imports: [COMMON_MODULES, VariableInsertComponent],
  template: `
    <div class="variable-box" contenteditable="false">
      <span>{{ "{" }}</span>

      <span #variableContainer
            (blur)="onBlur()"
            (click)="onClick()"
            (input)="onInput($event)"
            (keydown.enter)="onEnterKeyPress($event)"
            [attr.contenteditable]="canEdit"
            [class]="{
        'cursor-pointer': !canEdit,
        'cursor-text':canEdit
         }"
            class="variable-input">
    {{ variable }}
  </span>
      <span>{{ "}" }}</span>
      <span class="cursor-pointer" *ngIf="!onlyShow&&promptType==='multi'">
    <ng-container *ngIf="!isChangeType;else unfoldTemplate">
      <img
        (click)="showTypeChange()"
        [src]="changeUrl('assets/agent-center/flow/fold.svg')"
        alt="" class="h-4 w-4">
    </ng-container>
    <ng-template #unfoldTemplate>
      <img
        (click)="hideTypeChange()"
        [src]="changeUrl('assets/agent-center/flow/unfold.svg')"
        alt="" class="h-4 w-4">
    </ng-template>
  </span>
    </div>
  `,
  styles: `
    ::ng-deep.wrapper {
      height: 0
    }

    ::ng-deep.variable-box {
      vertical-align: top;
      margin: 0 4px;
      height: 18px;
      display: inline-flex;
      flex-direction: row;
      justify-content: center;
      align-items: center;
      gap: 4px;
      padding: 0px 4px 0px 4px;
      border-radius: 4px;
      background: rgba(228, 247, 233, 1);
      color: rgba(2, 153, 49, 1);
      font-family: PingFang SC;
      font-size: 12px;
      font-weight: 400;
      line-height: 18px;
      letter-spacing: 0px;
      text-align: justify;

      .variable-input {
        outline: none;
        height: 100%;
        min-width: 5px;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ParamVariableComponent extends VariableComponent {

}
