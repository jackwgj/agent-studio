import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectorRef,
  ViewChild,
  ElementRef,
  HostListener,
  Input,
  Output,
  EventEmitter,
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { MODULES } from '@shared/modules';
import { Subject } from 'rxjs';
import { v4 as uuidV4 } from 'uuid';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import {
  AgentVal,
  IAgentVar,
} from '../agent-center/app-agent/components/var-config/var-config.component';
import flowCommonLogic from '../agent-center/app-flow/common-logic-workflow';
import {
  SendData,
  SenderComponent,
} from '@shared/components/sender/sender.component';
import { CommonUtils } from '../../utils/common.util';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { AgentDataService } from '@services/agent-center/agent-data.service';
import { AssetInterpreterIconComponent } from '@shared/components/assets/asset-interpreter-icon/asset-interpreter-icon.component';
import { ChatItemUpdateComponent } from '@routes/agent-center/app-agent/components/chat-item-update/chat-item-update.component';
import { IWorkflowField } from '@routes/agent-center/app-flow/node.type';
import { ContextService } from '@services/context.service';
import { cdnAssetUrl } from '../../single-spa/assets-url';

@Component({
  selector: 'ai-assist-agent-preview',
  templateUrl: './ai-assist-agent-preview.component.html',
  styleUrls: ['./ai-assist-agent-preview.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    NzToolTipModule,
    NzSpinModule,
    SenderComponent,
    AssetInterpreterIconComponent,
    ChatItemUpdateComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.COMMON,
        I18nNamespace.REVIEW,
        I18nNamespace.AGENT,
      ],
    },
  ],
})
export class AiAssistAgentPreviewComponent implements OnInit, OnDestroy {
  public lang = CommonUtils.getLanguage();

  private readonly DESTROY$ = new Subject<void>();
  isExpandDialog: boolean = false;

  get expandDialogText(): string {
    return this.isExpandDialog 
      ? this.i18n.transform('full_screen_hide')
      : this.i18n.transform('full_screen_display');
  }

  @ViewChild('chatItem') chatItemRef;
  @ViewChild('chatContainRef') chatContainRef!: ElementRef;

  @Input()
  agentInfo: any = {
    agentId: '',
    name: '',
    icon: '',
    description: '',
    prompt: '',
    modelDeploymentId: '',
    enable: false,
    openText: '',
    openQuestions: [],
  };

  /** 表示用户是否向上滚动 */
  public isUserScrolling = false;

  /** 表示纵向滚动条的当前位置 */
  public lastScrollTop = 0;

  public chatHistory: any[][] = [];

  public chatParams: IAgentVar[] = [];

  public isCodeInterpreter = false;

  public isCodeInterpreterActive = true;

  private codeInterpreterId = '';

  public isRequesting = false;

  public sensitiveFlag = false;

  public isShowStopIcon = false;

  public isTimeoutOrError = false;

  public isStopped = false;

  public uuid = uuidV4();

  public deploymentId: string;

  public pluginAndFlowList = {
    flows: [],
    plugins: [],
    kbs: [],
  };

  /** 最近一轮问答对应的自动追问列表是否可见。对于已展示的最近一轮自动追问列表，不受追问开关的影响 */
  public isRecentQuestionVisible = false;

  public isQuestionsLoading = false;

  // 自动追问接口是否失败。若失败，则隐藏【你可以继续提问】
  public isQuestionsFailed = false;

  public showMemo = false;

  public versionId = '';

  public agentSseInstance: any;

  public userQueryLimit;

  public agentParams: IWorkflowField[] = [];

  public isCloseAgentParamsPanel = false;
  public changeUrl = cdnAssetUrl;

  public isF1 = this.ctxServ.isF1ReqList();

  public sendDisabled = false;
  public isHovered = false;

  public isDisabledSenderBtn: boolean = false;

  isRightLoading = false;

  @Output() expandDialogEnv = new EventEmitter<boolean>();

  constructor(
    private i18n: I18NextEagerPipe,
    private cdr: ChangeDetectorRef,
    private appService: AppAgentRepoService,
    public agentDataServe: AgentDataService,
    public configServ: AgentConfigService,
    public ctxServ: ContextService,
    private nzMessageService: NzMessageService,
  ) {}

  ngOnInit(): void {
    this.userQueryLimit =
      this.configServ.getConfigs().user_query_limit ?? 100000;
  }

