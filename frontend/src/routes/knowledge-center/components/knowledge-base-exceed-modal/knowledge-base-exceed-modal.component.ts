import { Component, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { HelpDocKey } from '@constants/support-topic.const';
import { I18nNamespace } from '@i18n';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { MODULES } from '@shared/modules';
import { HelpLinksService } from '@shared/services/help-links.service';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { CommonUtils } from 'src/utils/common.util';
import { getLocalStorage } from "../../../../utils/utils";

@Component({
  selector: 'knowledge-base-exceed-modal',
  templateUrl: './knowledge-base-exceed-modal.component.html',
  styleUrls: ['./knowledge-base-exceed-modal.component.less'],
  standalone: true,
  imports: [MODULES, NzModalModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.PLATFORM_MANAGEMENT,
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.PROMPT_PLATFORM,
      ],
    },
  ],
})
export class KnowledgeBaseExceedModalComponent {
  @ViewChild('exceedModal') exceedModal: TemplateRef<any>;

  protected readonly changeUrl = cdnAssetUrl;
  public exceedTip: string = '';
  public isPayed: boolean = false;
  public type: string = 'default';
  purchaseUrl: string = '';

  constructor(
    private router: Router,
    private nzModalService: NzModalService,
    private i18n: I18NextEagerPipe,
    private helpLinksService: HelpLinksService,
  ) {}

  ngAfterViewInit(): void {
    this.purchaseUrl = this.helpLinksService.getHelpCenterLink(HelpDocKey.PURCHASE_GUIDE);
  }

  show(params): void {
    const { type, totalAmount } = params;

    const isEnterprise =
      getLocalStorage('isEnterprise') || false;

    const isDefaultKnowBase = type === 'default';
    this.isPayed = isEnterprise;
    this.type = type;

    const typeList = {
      default: 'exceed_tip_content_1',
      third: 'third_exceed_tip_content',
      external: 'ext_exceed_tip_content',
    };
    this.exceedTip = this.i18n.transform(typeList[type], {
      num: totalAmount,
    });
    this.nzModalService.create({
      nzContent: this.exceedModal,
      nzClassName: isDefaultKnowBase
        ? 'know-default-exceed-class'
        : 'know-exceed-class',
    });
  }

  goToAppManage() {
    this.router.navigate(['/home/agent-center/single']);
  }

  upgradeToBusinessEdition() {

  }

  goToPurchasePage() {
    window.open(this.purchaseUrl, '_blank', 'noopener,noreferrer');
  }
}
