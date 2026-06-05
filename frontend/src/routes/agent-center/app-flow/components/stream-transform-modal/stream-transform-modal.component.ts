import { Component } from '@angular/core';
import { ModalBaseComponent } from '../base/modal-base.component';

@Component({
  selector: 'stream-transform-modal',
  templateUrl: './stream-transform-modal.component.html',
  styleUrls: ['./stream-transform-modal.component.less', '../common-styles.less'],
  standalone: true,
  imports: [],
  providers: [],
})
export class StreamTransformModalComponent extends ModalBaseComponent {}
