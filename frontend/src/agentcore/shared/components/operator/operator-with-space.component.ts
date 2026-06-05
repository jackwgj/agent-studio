import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, DestroyRef, inject, Input } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

export interface OperatorWithSpaceParams {
  operators: any[];
  newLine?: boolean;
  space?: string;
}

@Component({
  selector: 'operator-with-space',
  template: `<br *ngIf="compParams.newLine" />
    <div class="align-center">
      @for (item of operators; track item; let index = $index; let first = $first) {
        <div [ngClass]="{ 'ml-space': compParams.newLine ? index !== 0 : !first }" [style.margin-left]="spaceSize">
          <ng-content></ng-content>
        </div>
      }
    </div>`,
  styles: [
    `
      :host .ml-space {
        margin-left: var(--dynamic-ml, 8px) !important;
      }
      :host::ng-deep.align-center {
        display: flex;
        align-items: center;
        span {
          display: flex;
          align-items: center;
          height: 100%;
        }
      }
    `,
  ],
  standalone: true,
  imports: [CommonModule],
})
export class OperatorWithSpaceComponent {
  @Input() operators: any[] = [];
  @Input() compParams: OperatorWithSpaceParams;
  destroyRef = inject(DestroyRef);
  spaceSize;
}
