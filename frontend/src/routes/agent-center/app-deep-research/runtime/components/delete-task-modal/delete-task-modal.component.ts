import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { FormBuilder, FormControl } from '@angular/forms';
import { NzModalRef } from 'ng-zorro-antd/modal';


@Component({
  selector: 'update-node-name-modal',
  templateUrl: './delete-task-modal.component.html',
  styleUrls: ['./delete-task-modal.component.less'],
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class DeleteTaskModalComponent implements OnInit {
  @Input() name: string;


  @Output() deleteChat = new EventEmitter<string>();

  constructor(
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private i18n: I18NextEagerPipe,
    private modalRef: NzModalRef,
  ) {}

  public confirmValue(e: Event) {
    e.stopPropagation();
    this.deleteChat.emit();
    this.close();
  }

  close(): void {
    this.modalRef.close();
  }

  dismiss(): void {
    this.modalRef.close();
  }

  ngOnInit() {

  }
}
