import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { AppMarkdownAnswerComponent } from '@shared/components/app-markdown-answer/app-markdown-answer.component';
import { AssetFstLatencyIconComponent } from '@shared/components/assets/asset-fst-latency-icon/asset-fst-latency-icon.component';
import { AssetColorUserIconComponent } from '@shared/components/assets/asset-color-user-icon/asset-color-user-icon.component';
import { AssetQuoteIconComponent } from '@shared/components/assets/asset-quote-icon/asset-quote-icon.component';
import { CustomPopconfirmComponent } from '@shared/components/custom-popconfirm/custom-popconfirm.component';
import { ClickOutsideDirective } from '@shared/directives/click-outside.directive';
import { UploadFileIconComponent } from '@shared/components/upload-file-icon/upload-file-icon.component';

import { ChatItemComponent } from '@routes/agent-center/app-agent/components/chat-item/chat-item.component';
import { cdnAssetUrl } from '../../../../../single-spa/assets-url';

import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzPopoverModule } from 'ng-zorro-antd/popover';

@Component({
  selector: 'model-chat-item',
  templateUrl: './chat-item.component.html',
  styleUrls: ['./chat-item.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    CustomPopconfirmComponent,
    AppMarkdownAnswerComponent,
    AssetFstLatencyIconComponent,
    AssetColorUserIconComponent,
    AssetQuoteIconComponent,
    ClickOutsideDirective,
    UploadFileIconComponent,
    NzButtonModule,
    NzIconModule,
    NzToolTipModule,
    NzPopoverModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ModelChatItemComponent extends ChatItemComponent implements OnDestroy {
  public changeUrl = cdnAssetUrl;
}
