import { Component, Input } from '@angular/core';
import { MODULES } from '@shared/modules';
import { NzPopoverModule } from 'ng-zorro-antd/popover';

@Component({
  selector: 'asset-format-icon',
  templateUrl: './asset-format-icon.component.html',
  imports: [MODULES, NzPopoverModule],
  standalone: true,
})
export class AssetFormatIconComponent {
  @Input() tiTip: string;
  @Input() tiTipPosition: 'top' | 'bottom' | 'left' | 'right' = 'top';
}
