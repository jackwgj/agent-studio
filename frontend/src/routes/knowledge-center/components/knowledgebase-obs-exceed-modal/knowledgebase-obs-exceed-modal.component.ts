import { Component, Input, Optional } from '@angular/core';
import { HelpDocKey } from '@constants/support-topic.const';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { HelpLinksService } from '@shared/services/help-links.service';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzModalRef } from 'ng-zorro-antd/modal';

@Component({
  selector: 'knowledgebase-obs-exceed-modal',
  templateUrl: './knowledgebase-obs-exceed-modal.component.html',
  styleUrls: ['./knowledgebase-obs-exceed-modal.component.less'],
  standalone: true,
  imports: [MODULES, NzButtonModule],
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
export class KnowledgebaseObsExceedModalComponent {
  protected readonly changeUrl = cdnAssetUrl;

  @Input()
  public totalAmount: number = 1;

  @Input()
  public isEnterprise: boolean = false;

  purchaseUrl: string = '';

  constructor(
    private helpLinksService: HelpLinksService,
    @Optional() private modalRef: NzModalRef
  ) {}

  ngOnInit() {
    this.purchaseUrl = this.helpLinksService.getHelpCenterLink(
      HelpDocKey.PURCHASE_GUIDE,
    );
  }

  upgradeToBusinessEdition() {
  }

  goToPurchasePage() {
    window.open(this.purchaseUrl, '_blank', 'noopener,noreferrer');
  }

  close(): void {
    this.modalRef?.close();
  }
}
