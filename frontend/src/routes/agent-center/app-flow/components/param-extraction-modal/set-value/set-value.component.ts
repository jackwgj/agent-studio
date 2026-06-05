import { Component } from '@angular/core';
import { SetDefaultComponent } from '@routes/agent-center/app-flow/components/set-default/set-default.component';

@Component({
  selector: 'meta-set-value',
  templateUrl: './set-value.component.html',
  styleUrls: ['./set-value.component.less'],
  standalone: true,
  imports: [],
  providers: [],
})
export class SetValueComponent extends SetDefaultComponent {}
