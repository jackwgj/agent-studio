import { Component } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import * as I18next from 'angular-i18next';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

@Component({
  selector: 'delete-version-modal',
  templateUrl: './delete-version-modal.component.html',
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18next.I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT],
    },
  ],
})
export class DeleteVersionModalComponent {
  public changeUrl = cdnAssetUrl;

  public deleteVersion() {
    this.close();
  }

  public close() {}

  public dismiss() {}
}
