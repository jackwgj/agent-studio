import { Component } from '@angular/core';
import { ModalBaseComponent } from '@routes/agent-center/app-flow/components/base/modal-base.component';

@Component({
  selector: 'condition-modal',
  templateUrl: './condition-modal.component.html',
  styleUrls: ['./condition-modal.component.less', '../../common-styles.less'],
  standalone: true,
  imports: [],
  providers: [],
})
export class ConditionModalComponent extends ModalBaseComponent {}
