/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/no-unsafe-call */
/* eslint-disable @typescript-eslint/no-unsafe-member-access */
import { Component, Input } from '@angular/core';
import { MODULES } from '@shared/modules';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { type IChatFlowAppDetail } from '@routes/agent-center/app-flow/app-flow.types';
import { PipesModule } from 'src/pipes/pipes.module';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'right-op-panel',
  templateUrl: './right-op-panel.component.html',
  styleUrls: ['./right-op-panel.component.less'],
  imports: [MODULES, NzToolTipModule, PipesModule],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class RightOpPanelComponent {
  constructor(private i18n: I18NextEagerPipe) {}

  @Input() sources = '';

  @Input() headInfo: IChatFlowAppDetail;

  public changeUrl = cdnAssetUrl;

  public isExpanded = true;

  public show = false;

  public onToggle() {
    this.isExpanded = !this.isExpanded;
  }

  get filterPlugIn() {
    if (this.headInfo.tools.length > 7) {
      return this.headInfo.tools.slice(0, 7);
    }
    return this.headInfo.tools;
  }

  get showConfig() {
    return (
      this.headInfo?.model_name &&
      this.headInfo?.tools.length > 0 &&
      this.headInfo?.workflows.length > 0
    );
  }
}
