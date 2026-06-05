import { Component } from '@angular/core';
import { ModalBaseComponent } from '../base/modal-base.component';

@Component({
  selector: 'meta-param-extraction-modal',
  templateUrl: './param-extraction-modal.component.html',
  styleUrls: [
    './param-extraction-modal.component.less',
    '.././common-styles.less',
  ],
  standalone: true,
  imports: [],
  providers: [],
})
export class ParamExtractionModalComponent extends ModalBaseComponent {}
