import {
  Component, EventEmitter,
  HostListener,
  Input,
  OnDestroy, Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  SourceFileDetailComponent
} from '@routes/agent-center/app-agent/components/source-file-detail/source-file-detail.component';
import {KnowledgeRepoService} from '@services/agent-center/knowledge.service';
import {KbAnswerResolverService} from '@services/knowledge-center/kb-answer-resolver.service';
import {MODULES} from '@shared/modules';
import {I18nNamespace} from '@i18n';
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from 'angular-i18next';
import {cdnAssetUrl} from 'src/single-spa/assets-url';
import {AppMarkdownAnswerComponent} from '@shared/components/app-markdown-answer/app-markdown-answer.component';
import {
  AssetFstLatencyIconComponent
} from '@shared/components/assets/asset-fst-latency-icon/asset-fst-latency-icon.component';
import {
  AssetColorUserIconComponent
} from '@shared/components/assets/asset-color-user-icon/asset-color-user-icon.component';
import {AssetQuoteIconComponent} from '@shared/components/assets/asset-quote-icon/asset-quote-icon.component';
import {CustomPopconfirmComponent} from '@shared/components/custom-popconfirm/custom-popconfirm.component';
import {ActivatedRoute} from '@angular/router';
import {ContextService} from '@services/context.service';
import {MessageComponent} from '@shared/services/cfdata.service';

import {ClickOutsideDirective} from '@shared/directives/click-outside.directive';
import {UploadFileIconComponent} from '@shared/components/upload-file-icon/upload-file-icon.component';
import {Subject, takeUntil} from 'rxjs';
import {TtsPlayerService, TtsState} from '@services/tts-player.service';
import {AgentDataService} from '@services/agent-center/agent-data.service';
import {AgentConfigService} from '@routes/agent-center/agent-config.service';
import {NzModalService} from 'ng-zorro-antd/modal';
import {AiAnswerListService} from "@routes/agent-center/app-agent/components/chat-item/ai-answer.component.service";
import {CommonUtils} from 'src/utils/common.util';
import {AssetCopyIconComponent} from "@shared/components/assets/asset-copy-icon/asset-copy-icon.component";
import {SendData} from "@shared/components/sender/sender.component";
import {FormateTimePipe} from "../../../../../../pipes/formate-time.pipe";

import {DeepResearchService} from "@services/agent-center/deep-research.service";
import {Clipboard} from "@angular/cdk/clipboard";

@Component({
  selector: 'deep-research-chat-item',
  templateUrl: './deep-research-chat-item.component.html',
  styleUrls: ['./deep-research-chat-item.component.scss'],
  imports: [
    CommonModule,
    MODULES,
    CustomPopconfirmComponent,
    AppMarkdownAnswerComponent,
    AssetFstLatencyIconComponent,
    AssetColorUserIconComponent,
    AssetQuoteIconComponent,
    AssetCopyIconComponent,
    ClickOutsideDirective,
    UploadFileIconComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.TRACE, I18nNamespace.AGENT],
    },
    TtsPlayerService,
    FormateTimePipe
  ],
})
export class DeepResearchChatItemComponent implements OnDestroy {
  @Input() agentIcon: string;

  @Input() content: any = {};

  @Input() conversationId: string;

  @Input() messageId: number;

  @Input() isError?: boolean;

  @Input() agentName: string;

  @Input() taskId: string;

  @ViewChild('tip') tipDirective: any;

  @Input() isQuestionsLoading: boolean;

  @Input() startDisabled: boolean;
  @Input() isRuntime: boolean;

  @Input() conversationRuntime?:string

  @Output() send = new EventEmitter<SendData>();

  @Output() openViewArticle = new EventEmitter<SendData>();

  public cdnUrl = cdnAssetUrl;

  public isF1 = this.ctxServ.isF1ReqList();

  public supportFeedback = this.configServ.getConfigs()?.feedback_enable ?? false;

  public supportAnnotation = this.configServ.getConfigs()?.agentops_evaluation_display && this.configServ.getConfigs().agentops_menu_display;

  private agentId = '';


  public voiceEnable = false;

  public soundState = TtsState.Idle;

  private destroy$ = new Subject<void>();

  public actions = [
    {
      icon: 'assets/agent-center/images/MD.svg',
      id: 'md',
      label: 'Markdown',
    },
    {
      id: 'docx',
      icon: 'assets/agent-center/images/word.svg',
      label: 'Word',
    },

    {
      id: 'pdf',
      icon: 'assets/agent-center/images/pdf.svg',
      label: 'Pdf',
    }
  ];

  constructor(
    private route: ActivatedRoute,
    private clipboard: Clipboard,
    private ctxServ: ContextService,
    private i18n: I18NextEagerPipe,
    private ttsPlayerServe: TtsPlayerService,
    private agentDataServe: AgentDataService,
    private configServ: AgentConfigService,
    private nzModalService: NzModalService,
    public aiAnswerListService: AiAnswerListService,
    public kbAnswerResolverService: KbAnswerResolverService,
    private knowledgeRepoService: KnowledgeRepoService,
    private formateTimePipe: FormateTimePipe,
    private deepResearchServ: DeepResearchService,
  ) {
    this.route.queryParams.subscribe((params) => {
      this.agentId = params.agentId;
    });

    this.voiceEnable = this.configServ.voiceEnable();

    this.ttsPlayerServe.state$
      .pipe(takeUntil(this.destroy$))
      .subscribe((state) => (this.soundState = state));
  }

  sourceFiles = [];

