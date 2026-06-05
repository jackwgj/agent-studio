import { Component, Input, Optional } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzModalRef } from 'ng-zorro-antd/modal';

@Component({
  selector: 'intent-add-tip-modal',
  templateUrl: './intent-add-tip-modal.component.html',
  styleUrls: ['./intent-add-tip-modal.component.less'],
  standalone: true,
  imports: [MODULES, NzButtonModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class IntentAddTipModalComponent {
  protected readonly changeUrl = cdnAssetUrl;

  @Input()
  public packageName: string = '';

  @Input()
  public intentId: string = '';

  tipContent: string = '';

  constructor(
    private i18n: I18NextEagerPipe,
    @Optional() private modalRef: NzModalRef
  ) {}

  ngOnInit() {
    this.tipContent = this.i18n.transform('intent_add_success_tip_content', {
      packName: this.packageName,
    });
  }

  ngOnChanges(): void {
    this.tipContent = this.i18n.transform('intent_add_success_tip_content', {
      packName: this.packageName,
    });
  }

  dismiss(): void {
    this.modalRef?.destroy();
  }

  close(): void {
    this.modalRef?.close();
  }
}