  ngOnDestroy(): void {
    this.DESTROY$.next();
    this.DESTROY$.complete();
  }

  public handleClickExpandDialog() {
    this.isExpandDialog = !this.isExpandDialog;
    this.expandDialogEnv.emit(this.isExpandDialog);
  }

  @HostListener('scroll', ['$event.target'])
  onScroll(target: any) {
    if (this.chatItemRef) {
      this.chatItemRef?.kbConfRef?.hide();
    }
    const currentScrollTop = target.scrollTop;

    // 判断是否向上滚动
    if (currentScrollTop < this.lastScrollTop) {
      this.isUserScrolling = true; // 停止自动滚动
    } else if (
      currentScrollTop + target.clientHeight >=
      target.scrollHeight - 2
    ) {
      this.isUserScrolling = false; // 继续自动滚动（当用户手动滚动到最底部）
    }
    this.lastScrollTop = currentScrollTop;
  }

  public submitQuestion(question: string) {
    const chatContent = {
      role: 'user',
      content: question,
    };
    const oneChat = [chatContent];
    this.chatHistory.push(oneChat);
    this.scrollRightToBottom();

    const messages = this.getDebugRunParam(this.chatHistory.length - 1);
    this.postStream(messages, this.chatHistory.length - 1);
    this.cdr.markForCheck();
  }

  /** 获取预览调试的run接口入参 */
  private getDebugRunParam(currentIndex: number, isUserInput: boolean = false) {
    const { content, uploadData } = this.chatHistory[currentIndex][0] || {};
    let query = content;
    let files = [];
    if (uploadData?.[0]?.url && isUserInput) {
      query = `${JSON.stringify(uploadData.map((item) => item.url))} ${query}`;
      files = uploadData.map((item) => item.url);
    }
    let inputs: Record<string, AgentVal> = null;
    if (this.chatParams.length) {
      inputs = {};
      this.chatParams.forEach((param) => {
        if (['array', 'object'].includes(param.type)) {
          try {
            inputs[param.name] = JSON.parse(param.val as string);
          } catch {
            inputs[param.name] = null;
          }
        } else {
          inputs[param.name] = param.val;
        }
      });
    }

    const debugRunParam: {
      query: string;
      files: string[];
      inputs?: Record<string, AgentVal>;
      tool_switch_dict?: Record<string, any>;
    } = {
      query,
      files,
    };

    if (inputs && Object.keys(inputs).length > 0) {
      debugRunParam.inputs = inputs;
    }

    // 添加代码解释器插件并关闭的情况，要多传一个tool_switch_dict
    if (this.isCodeInterpreter && !this.isCodeInterpreterActive) {
      debugRunParam.tool_switch_dict = {
        [this.codeInterpreterId]: false,
      };

      return debugRunParam;
    }
    return debugRunParam;
  }