  showFileList = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.data) {

    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.showFileList = false;
    this.kbAnswerResolverService.clear();
  }


  public getTrimmedQuestion(question: string) {
    return question.trim();
  }


  public getText(content: any): string {
    return content?.content?.trim() ?? '';
  }

  public onSoundOut(content: any) {
    if (this.soundState === TtsState.Playing) {
      this.ttsPlayerServe.stop();
      return;
    }
    const {language, timbre, domain} = this.agentDataServe.getVoiceConfig();
    this.ttsPlayerServe.play(this.getText(content), {
      property: `${language}_${timbre}_${domain}`,
    });
  }

  /** 点赞按钮 */
  public likeAnswer(content: any) {
  }

  /** 点踩抽屉的取消按钮 */
  public cancelDislike() {

  }

  /** 点踩按钮 - 踩 or 取消踩 */
  public submitDislike(content: any) {

  }

  /** 当调用点踩接口完成时，关闭Tip弹窗 */
  public closeDislikeTip() {

  }

  /** 点踩后提交反馈问题 */
  public submitDisLikeFeedback(params) {
  }

  /** 取消反馈 */
  private cancelFeedback(content: any) {

  }

  show(sourceFileDetail): void {
    const modalRef = this.nzModalService.create({
      nzTitle: '',
      nzFooter: null,
      nzContent: SourceFileDetailComponent,
    });
    const instance = modalRef.componentInstance as SourceFileDetailComponent;
    instance.sourceFileDetail = sourceFileDetail;
  }

  downloadSourceFileFn(file) {
    const {file_id, knowledge_base_id, type, document_name} = file;
    if (file_id && knowledge_base_id) {
      if (type === 'faq') {
        this.knowledgeRepoService.downloadFaqFile(knowledge_base_id, file_id).then((res) => {
          CommonUtils.downloadFile(res as Blob, document_name, true);
        })
      } else {
        this.knowledgeRepoService.retrieveFileContent(knowledge_base_id, file_id).then((res) => {
          CommonUtils.downloadFile(res as Blob, document_name, true);
        })
      }
    }
  }


  @HostListener('scroll', ['$event.target'])
  onScroll(target: any) {
    this.kbConfRef.hide();
  }

  tipContext: any = {};
  @ViewChild('kbConfRef') kbConfRef: any;

  closeTag() {
    this.kbConfRef.hide();
  }

  public like(e, row) {

  }

  public disLike(e, row) {

  }



  public changeUrl = cdnAssetUrl;

  public isRequesting = false;

  public timbre = '';

  /** 复制、点赞和点踩按钮是否可见。大模型的回答可能是空字符串，不能调用反馈接口 */
  public isVisibleOfLastBtns(item: any): boolean {
    return (
      item.showBottomBtns &&
      !item.isTimeoutOrError &&
      item.showAnswer?.trim() !== ''
    );
  }

  /** 复制整体答案 */
  copyAllAnswer(messages: any,) {
    this.clipboard.copy(messages.replace('\n\n', ''));
    MessageComponent.showSuccess(this.i18n.transform('copy_successful'));
  }

  /** 点击底部的清空对话，开启新聊天 */
  public clearChat() {

    this.isRequesting = false; // 在新一轮对话中，保证能发送输入的新问题
  }

  public regenerate(item: any, index: number) {
    if (!this.canRegenerate(item)) {
      return;
    }
  }

  /**
   * 每轮问答的【重新生成】按钮是否可以点击。
   * 原则是当前页面中，只能有一个流式请求
   */
  public canRegenerate(item: any): boolean {
    return !this.isRequesting && !!item.showQuestion.trim();
  }

  public handleCollapsedChat(item) {
    item.collapsed = !item?.collapsed
  }

  public startTask() {
    this.send.emit({
      role: 'user',
      content: this.i18n.transform('deepresearchchatitem-2',{ns: I18nNamespace.AGENT}),
      uploadData: []
    })
  }

  onClick(e) {
    e.stopPropagation()
  }

  handelTask(action, fileItem) {
    this.downloadFile(action.id, this.taskId, fileItem)
  }

  previewFile(fileItem, title) {
    const {end} = fileItem.content
    let query: any = {
      task_id: this.taskId, file_id: end.content.file_id
    }
    if (!end.response_content) {
      this.deepResearchServ.downloadFile(query).then((res) => {
        this.blob2Text(res).then((response_content) => {
            end.response_content = response_content
            this.openViewArticle.emit({...end, content: response_content, title: title})
          }
        )
      })
    } else {
      this.openViewArticle.emit({...end, content: end.response_content, title: title})
    }
  }

  blob2Text(blob, type = "Text") {
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onloadend = () => resolve(reader.result);
      try {
        reader[`readAs${type}`](blob);
      } catch (e) {
        console.error("Error:", e);
      }
    });
  }

  downloadFileLocal(e, fileItem) {
    e.stopPropagation()
    const blob = new Blob([fileItem.content.end.content.response_content], {type: 'text/markdown'})
    CommonUtils.downloadFile(blob, `${fileItem.content.title}.md`, true);
  }

  downloadFile(type, task_id, fileItem) {
    const {title, end} = fileItem
    let query: any = {
      task_id: task_id, file_id: end.content.file_id
    }
    if (type !== 'md') {
      query.target_format = type
    }
    this.deepResearchServ.downloadFile(query).then((res) => {
      CommonUtils.downloadFile(res as Blob, `${title}.${type}`, true);
    })
  }

  getTime(time: any) {
    return this.formateTimePipe.transform(time);
  }

  public handleOpenViewArticle(content, title) {
    this.openViewArticle.emit({...content, title: title})
  }

  public openViewEndArticle(content, title) {
    if (this.isRuntime) {
      this.previewFile(content, title)
      return
    }
    let end = {
      ...content.content.end, content: content.content.end.content.response_content, title: title
    }
    this.openViewArticle.emit(end)
  }
}
