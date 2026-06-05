import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { agentCommonLogic } from '@routes/agent-center/app-agent/common-logic-agent';
import {AppMarkdownAnswerComponent} from "@shared/components/app-markdown-answer/app-markdown-answer.component";
import {MessageComponent} from '@shared/services/cfdata.service';
import {Clipboard} from "@angular/cdk/clipboard";
import {CommonUtils} from "../../../../../../utils/common.util";

@Component({
  selector: 'agent-deep-research-article',
  templateUrl: './deep-research-article.component.html',
  styleUrls: ['./deep-research-article.component.less'],
  imports: [
    CommonModule,
    MODULES,
    AppMarkdownAnswerComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.AGENT_CENTER,
      ],
    },
    agentCommonLogic,
  ],
})
export class DeepResearchArticleComponent implements OnInit, OnDestroy {
  @Input() articleId: string = '';
  @Input() content: string = '';
  @Input() width: string = '';
  @Input() title: string = '';
  @Output() closeArticleEnv = new EventEmitter<boolean>();


  constructor(
    private i18n: I18NextEagerPipe,
    private clipboard: Clipboard
  ){}

  ngOnInit() {}

  ngOnChanges(changes) {
    if(changes.content){
      const regex = /\[citation:\d+\]/g;
      this.content = changes.content.currentValue.replace(regex, '')
    }
  }

  ngOnDestroy() {}

  handleClickCloseArticleEnv() {
    this.closeArticleEnv.emit();
  }

  downloadFile(){
    const blob = new Blob([this.content], {type: 'text/markdown'})
    CommonUtils.downloadFile(blob, `${this.title}.md`, true);
  }

  copy() {
    this.clipboard.copy(this.content.replace('\n\n', ''));
    MessageComponent.showSuccess(this.i18n.transform('copy_successful'));
  }
}