  public postStream(messages: any, currentIndex: number) {
    // 开始新一轮问答前，先清空追问问题
    this.chatHistory = this.chatHistory.map((conversation) =>
      conversation.filter((item) => item.role !== 'followUpQuestions'),
    );
    this.sensitiveFlag = false;
    this.isRequesting = true;
    this.isRightLoading = true;
    this.isShowStopIcon = true;
    this.isTimeoutOrError = false;
    this.isStopped = false;
    this.isUserScrolling = false;
    messages.model_deployment_id = this.agentInfo.modelDeploymentId ?? '';
    this.agentSseInstance = this.appService.knowledgeChatSSE(
      messages,
      this.agentInfo?.agentId,
      this.uuid,
      'DEBUG',
      this.lang,
      {
        onMessage: (token: any) => {
          try {
            const chunkDataObj =
              token.data && flowCommonLogic.stringToObject(token.data);
            const {
              event,
              content,
              plugin,
              latency,
              createdTime,
              data,
              reasoning_content,
            } = chunkDataObj || {};
            if (event === 'error') {
              let assistantMessage = this.chatHistory[currentIndex].find(
                (item) => item.role === 'assistant',
              );
              if (assistantMessage) {
                assistantMessage.content = this.i18n.transform('run_error');
              } else {
                assistantMessage = {
                  role: 'assistant',
                  content: this.i18n.transform('run_error'),
                };
                this.chatHistory[currentIndex] = [
                  ...this.chatHistory[currentIndex],
                  assistantMessage,
                ];
                // 对话运行失败时不能点赞踩的标识
                this.chatHistory[currentIndex][0].isError = true;
              }
              this.endThink(currentIndex);
            }

            if (event === 'plugin_start') {
              const { memory_variables } = plugin || {};
              let thought = this.getThought(currentIndex);
              if (
                memory_variables &&
                Object.keys(memory_variables).length > 0
              ) {
                const info = chunkDataObj;
                this.chatHistory[currentIndex] = [
                  ...this.chatHistory[currentIndex],
                  { ...info, role: 'memorySet' },
                ];
                // 触发变更检测：更新 chatHistory 的引用！！！
                this.chatHistory = [
                  ...this.chatHistory.slice(0, currentIndex),
                  this.chatHistory[currentIndex],
                  ...this.chatHistory.slice(currentIndex + 1),
                ];
              }
              if (thought !== '' && thought !== '\n\n' && thought !== '\n') {
                this.handlePluginCalls(chunkDataObj, currentIndex, thought);
              } else {
                this.handlePluginCalls(chunkDataObj, currentIndex);
              }
              this.endThink(currentIndex);
            }
            if (event === 'plugin_end') {
              this.handlePluginCalls(chunkDataObj, currentIndex);
              this.endThink(currentIndex);
            }

            if (event === 'message') {
              // 思考
              let thinkMessage = this.chatHistory[currentIndex].findLast(
                (item) => item?.role?.startsWith('think'),
              );
              let modelMessage = this.chatHistory[currentIndex].findLast(
                (item) => item?.role?.startsWith('model'),
              );
              let assistantMessage = this.chatHistory[currentIndex].find(
                (item) => item.role === 'assistant',
              );
              if (!thinkMessage || !assistantMessage || !modelMessage) {
                this.isRightLoading = false;
              }
              if (reasoning_content || content) {
                this.isRightLoading = false;
              }
              if (thinkMessage && thinkMessage.outputting && content) {
                // 追加大模型的中间输出
                if (content !== '\n\n' && content !== '\n') {
                  thinkMessage.reasoning_content += content;
                }
              } else if (
                modelMessage &&
                modelMessage.outputting &&
                reasoning_content
              ) {
                if (
                  reasoning_content !== '\n\n' &&
                  reasoning_content !== '\n'
                ) {
                  modelMessage.reasoning_content += reasoning_content;
                }
              } else {
                if (
                  reasoning_content !== undefined &&
                  reasoning_content !== '\n\n' &&
                  reasoning_content !== '\n'
                ) {
                  modelMessage = {
                    role: 'model' + uuidV4(),
                    thinking: true,
                    reasoning_content,
                    outputting: true,
                  };
                  if (thinkMessage) {
                    this.endThink(currentIndex, 'think');
                  }
                  this.chatHistory[currentIndex] = [
                    ...this.chatHistory[currentIndex],
                    modelMessage,
                  ];
                }
                if (
                  content !== undefined &&
                  content !== '\n\n' &&
                  content !== '\n'
                ) {
                  thinkMessage = {
                    role: 'think' + uuidV4(),
                    thinking: true,
                    reasoning_content: content,
                    outputting: true,
                  };
                  if (modelMessage) {
                    this.endThink(currentIndex, 'model');
                  }
                  this.chatHistory[currentIndex] = [
                    ...this.chatHistory[currentIndex],
                    thinkMessage,
                  ];
                }
              }
              if (assistantMessage) {
                if (
                  content !== undefined &&
                  content !== '\n\n' &&
                  content !== '\n'
                ) {
                  // 追加大模型的中间输出
                  assistantMessage.content += content;
                }
                thinkMessage.thinking = false;
              } else {
                if (content !== undefined) {
                  assistantMessage = {
                    role: 'assistant',
                    content,
                  };
                  this.chatHistory[currentIndex] = [
                    ...this.chatHistory[currentIndex],
                    assistantMessage,
                  ];
                }
              }
              this.chatHistory = [
                ...this.chatHistory.slice(0, currentIndex),
                this.chatHistory[currentIndex],
                ...this.chatHistory.slice(currentIndex + 1),
              ];
            }

            if (event === 'statistic_data') {
              let hasAssistant = false;
              const updatedItem = this.chatHistory[currentIndex].map((item) => {
                if (item.role === 'assistant') {
                  hasAssistant = true;
                  return { ...item, time_consumption: latency };
                }
                return item;
              });
              // 规避大模型总结之前没有返回任何的中间过程
              if (!hasAssistant) {
                updatedItem.push({
                  role: 'assistant',
                  time_consumption: latency,
                });
              }
              this.chatHistory = [
                ...this.chatHistory.slice(0, currentIndex),
                updatedItem,
                ...this.chatHistory.slice(currentIndex + 1),
              ];
              this.endThink(currentIndex);
            }

            if (event === 'summary_response') {
              if (createdTime) {
                // 赞踩反馈需要的messageId
                this.chatHistory[currentIndex][0].messageId = createdTime;
              }
              // 规避大模型总结之前没有返回任何的中间过程
              let hasAssistant = false;
              let quoteList = [];
              // 如果存在event:"plugin_start"并且plugin.name等于 "retrieval"
              const hasRetrieval = this.chatHistory[currentIndex].some(
                (item) =>
                  item.event === 'plugin_start' &&
                  item.plugin?.name === 'retrieval',
              );
              if (hasRetrieval) {
                const pluginEndItem = this.chatHistory[currentIndex].find(
                  (item) =>
                    item.event === 'plugin_end' && item.content?.output_list,
                );
                if (pluginEndItem) {
                  // 根据 document_name 去重
                  const uniqueDocuments = new Map();
                  pluginEndItem.content?.output_list.forEach((item) => {
                    if (!uniqueDocuments.has(item.document_name)) {
                      uniqueDocuments.set(item.document_name, item);
                    }
                  });
                  quoteList = Array.from(uniqueDocuments.values());
                }
              }
              const updatedItem = this.chatHistory[currentIndex].map((item) => {
                if (item.role === 'assistant') {
                  hasAssistant = true;
                  let res = this.sensitiveFlag ? item.content : content;
                  return {
                    ...item,
                    content: res,
                    res,
                    quoteList,
                    executionId: chunkDataObj.executionId,
                    tagNum: 0,
                  };
                }
                return item;
              });
              if (!hasAssistant) {
                updatedItem.push({
                  role: 'assistant',
                  executionId: chunkDataObj.executionId,
                  tagNum: 0,
                  content,
                  quoteList,
                });
              }
              this.chatHistory = [
                ...this.chatHistory.slice(0, currentIndex),
                updatedItem,
                ...this.chatHistory.slice(currentIndex + 1),
              ];
              this.endThink(currentIndex);
            }

            if (event === 'sensitive') {
              this.sensitiveFlag = true;
              const { offset, text } = data;
              let assistantMessage = this.chatHistory[currentIndex].find(
                (item) => item.role === 'assistant',
              );

              if (assistantMessage) {
                // 替换大模型回答中的敏感词
                const curAnsText = assistantMessage?.content ?? '';
                const safeOffset = Math.min(offset, curAnsText.length);
                assistantMessage.content =
                  curAnsText.slice(0, safeOffset) + (text ?? '');
              }

              // 替换完成后，更新对话历史
              this.chatHistory.splice(
                currentIndex,
                1,
                this.chatHistory[currentIndex],
              );
              this.endThink(currentIndex);
            }
          } catch (error) {}
          this.scrollRightToBottom();
          if (this.isRequesting) {
            this.isShowStopIcon = true;
          }
          this.cdr.markForCheck();
        },
        onDone: () => {
          this.isRequesting = false;
          this.isRightLoading = false;
          if (this.agentInfo.enable) {
            this.getNextQuestions(currentIndex);
          } else {
            this.isRecentQuestionVisible = false;
          }
          this.isShowStopIcon = false;
          this.showMemo = true;
          const hasHistory =
            this.chatHistory.length > 0 &&
            this.chatHistory[0].some((item) => item.role === 'assistant');
          this.chatHistory[currentIndex][0].isFinished = true;
          this.agentDataServe.setHasQAFlag(hasHistory);
          this.scrollRightToBottom();
          this.cdr.markForCheck();
        },
        onError: (error: { data: string; status: any }) => {
          // 对话运行失败时不能点赞踩的标识
          this.chatHistory[currentIndex][0].isError = true;
          this.isRequesting = false;
          this.isRightLoading = false;
          this.isShowStopIcon = false;
          this.isTimeoutOrError = true;
          if (error.data) {
            try {
              const errInfo = JSON.parse(error.data);
              const apiError = errInfo?.error_msg
                ? this.i18n.transform('ApiError', {
                    errCode: errInfo.error_code,
                    reason: errInfo.error_msg,
                  })
                : '';
              this.nzMessageService.error(apiError, { nzDuration: 3000 });
            } catch {
              this.nzMessageService.error(
                this.i18n.transform('NetErrorTips'),
                { nzDuration: 3000 },
              );
            }
          }
          this.scrollRightToBottom();
          this.cdr.markForCheck();
        },
        onTimeout: () => {
          this.isRequesting = false;
          this.isRightLoading = false;
          this.isShowStopIcon = false;
          this.isTimeoutOrError = true;
          this.nzMessageService.error(
            this.i18n.transform('streaming_timeout'),
            { nzDuration: 3000 },
          );
          this.scrollRightToBottom();
          this.cdr.markForCheck();
        },
      },
      this.versionId,
    );
  }

