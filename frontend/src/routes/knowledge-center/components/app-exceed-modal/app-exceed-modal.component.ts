import { Component, ViewChild, TemplateRef } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { Router } from '@angular/router';
import { SubscriptionService } from '@services/subscription.service';
import { CommonUtils } from 'src/utils/common.util';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { getLocalStorage } from "../../../../utils/utils";

@Component({
  selector: 'app-exceed-modal',
  templateUrl: './app-exceed-modal.component.html',
  styleUrls: ['./app-exceed-modal.component.less'],
  standalone: true,
  imports: [MODULES, NzModalModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.PLATFORM_MANAGEMENT,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.PROMPT_PLATFORM,
      ],
    },
  ],
})
export class AppExceedModalComponent {
  @ViewChild('appExceedModal') appExceedModal: TemplateRef<any>;

  protected readonly changeUrl = cdnAssetUrl;

  public isPayed: boolean = false;

  public isHomePage: boolean = true;

  public exceedTip: string = '';

  isDevelopSpace = CommonUtils.judeg_develop_space();

  modalRef: any;

  constructor(
    private router: Router,
    private nzModal: NzModalService,
    private i18n: I18NextEagerPipe,
    private subscriptionService: SubscriptionService,
  ) {}

  async show(params) {

  }

  goToAppManage() {
    this.router.navigate(['/home/agent-center/single']);
  }

  upgradeToBusinessEdition() {
  }
}
