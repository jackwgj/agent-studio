import {
  Component, Input, OnDestroy, OnInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  getFileExtension,
  messageSourceFileMap,
} from '@routes/agent-center/app-agent/components/chat-item/chat-item.enum';
import type { ReferenceItemData } from '@routes/knowledge-center/knowledge-base.interface';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { KbAnswerResolverService } from '@services/knowledge-center/kb-answer-resolver.service';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { cdnAssetUrl } from '../../../../../single-spa/assets-url';
import { MarkedPipe } from "../../../../../pipes/marked.pipe";
import { AsyncPipe } from "@angular/common";
import { SafeHtmlPipe } from "../../../../../pipes/safehtml.pipe";
@Component({
  selector: 'source-file-detail',
  templateUrl: './source-file-detail.component.html',
  styleUrls: ['./source-file-detail.component.less'],
  imports: [
    CommonModule,
    MODULES,
    MarkedPipe,
    AsyncPipe,
    SafeHtmlPipe,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})

export class SourceFileDetailComponent implements OnInit, OnDestroy{

  @Input() sourceFileDetail: ReferenceItemData;

  renderData: ReferenceItemData;

  constructor(
    private knowledgeRepoService: KnowledgeRepoService,
    private kbAnswerResolverService: KbAnswerResolverService,
  ) {}

  ngOnInit() {
    if (this.sourceFileDetail) {
      const { slicingFiles, ...others } = this.sourceFileDetail;
      this.renderData = {
        ...others,
        slicingFiles: slicingFiles.map(file => {
          const { content, ...fileDetail } = file;
          return {
            ...fileDetail,
            content: this.kbAnswerResolverService.resolveImage(
              content,
              (imgId: string) => this.knowledgeRepoService.queryImage(imgId),
            ),
          }
        })
      }
    }
  }

  public formatToTwoDecimal(num) {
    return parseFloat(num.toFixed(2));
  }

  protected readonly messageSourceFileMap = messageSourceFileMap;
  protected readonly getFileExtension = getFileExtension;
  protected readonly cdnAssetUrl = cdnAssetUrl;

  ngOnDestroy() {
    // 该组件可不对kbAnswerResolverService执行clear
    // 因为是在智能体的嵌套组件，会由智能体进行清空。清空反而会增加调用，降低性能
  }
}
