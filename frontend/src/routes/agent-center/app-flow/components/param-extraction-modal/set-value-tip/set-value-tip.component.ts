import { Component } from '@angular/core';
import { SetDefaultTipComponent } from '@routes/agent-center/app-flow/components/set-default-tip/set-default-tip.component';

@Component({
  selector: 'meta-set-value-tip',
  templateUrl: './set-value-tip.component.html',
  styleUrls: ['./set-value-tip.component.less', '../../common-styles.less'],
  standalone: true,
  imports: [],
  providers: [],
})
export class SetValueTipComponent extends SetDefaultTipComponent {}
