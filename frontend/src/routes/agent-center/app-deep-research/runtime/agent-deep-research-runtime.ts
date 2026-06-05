import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { agentCommonLogic } from '@routes/agent-center/app-agent/common-logic-agent';
import {
  DeepResearchChatComponent
} from "@routes/agent-center/app-deep-research/runtime/components/deep-research-chat/deep-research-chat.component";
import type { ISingleAgentQueryResp } from "@routes/agent-center/types/my-workspace.types";
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import {
  DeepResearchArticleComponent
} from '@routes/agent-center/app-deep-research/runtime/components/deep-research-article/deep-research-article.component';
import * as I18next from 'angular-i18next';

@Component({
  selector: 'agent-deep-research-runtime',
  templateUrl: './agent-deep-research-runtime.html',
  styleUrls: ['./agent-deep-research-runtime.less'],
  imports: [
    CommonModule,
    MODULES,
    DeepResearchChatComponent,
    DeepResearchArticleComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.AGENT,
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.COMMON,
        I18nNamespace.TRACE,
      ],
    },
    agentCommonLogic,
  ],
})
export class AgentDeepResearchRuntime implements OnInit, OnDestroy {
  @Input() agentInfo: ISingleAgentQueryResp = {
    icon: '',
    name: '',
    description: '',
    id:''
  };
  @Input() isRuntime?:boolean
  @Input() searchEngine: any[] = [];
  @Input() agentType: string = '';
  @Output() expandDialogEnv = new EventEmitter<boolean>();
  @Output() viewArticleEnv = new EventEmitter<boolean>();

  protected readonly changeUrl = cdnAssetUrl;
  dialogWidth: string = '100%';
  articleWidth: string = '0%';

  public articleContent:string ='';
  public articleTitle:string ='';
  public isShowNoSearchEngineTip: boolean = false;

  constructor(private i18n: I18next.I18NextEagerPipe) {
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {}

  handleClickExpandDialog(data: boolean): void {
    this.dialogWidth = '100%';
    this.articleWidth = '0%';
    this.expandDialogEnv.emit(data);
  }

  handleClickViewArticle(data: any): void {
    this.articleTitle = data.title;
    this.articleContent = data.content;
    this.viewArticleEnv.emit(data.show);
    if (data.show) {
      this.articleWidth = '40%';
      this.dialogWidth = '60%';
    } else {
      this.articleWidth = '0%';
      this.dialogWidth = '100%';
    }
  }

  handleClickCloseArticle(): void {
    this.articleWidth = '0%';
    this.dialogWidth = '100%';
  }

  handleDisabledSender(data: boolean) {
    this.isShowNoSearchEngineTip = data;
  }
}