  /**
   * 阶段思考结束
   * @param currentIndex
   */
  endThink(currentIndex, key?: string) {
    let thinkMessage = this.chatHistory[currentIndex].findLast((item) =>
      item?.role?.startsWith('think'),
    );
    let modelMessage = this.chatHistory[currentIndex].findLast((item) =>
      item?.role?.startsWith('model'),
    );
    if (thinkMessage) {
      thinkMessage.outputting = false;
    }
    if (modelMessage) {
      modelMessage.outputting = false;
    }
  }

  /** 组装插件调用的thought字段值 */
  private getThought(currentIndex: number): string {
    const index = this.chatHistory[currentIndex].findIndex(
      (item) => item.role === 'assistant',
    );
    let thought = '';
    if (index !== -1) {
      thought =
        this.chatHistory[currentIndex].find((item) => item.role === 'assistant')
          .content ?? '';
      this.chatHistory[currentIndex].splice(index, 1);
    }
    return thought;
  }

  private handlePluginCalls(data: any, currentIndex: number, thought?: string) {
    const index = this.chatHistory[currentIndex].findIndex(
      (item) => item.role === 'assistant',
    );
    if (index !== -1) {
      this.chatHistory[currentIndex].splice(index, 1);
    }
    let tool = {};
    if (data?.plugin?.name) {
      if (data.plugin.name !== 'retrieval') {
        tool = this.handlePluginInfo(data.plugin.name, data.type);
      } else {
        let kbList = data?.result?.output_list || [];
        tool = this.handleKbs(kbList);
      }
    }

    // 添加 thought key
    let modifiedPlugin;
    if (thought) {
      modifiedPlugin = { ...data.plugin, thought };
    } else {
      modifiedPlugin = { ...data.plugin };
    }
    if (modifiedPlugin) {
      modifiedPlugin = { ...modifiedPlugin, ...tool };
    }
    const info = { ...data, plugin: modifiedPlugin };
    this.chatHistory[currentIndex] = [...this.chatHistory[currentIndex], info];
    // 触发变更检测：更新 chatHistory 的引用！！！
    this.chatHistory = [
      ...this.chatHistory.slice(0, currentIndex),
      this.chatHistory[currentIndex],
      ...this.chatHistory.slice(currentIndex + 1),
    ];
  }
  handlePluginInfo(name, type) {
    let tool;
    if (type === 'plugin' && this.pluginAndFlowList?.plugins?.length) {
      tool = this.pluginAndFlowList.plugins.find(
        (item) => item.plugin_display_name === name,
      );
      if (tool) {
        return {
          name:
            this.lang === 'zh-cn'
              ? tool.tool_chinese_name
              : tool.tool_display_name,
          icon: tool.tool_icon,
        };
      }
    }
    if (type === 'workflow' && this.pluginAndFlowList?.flows?.length) {
      tool = this.pluginAndFlowList.flows.find((item) => item.code === name);
      if (tool) {
        return {
          name: this.lang === 'zh-cn' ? tool.workflow_name : tool.code,
          icon: tool.tool_icon,
        };
      }
    }
    if (type === 'mcp') {
      return { name: name };
    }
    return { name: name };
  }

