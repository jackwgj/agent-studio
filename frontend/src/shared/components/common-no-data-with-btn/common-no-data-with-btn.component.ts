import { Component, Output, EventEmitter, Input } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';

@Component({
  selector: 'app-common-no-data-with-btn',
  templateUrl: './common-no-data-with-btn.component.html',
  styleUrls: ['./common-no-data-with-btn.component.less'],
  imports: [MODULES],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.MODEL_ACCESS, I18nNamespace.REVIEW],
    },
  ],
})
export class CommonNoDataWithBtnComponent {
  @Input() noDataStr = '';

  @Input() createStr = '';

  @Input() noDataBtnStr = '';

  @Output() create = new EventEmitter<void>();

  onCreate() {
    this.create.emit();
  }
}
