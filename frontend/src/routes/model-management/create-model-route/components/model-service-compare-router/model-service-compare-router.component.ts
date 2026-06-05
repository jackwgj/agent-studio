import {
  Component,
  Input,
  ChangeDetectorRef,
  ViewChild,
  ElementRef,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from 'angular-i18next';
import { Subject } from 'rxjs';
import { TextFieldModule } from '@angular/cdk/text-field';
import flowCommonLogic from '../../../../agent-center/app-flow/common-logic-workflow';
import { v4 as uuidV4 } from 'uuid';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { CommonUtils } from 'src/utils/common.util';
import { SendData } from '@shared/components/sender/sender.component';
import { JiuwenModelService } from '@services/jiuwen-model/jiuwen-model.service';
import { ModelChatItemComponent } from '@routes/model-management/online-test/components/chat-item/chat-item.component';
import { ModelSenderComponent } from '@routes/model-management/online-test/components/sender/sender.component';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'model-service-compare-router',
  templateUrl: './model-service-compare-router.component.html',
  styleUrls: ['./model-service-compare-router.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    TextFieldModule,
    ModelSenderComponent,
    ModelChatItemComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.AGENT,
        I18nNamespace.JIUWEN_MODEL,
        I18nNamespace.MODEL_ACCESS
      ],
    },
  ],
})
export class ModelServiceCompareRouter {
  public changeUrl = cdnAssetUrl;
  @Input() serviceInfo = {
    name: '',
    logo:'',
    service_key: '',
    id:''
  };

  @Input() settingInfo = {
    stream_val: true,
    securityVerify:false
  }
  @Input() config = [];


  @ViewChild('chatContainerRef') chatContainerRef!: ElementRef;

  public isShowStopIcon = false;

  /** 预置的prompt */
  public presetPrompt = this.i18n.transform('vm_preset_prompt',{
    ns: I18nNamespace.JIUWEN_MODEL
  });
  public dialogHistory: any[][] = [
    [
      {
        role: 'system',
        content: this.presetPrompt,
      },
    ],
  ];

  public sendDisabled = false;

  public isRequesting = false;

  public isLoading = false;

  public uuid = uuidV4();

  public isTimeoutOrError = false;



  public userQueryLimit;

  public lang = CommonUtils.getLanguage();

  private destroy$ = new Subject<void>();

  private sseInstance: any;

  /** 表示用户是否向上滚动 */
  private isUserScrolling = false;

  /** 表示纵向滚动条的当前位置 */
  private lastScrollTop = 0;
  /** 记录当前模型服务，修改配置时，不重置聊天*/
  private currentServiceKey = '';

  private abortController: AbortController;

  constructor(
    private i18n: I18NextEagerPipe,
    private cdr: ChangeDetectorRef,
    private jiuwenModelServ: JiuwenModelService,
    private configServ: AgentConfigService,
    private message: NzMessageService,
  ) {}

  ngOnInit() {

    this.userQueryLimit =
      this.configServ.getConfigs().user_query_limit ?? 100000;
  }

