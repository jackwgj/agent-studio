import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'header-operator-button',
  standalone: true,
  imports: [NzButtonModule, CommonModule, NzToolTipModule],
  template: `<button
    nz-button
    [disabled]="compParams.disabled ? compParams.disabled() : false"
    [nzType]="compParams.color === 'major' ? 'primary' : 'default'"
    (click)="compParams.click()"
    [nz-tooltip]="compParams.tip"
  >
    {{ compParams.text }}
  </button>`,
})
export class HeaderButtonOperator {
  @Input() compParams: {
    disabled?;
    text: string;
    color?: string;
    tip?: string;
    click: () => void;
  };
}
