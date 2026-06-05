import { OverlayModule } from '@angular/cdk/overlay';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import * as I18next from 'angular-i18next';

interface IArrowPos {
  bottom: string;
  right?: string;
  left?: string;
}

type ArrowDir = 'bottomRight' | 'left';

const ARROW_POS = new Map<ArrowDir, IArrowPos>([
  [
    'bottomRight',
    {
      bottom: '-12px',
      right: '16px',
    },
  ],
  [
    'left',
    {
      bottom: '12px',
      left: '-6px',
    },
  ],
]);

@Component({
  selector: 'pe-popconfirm',
  templateUrl: './pe-popconfirm.component.html',
  standalone: true,
  styleUrls: ['./pe-popconfirm.component.less'],
  imports: [MODULES, OverlayModule],
  providers: [
    {
      provide: I18next.I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PROMPT_PLATFORM],
    },
  ],
})
export class PePopconfirmComponent {
  @Input() content: string;

  @Input() icon: string;

  @Input() isShowButtonGroup: boolean;

  @Input() arrowPos: 'bottomRight' | 'left';

  @Output() confirm = new EventEmitter<void>();

  @Output() cancel = new EventEmitter<void>();

  public onConfirm(): void {
    this.confirm.emit();
  }

  public onCancel(): void {
    this.cancel.emit();
  }

  public getArrowPos(): IArrowPos {
    return ARROW_POS.get(this.arrowPos);
  }
}
