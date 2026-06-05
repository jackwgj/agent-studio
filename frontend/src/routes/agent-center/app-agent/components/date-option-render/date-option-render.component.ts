import { Component, Input } from '@angular/core';
import { ALL } from '@routes/agent-center/app-flow/utils/log-utils';
import { MODULES } from '@shared/modules';

@Component({
  selector: 'date-option-render',
  templateUrl: './date-option-render.component.html',
  styleUrls: ['./date-option-render.component.scss'],
  standalone: true,
  imports: [MODULES],
  providers: [],
})
export class DateOptionRender {
  public ALL_ITEM = ALL;
  @Input() selectDate: string;
  @Input() optionData: any = {};
}
