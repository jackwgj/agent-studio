import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';

@Component({
  selector: 'no-data-with-btn',
  templateUrl: './no-data-with-btn.component.html',
  styleUrls: ['./no-data-with-btn.component.less'],
  imports: [MODULES],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class NoDataWithBtnComponent implements OnInit {
  @Input() type = ''; // 创建空状态的类型

  @Output() create = new EventEmitter<void>(); // 创建按钮事件

  public noData = '';

  public btnLable = '';

  constructor(private i18n: I18NextEagerPipe) {}

  onCreate() {
    this.create.emit();
  }

  ngOnInit(): void {
    this.btnLable = this.i18n.transform(`create_${this.type}`);
    this.noData = this.i18n
      .transform('create_null_hint')
      .replace('{}', this.i18n.transform(this.type));
  }
}
