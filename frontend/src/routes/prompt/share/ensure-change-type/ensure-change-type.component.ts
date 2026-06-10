import { ChangeDetectionStrategy, Component, EventEmitter, Output } from '@angular/core';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'meta-ensure-change-type',
  standalone: true,
  imports: [NzButtonModule, MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.PROMPT_PLATFORM
      ],
    },
  ],
  templateUrl: './ensure-change-type.component.html',
  styleUrl: './ensure-change-type.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnsureChangeTypeComponent {
  protected readonly changeUrl = cdnAssetUrl;
  @Output() readonly ensure: EventEmitter<any> = new EventEmitter();
  close(): void {
  }
  dismiss(): void {
  }
  confirm(): void {
    this.ensure.emit();
    this.close();
  }
}
