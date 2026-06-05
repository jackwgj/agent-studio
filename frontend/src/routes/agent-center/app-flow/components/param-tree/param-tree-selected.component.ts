import { Component, Input } from '@angular/core';
import { MODULES } from '@shared/modules';
import { ParamLabelPipe } from 'src/pipes/param-label.pipe';
import type { IParamRef } from '../../node.type';

@Component({
  selector: 'meta-param-tree-selected',
  template: `<div
    class="flex h-full w-full items-center gap-[8px] px-[8px] leading-6"
  >
    <div tiOverflow class="text-[12px] text-[#191919]">
      {{ item.name }}
    </div>
    <div
      tiOverflow
      class="rounded-[2px] bg-[#f5f5f5] px-[4px] text-[12px] leading-5 text-[#191919]"
    >
      {{ item.type | paramLabel }}
    </div>
  </div>`,
  styles: [''],
  standalone: true,
  imports: [MODULES, ParamLabelPipe],
})
export class ParamTreeSelectedComponent {
  @Input() item: IParamRef;
}
