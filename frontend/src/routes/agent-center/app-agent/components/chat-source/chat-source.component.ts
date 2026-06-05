import {
  Component, Input,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  getFileExtension,
  messageSourceFileMap,
} from '@routes/agent-center/app-agent/components/chat-item/chat-item.enum';
import {
  SourceFileDetailComponent
} from '@routes/agent-center/app-agent/components/source-file-detail/source-file-detail.component';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { cdnAssetUrl } from '../../../../../single-spa/assets-url';
import { NzModalService } from "ng-zorro-antd/modal";

@Component({
  selector: 'chat-source',
  templateUrl: './chat-source.component.html',
  styleUrls: ['./chat-source.component.less'],
  imports: [
    CommonModule,
    MODULES,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class ChatSourceComponent {
  @Input() sourceFiles = [];

  constructor(
    private nzModal: NzModalService,
  ) {}

  show(sourceFileDetail): void {
    this.nzModal.create({
      nzTitle: null,
      nzFooter: null,
      nzContent: SourceFileDetailComponent,
      nzWidth: 500,
      nzData: {
        sourceFileDetail
      }
    });
  }


  protected readonly cdnAssetUrl = cdnAssetUrl;
  protected readonly messageSourceFileMap = messageSourceFileMap;
  protected readonly getFileExtension = getFileExtension;
}