  handleKbs(kbList) {
    let tool;
    if (this.pluginAndFlowList.kbs.length && kbList.length) {
      tool = this.pluginAndFlowList.kbs.find(
        (item) => item.knowledge_repo_id === kbList[0].knowledge_base_id,
      );
      if (tool) {
        return { name: tool.knowledge_repo_name };
      }
    }
    return { name: 'retrieval' };
  }

  /** 获取自动追问列表。该接口完成后，才能发送新一轮的问题 */
  public getNextQuestions(currentIndex: number) {
    this.isQuestionsLoading = true;
    this.isQuestionsFailed = false;
    this.isRecentQuestionVisible = true;
    const params = {
      name: this.agentInfo.name,
      enable: this.agentInfo.enable,
      prompt: this.agentInfo.prompt,
      version_id: this.versionId,
    };
    this.appService
      .getNextQuestions(this.agentInfo?.agentId, this.uuid, params)
      .then((result) => {
        this.isQuestionsLoading = false;
        const { questions } = result;
        if (!questions.length) {
          this.isQuestionsFailed = true;
          this.cdr.markForCheck();
          return;
        }
        const followUpQuestions = {
          role: 'followUpQuestions',
          questions,
        };
        this.chatHistory[currentIndex] = [
          ...this.chatHistory[currentIndex],
          followUpQuestions,
        ];
        // 更新 chatHistory 的引用以触发变更检测
        this.chatHistory = [
          ...this.chatHistory.slice(0, currentIndex),
          this.chatHistory[currentIndex],
          ...this.chatHistory.slice(currentIndex + 1),
        ];
        this.cdr.markForCheck();
        this.scrollRightToBottom();
      })
      .catch(() => {
        this.isQuestionsLoading = false;
        this.isQuestionsFailed = true;
        this.cdr.markForCheck();
      });
  }

