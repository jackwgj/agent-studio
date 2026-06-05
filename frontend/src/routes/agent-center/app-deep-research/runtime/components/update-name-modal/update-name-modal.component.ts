import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from 'angular-i18next';
import {I18nNamespace} from '@i18n';
import {MODULES} from '@shared/modules';
import {NzModalRef} from 'ng-zorro-antd/modal';
import {NzMessageService} from 'ng-zorro-antd/message';

@Component({
  selector: 'update-node-name-modal',
  templateUrl: './update-name-modal.component.html',
  styleUrls: ['./update-name-modal.component.less'],
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class UpdateNameModalComponent implements OnInit {
  @Input() name: string;
  @Output() nameChange = new EventEmitter<string>();

  constructor(
    private cdr: ChangeDetectorRef,
    private i18n: I18NextEagerPipe,
    private modalRef: NzModalRef,
    private msg: NzMessageService,
  ) {
  }

  public confirmValue(e: Event) {
    e.stopPropagation();
    if (!this.name) {
      this.msg.error(this.i18n.transform('deep_research_task.task_5'));
      return
    }
    this.nameChange.emit(this.name);
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
