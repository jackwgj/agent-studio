import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, DestroyRef, inject, Inject, Input } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzIconModule } from 'ng-zorro-antd/icon';

@Component({
  selector: 'net-basic-render',
  standalone: true,
  template: `
    @if (param) {
      <span [class]="[param.className]" [nz-tooltip]="param.tip">
        {{ param.text }}
        @if (param.showCopy) {
          <span class="copy-icon" (click)="onCopy(param.copyContent)">
            <nz-icon nzType="copy" nzTheme="outline"></nz-icon>
          </span>
        }
      </span>
    }
  `,
  imports: [NzToolTipModule, CommonModule, NzIconModule],
  styles: [`
    .copy-icon {
      cursor: pointer;
      margin-left: 4px;
      &:hover {
        color: #1890ff;
      }
    }
  `],
})
export class CopyTextRenderComponent {
  destroyRef = inject(DestroyRef);
  param;

  @Input()
  set context(value: any) {
    this.param = value;
    if (this.param) {
      this.param.showCopy ??= true;
    }
  }

  constructor(private cd: ChangeDetectorRef) {}

  onCopy(content: string): void {
    navigator.clipboard.writeText(content);
  }
}