  /** 点击底部的清空对话，开启新聊天 */
  public clearChat() {
    this.agentSseInstance?.close(); // 作用：上一轮对话中，断开 (cancel) 最后一个问题的流式接口
    this.isShowStopIcon = false; // 隐藏"停止生成"按钮
    this.chatHistory = []; // 置空页面主体的多轮对话
    this.isRequesting = false; // 在新一轮对话中，保证能发送输入的新问题
    this.isRightLoading = false;
    this.uuid = uuidV4();
    this.agentDataServe.setHasQAFlag(false);
  }

  public sendQuestion(chatContent: SendData) {
    const oneChat = [chatContent];
    this.chatHistory.push(oneChat);
    this.isUserScrolling = false;
    this.scrollRightToBottom();

    const messages = this.getDebugRunParam(this.chatHistory.length - 1, true);
    this.postStream(messages, this.chatHistory.length - 1);
    this.cdr.markForCheck();
  }

  public stopChat() {
    this.agentSseInstance?.close();
    this.isRequesting = false;
    this.isRightLoading = false;
    this.isShowStopIcon = false;
    this.isStopped = true;
    this.scrollRightToBottom();
    this.cdr.markForCheck();
  }

  public showVarConfPanel() {
    this.isCloseAgentParamsPanel = false;
  }

  public toggleCodePlugin() {
    if (!this.isCodeInterpreter) {
      this.isCodeInterpreterActive = false;
      return;
    }
    this.isCodeInterpreterActive = !this.isCodeInterpreterActive;
  }

  public onMouseEnter() {
    this.isHovered = true;
  }

  public onMouseLeave() {
    this.isHovered = false;
  }

  public getDisableSender(event) {
    this.isDisabledSenderBtn = event;
  }

  get codeInterpretTip() {
    if (!this.isCodeInterpreter) {
      return this.i18n.transform('codeInterpretTip1');
    } else if (this.isCodeInterpreterActive) {
      return this.i18n.transform('codeInterpretTip2');
    } else {
      return this.i18n.transform('codeInterpretTip3');
    }
  }

  get isQuestionsVisible() {
    return (
      !this.isRequesting &&
      !this.isStopped &&
      !this.isTimeoutOrError &&
      this.isRecentQuestionVisible
    );
  }

  // 过滤出 role 等于 "followUpQuestions" 的对象
  public getFollowUpQuestions(currentConversationPair: any[]): any[] {
    return currentConversationPair.filter(
      (item) => item.role === 'followUpQuestions',
    );
  }

  public probeQuestions(question: string) {
    this.submitQuestion(question);
  }

  public scrollRightToBottom() {
    if (!this.isUserScrolling) {
      setTimeout(() => {
        this.chatContainRef.nativeElement.scrollTop =
          this.chatContainRef.nativeElement.scrollHeight;
      }, 0);
    }
  }
}
