import { Component, Input, inject } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { cdnAssetUrl } from '../../../single-spa/assets-url';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { NzModalRef } from 'ng-zorro-antd/modal';
@Component({
  selector: 'publish-version-submit-success-modal',
  templateUrl: './version-submit-component.html',
  styleUrls: [],
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT, I18nNamespace.COMMON],
    },
  ],
})
export class PublishVersionSubmitSuccessComponent {
  @Input() confirmFn = null;
  readonly modalRef = inject(NzModalRef);
  constructor(private configServ: AgentConfigService) {}
  public successIcon = cdnAssetUrl('assets/agent-center/images/version-submit-success.svg');

  get site() {
    return this.configServ.getConfigs().site;
  }

  public share() {
    this.confirmFn();
    this.modalRef.destroy();
  }

  public close() {
    this.modalRef.destroy();
  }

  public dismiss() {
    this.modalRef.destroy();
  }
}