  ngOnChanges(): void {
    if (this.currentServiceKey !== this.serviceInfo?.service_key) {
      this.currentServiceKey = this.serviceInfo?.service_key ?? '';
      this.clearChat();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  public sendQuestion(chatContent: SendData) {
    const oneChat = [chatContent];
    this.dialogHistory.push(oneChat);
    this.isUserScrolling = false;
    this.scrollToBottom();

    const messages = this.getDebugRunParam(this.dialogHistory.length - 1, true);
    this.postChat(messages, this.dialogHistory.length - 1);
    this.cdr.markForCheck();
  }

  /** 点击底部的清空对话，开启新聊天 */
  public clearChat() {
    this.sseInstance?.close(); // 作用：上一轮对话中，断开 (cancel) 最后一个问题的流式接口
    this.isShowStopIcon = false; // 隐藏"停止生成"按钮
    this.dialogHistory = [
      [
        {
          role: 'system',
          content: this.presetPrompt,
        },
      ],
    ]; // 置空页面主体的多轮对话
    this.isRequesting = false; // 在新一轮对话中，保证能发送输入的新问题
    this.isLoading = false;
  }

  private getChatConfig() {
    const result = {};
    this.config.forEach((c) => {
      result[c.name] = c.value;
    });
    return result;
  }

  private getChatParam(msg: any) {
    const configObj = this.getChatConfig();
    const messages = [];
    this.dialogHistory.forEach((his) => {
      //第一个系统提示词不加入对话历史
      if (his.length > 1) {
        his.forEach((h) => {
          messages.push({
            role: h.role,
            content: h.content,
          });
        });
      }
    });
    messages.push({
      role: 'user',
      content: msg.query,
    });
    return {
      model: this.serviceInfo.id,
      stream: this.settingInfo.stream_val,
      messages,
      ...configObj,
      content_security_verify: {
        is_response_verify: this.settingInfo.securityVerify,
        is_request_verify: this.settingInfo.securityVerify,
      },
    };
  }

  private postChat(messages: any, currentIndex: number) {
    this.isRequesting = true;
    this.isLoading = true;
    this.isShowStopIcon = true;
    this.isTimeoutOrError = false;
    this.isUserScrolling = false;
    const param = this.getChatParam(messages);
    if (this.settingInfo.stream_val) {
      this.postStream(param, currentIndex);
    } else {
      this.postMessage(param, currentIndex);
    }
  }

  private postMessage(param, currentIndex) {
    this.abortController = new AbortController();
    this.jiuwenModelServ
      .modelTestChat(param, this.abortController?.signal)
      .then(({content}) => {
        let assistantMessage = {
          role: 'assistant',
          content,
        };
        this.dialogHistory[currentIndex] = [
          ...this.dialogHistory[currentIndex],
          assistantMessage,
        ];
        this.dialogHistory = [
          ...this.dialogHistory.slice(0, currentIndex),
          this.dialogHistory[currentIndex],
          ...this.dialogHistory.slice(currentIndex + 1),
        ];
        this.scrollToBottom();
      })
      .finally(() => {
        this.isRequesting = false;
        this.isLoading = false;
        this.isShowStopIcon = false;
        this.cdr.markForCheck();
      });
  }

  private postStream(param, currentIndex) {
    this.sseInstance = this.jiuwenModelServ.modelTestChatSSE(
      param,
      this.lang,
      {
        onMessage: (token: any) => {
          try {
            const chunkDataObj =
              token.data && flowCommonLogic.stringToObject(token.data);
            const event = chunkDataObj.choices ? 'message' : 'error';
            const info = chunkDataObj?.choices[0]?.delta || chunkDataObj?.choices[0]?.message
            const content =
              info?.content ??
              info?.reasoning_content ?? '';

            if (event === 'error') {
              let assistantMessage = this.dialogHistory[currentIndex].find(
                (item) => item.role === 'assistant',
              );
              if (assistantMessage) {
                assistantMessage.content = content;
              } else {
                assistantMessage = {
                  role: 'assistant',
                  content,
                };
                this.dialogHistory[currentIndex] = [
                  ...this.dialogHistory[currentIndex],
                  assistantMessage,
                ];
              }
            }

            if (event === 'message') {
              let assistantMessage = this.dialogHistory[currentIndex].find(
                (item) => item.role === 'assistant'
              );
              if (!assistantMessage) {
                this.isLoading = false;
              }
              if (content) {
                this.isLoading = false;
              }
              if (assistantMessage) {
                // 追加大模型的中间输出
                assistantMessage.content += content;
              } else {
                assistantMessage = {
                  role: 'assistant',
                  content,
                };
                this.dialogHistory[currentIndex] = [
                  ...this.dialogHistory[currentIndex],
                  assistantMessage,
                ];
              }
              this.dialogHistory = [
                ...this.dialogHistory.slice(0, currentIndex),
                this.dialogHistory[currentIndex],
                ...this.dialogHistory.slice(currentIndex + 1),
              ];
            }
          } catch (error) {}
          this.scrollToBottom();
          if (this.isRequesting) {
            this.isShowStopIcon = true;
          }
          this.cdr.markForCheck();
        },
        onDone: () => {
          this.isRequesting = false;
          this.isLoading = false;
          this.isShowStopIcon = false;
          this.cdr.markForCheck();
        },
        onError: (error: { data: string; status: any }) => {
          this.isRequesting = false;
          this.isLoading = false;
          this.isShowStopIcon = false;
          this.isTimeoutOrError = true;
          if (error.data) {
            try {
              const errInfo = JSON.parse(error.data);
              this.message.error(errInfo.error_msg);
            } catch {
              this.message.error(this.i18n.transform('NetErrorTips'),);
            }
          }
          this.scrollToBottom();
          this.cdr.markForCheck();
        },
        onTimeout: () => {
          this.isRequesting = false;
          this.isLoading = false;
          this.isShowStopIcon = false;
          this.isTimeoutOrError = true;
          this.scrollToBottom();
          this.cdr.markForCheck();
        },
      },
    );
  }

  /** 回答完成 或 发送新问题时，页面滚动条到最底部 */
  private scrollToBottom() {
    if (!this.isUserScrolling) {
      setTimeout(() => {
        this.chatContainerRef.nativeElement.scrollTop =
          this.chatContainerRef.nativeElement.scrollHeight;
      }, 0);
    }
  }

  /** 获取预览调试的run接口入参 */
  private getDebugRunParam(currentIndex: number, isUserInput: boolean = false) {
    const { content, uploadData } = this.dialogHistory[currentIndex][0] || {};
    let query = content;
    if (uploadData?.[0]?.url && isUserInput) {
      query = `${JSON.stringify(uploadData.map((item) => item.url))} ${query}`;
    }

    const debugRunParam: {
      query: string;
    } = {
      query,
    };

    return debugRunParam;
  }

  public stopChat() {
    this.sseInstance?.close();
    this.isRequesting = false;
    this.isLoading = false;
    this.isShowStopIcon = false;
    this.scrollToBottom();
    this.cdr.markForCheck();
  }

  @HostListener('scroll', ['$event.target'])
  onScroll(target: any) {
    const currentScrollTop = target?.scrollTop;

    // 判断是否向上滚动
    if (currentScrollTop < this.lastScrollTop) {
      this.isUserScrolling = true; // 停止自动滚动
    } else if (
      currentScrollTop + target?.clientHeight >= target.scrollHeight - 2
    ) {
      this.isUserScrolling = false; // 继续自动滚动（当用户手动滚动到最底部）
    }
    this.lastScrollTop = currentScrollTop;
  }
}
