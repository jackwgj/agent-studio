import { Component, Inject, Input, Optional } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import * as I18next from 'angular-i18next';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { NzModalRef } from 'ng-zorro-antd/modal';

@Component({
  selector: 'delete-channel-modal',
  templateUrl: './delete-channel-modal.component.html',
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18next.I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class DeleteChannelModalComponent {
  @Input() name: string;
  @Input() type: string = 'APP_STORE';

  public changeUrl = cdnAssetUrl;

  constructor(@Optional() private modalRef: NzModalRef) {}

  public deletePublishedChannel() {
    this.close();
  }

  public close() {}

  public dismiss() {
    this.modalRef?.close();
  }
}
