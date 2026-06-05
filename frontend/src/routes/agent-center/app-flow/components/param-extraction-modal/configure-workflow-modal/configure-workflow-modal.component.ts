import { Component } from '@angular/core';
import { ModalBaseComponent } from '@routes/agent-center/app-flow/components/base/modal-base.component';

@Component({
  selector: 'configure-workflow-modal',
  templateUrl: 'configure-workflow-modal.component.html',
  styleUrls: [
    'configure-workflow-modal.component.less',
    '../../common-styles.less',
  ],
  standalone: true,
  imports: [],
  providers: [],
})
export class ConfigureWorkflowModalComponent extends ModalBaseComponent {}
