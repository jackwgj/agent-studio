import { TextFieldModule } from '@angular/cdk/text-field';
import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewChild,
} from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { Subject, takeUntil } from 'rxjs';
import { v4 as uuidV4 } from 'uuid';
import { MessageComponent } from '@shared/services/cfdata.service';
import { NzDrawerService } from 'ng-zorro-antd/drawer';
import { I18nNamespace } from '@i18n';
import { Network } from '@model';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import {
  DeepResearchChatItemComponent
} from "@routes/agent-center/app-deep-research/runtime/components/deep-research-chat-item/deep-research-chat-item.component";
import { DeepResearchSubService } from "@routes/agent-center/app-deep-research/runtime/deep-research-sub.service";
import { AppFileHalfModalComponent } from "@routes/agent-center/app-file/app-file-half-modal.component";
import type { ISingleAgentQueryResp } from "@routes/agent-center/types/my-workspace.types";
import { changePythonJson } from "@routes/agent-center/utils";
import { AppAgentRepoService } from "@services/agent-center/app-agent-repo.service";
import { DeepResearchService } from "@services/agent-center/deep-research.service";
import { WindowResizeService } from "@shared/base/window-resize.service";
import { SendData, SenderComponent } from '@shared/components/sender/sender.component';
import { MODULES } from '@shared/modules';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { CommonUtils } from 'src/utils/common.util';
import { halfModalWidth } from "../../../../../../utils/utils";
import flowCommonLogic from '../../../../app-flow/common-logic-workflow';

@Component({
  selector: 'deep-research-chat',
  templateUrl: './deep-research-chat.component.html',
  styleUrls: ['./deep-research-chat.component.scss'],
  imports: [
    CommonModule,
    MODULES,
    TextFieldModule,
    DeepResearchChatItemComponent,
    SenderComponent
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.AGENT,
        I18nNamespace.JIUWEN_MODEL,
        I18nNamespace.MODEL_ACCESS,
        I18nNamespace.TRACE
      ],
    },
  ],
})
export class DeepResearchChatComponent {
  public changeUrl = cdnAssetUrl;
  @Input() agentInfo: ISingleAgentQueryResp = {
    icon: '',
    name: '',
    description: '',
    id: '',
    agentSubType: '',
  };
  @Input() agentType: string = '';
  //判断是发布页 true  还是调试页 false
  @Input() isRuntime: boolean = false;
  @Input() config = [];
  @Input() searchEngine: any[] = []; // 搜索引擎数据
  @Output() expandDialogEnv = new EventEmitter<boolean>();
  @Output() viewArticleEnv = new EventEmitter<object>();
  @Output() disabledSender = new EventEmitter<boolean>();
  @ViewChild('chatContainerRef') chatContainerRef!: ElementRef;

  public isShowStopIcon = false;
  public sendDisable = false;

  /** 预置的prompt */
  public presetPrompt = this.i18n.transform('vm_preset_prompt', {
    ns: I18nNamespace.JIUWEN_MODEL
  });
  public dialogHistory = [];

  public tempDialogHistory = []

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

  public originHistroy = []

  public originMessageMap = {}


  public gradientLoadingIcon = `${Network.ASSETS_PATH}app-library/chating.gif`;
  public isExpandDialog: boolean = false; // 控制对话框的伸缩功能
  public viewArticle: boolean = false; // 判断用户是否查看deepResearch产物

  public startDisabled = false;

  public collectorInfoRetrievalMap = {}
  public taskInfo: any = {}
  public markDownInfo: any = {}

  public fileInfo: any = {}
  public windowWidth: number;

  public conversationRuntime: string = '0s'

  public placeholder: string = ''

  constructor(
    private i18n: I18NextEagerPipe,
    private cdr: ChangeDetectorRef,
    private deepResearchServ: DeepResearchService,
    private agentRepoServe: AppAgentRepoService,
    private nzDrawerService: NzDrawerService,
    private configServ: AgentConfigService,
    private deepResearchSubService: DeepResearchSubService,
    private windowResizeService: WindowResizeService,
  ) {
  }

  ngOnInit() {
    this.windowResizeService.getWindowWidth().subscribe((width) => {
      this.windowWidth = width;
    });

    this.deepResearchSubService
      .chatUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(async (task: any) => {
        if (task && task.id) {
          //任务创建后，创建会话
          this.taskInfo = task
          this.dialogHistory[0].task_id = task.id
          if (this.fileInfo.id) {
            task.files = [this.fileInfo.id]
          }
          this.startChatStream(task)
        }
      });

    //切换任务
    this.deepResearchSubService
      .chatTaskUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(async (task: any) => {
        if (task && task.id) {
          this.taskInfo = task
          this.initConfig()
          this.getTaskHistroy(task)
          //切换任务 关闭markdown
        }
      });

    //新建任务
    this.deepResearchSubService
      .taskPageUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(async (value: boolean) => {
        if (value) {
          this.taskInfo = {}
          this.initConfig()
        }
      });
    this.userQueryLimit =
      this.configServ.getConfigs().user_query_limit ?? 20000;

  }

  ngOnChanges(changes): void {
    if (false) {
      this.clearChat();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onClear() {
    this.clearChat();
  }

  initConfig() {
    this.sseInstance?.close();
    this.dialogHistory = []
    this.originMessageMap = {}
    this.collectorInfoRetrievalMap = {}
    this.sendDisable = false;
    this.startDisabled = false;
    this.isShowStopIcon = false;
    this.placeholder = '';
    this.fileInfo = {}
    if (this.viewArticle) {
      this.viewArticle = false
      this.viewArticleEnv.emit({show: this.viewArticle, content: '', title: ''});
    }
  }

  public sendQuestion(chatContent: SendData) {
    this.sendDisable = true
    this.dialogHistory.push(chatContent)
    this.tempDialogHistory.push(chatContent)
    this.isUserScrolling = false;
    this.scrollToBottom();
    this.postChat(chatContent, this.dialogHistory.length - 1);

    this.cdr.markForCheck();
  }

  /** 点击底部的清空对话，开启新聊天 */
  public clearChat() {
    this.uuid = uuidV4()
    this.sseInstance?.close(); // 作用：上一轮对话中，断开 (cancel) 最后一个问题的流式接口
    this.isShowStopIcon = false; // 隐藏"停止生成"按钮
    this.dialogHistory = []; // 置空页面主体的多轮对话
    this.isRequesting = false; // 在新一轮对话中，保证能发送输入的新问题
    this.isLoading = false;
    this.sendDisable = false;
    this.startDisabled = false;
  }

  private getChatConfig() {
    const result = {};
    this.config.forEach((c) => {
      result[c.name] = c.value;
    });
    return result;
  }

  private postChat(messages: any, currentIndex: number) {
    this.isRequesting = true;
    this.isLoading = true;
    this.isShowStopIcon = true;
    this.isTimeoutOrError = false;
    this.isUserScrolling = false;
    const param = this.getDebugRunParam(messages, true);
    //todo
    if (!this.isRuntime) {
      this.postStream(param);
    } else {
      //运行态 创建任务&调用第一次会话
      //获取this.dialogHistory[0] 如果存在task_id点击发送 就第二轮会话
      let conversation = this.dialogHistory[0]
      if (conversation.task_id) {
        param.task_id = conversation.task_id
        this.startChatStream(param)
      } else {
        this.deepResearchSubService.setTaskCreate(param)
      }
    }

  }


  handleMessage(item) {
    if (item.role === 'user') {
      this.dialogHistory.push(item)
    }
    if (item.role === 'assistant') {
      //事件：{“start”,”done”,”message”,”summary_response”, “waiting_user_input”}
      // 流式输出的第一条数据，event值为start；中间数据，event值为message；最后一条数据，event值为done。
      // 非流式输出则一次仅一条数据，event值为summary_response。
      // 中间反馈的数据，event值为waiting_user_input。


      //section_idx
      // 章节编号，1-10表示子章节的id编号，0表示是主图结点，没有章节编号

      // entry:当系统判定是非深度研究的简单query时，返回对简单问 ---deep 不会出现
      // generate_questions :基于query提出3个问题给用户 ，需要有确认按钮
      // feedback_handler:中断消息，提醒用户反馈
      // plan_reasoning :每个章节的研究任务
      // outline :报告的大纲，其中sections字段包括多个章节
      // sub_reporter:章节子报告内容
      // collector_info_retrieval:搜索结果，包括搜索query、搜索网页url、搜索网页标题，一一对应
      // collector_summary:研究步骤的收集信息的总结内容
      // end :最终报告+溯源信息+异常信息
      //
      // generate_questions :基于query提出3个问题给用户 ，需要有确认按钮---html 判断
      // feedback_handler:输入框可输入---deepresearch 回答期间是否可输入？ ---另外定义一个字段 流式末尾，message List是否存在该类型
      // plan_reasoning 塞到 outline
      // collector_info_retrieval 塞到outline html需要展示title可跳转

      // sub_reporter:章节子报告内容 流式输出 可追加message_id一样   需要根据 idx快速追加 ---只有这需要做流式处理
      // collector_summary:展示在章节尾

      // 可能会刷新
      // outline
      // plan_reasoning


      // "agent": "end" && "section_idx": "0" ---- 会话完成
      //
      // 开发态展示 response_content
      // 运行态 展示 file_name


      //定义一个map messageId_agentType key--index,value内容
      //因为当前数据没有塞 所以index为this.dialogHistory.length
      let newMessage = this.handleAnswerMessage(item, this.dialogHistory.length)
      if (newMessage) {
        this.dialogHistory.push(newMessage)
      }
    }
  }

  handleAnswerMessage(item, index) {
    if (typeof item.content === "string") {
      item.content = changePythonJson(item.content)
    }
    if (!item.content || item.content === '\n') {
      return false
    }
    switch (item.agent) {
      case "entry":
        this.fileInfo.disabled = true
        return item;
      case "generate_questions":
        this.isShowStopIcon = false
        return item;
      case "outline":
        this.isLoading = false;
        this.startDisabled = true;
        //if 存在  需要覆盖
        if (this.originMessageMap[item.agent]) {
          this.dialogHistory.pop()
          index = this.dialogHistory.length
        }
        if (item.event === 'done') {
          item.status = 'finished'
        }
        this.originMessageMap[item.agent] = {
          ...item, index: index
        }
        return item;
      case "plan_reasoning":
        //找到outline位置,根据idx 塞到content.sections
        this.pushThoughtInfo('outline', item)
        break;
      case "collector_info_retrieval":
        //找到outline位置,塞
        this.pushMessageInfo('outline', item, 'collectorInfoRetrieval')
        break;
      case "sub_reporter":
        //找到outline位置,流式追加
        this.handleSubReporter('outline', item)
        break;
      case "collector_summary":
        //找到outline位置,塞
        this.pushMessageInfo('outline', item)
        break;
      case "feedback_handler":
        //todo 等待用户输入
        this.sendDisable = false;
        this.isLoading = false;
        this.placeholder = '';
        break;

      case "end":
        //找到outline位置,塞
        // "agent": "end" && "section_idx": "0" ---- 会话完成
        //
        // 开发态展示 response_content
        // 运行态 展示 file_name
        if (item.section_idx === '0') {
          this.isShowStopIcon = false
          this.isLoading = false
          this.sendDisable = true;
          this.pusEndInfo('outline', item)
        } else {
          this.handleSessionStatus('outline', item)
        }
        break;
      case "error":
        this.handleError(item)
        break;
      case "heartbeat":
        break;

      default:
        return item;
        break;
    }
    return ''
  }

  pushThoughtInfo(key, item) {
    //找到outline位置,根据idx 塞到content.sections
    //层级太深 可能会出现数据不更新的情况
    const key1 = 'content'
    const key2 = 'sections'
    const id = Number(item.section_idx) - 1
    if (!this.originMessageMap[key]) {
      //todo 无outline
      return
    }
    let index = this.originMessageMap[key].index

    let sections = this.dialogHistory[index][key1][key2]
    if (sections && sections[id]) {
      if (sections[id].thought) {
        sections[id].thought.push(item)
      } else {
        sections[id] = {
          ...sections[id],
          thought: [item]
        }
      }
    }
    this.dialogHistory[index][key1][key2] = sections
  }

  pushMessageInfo(key, item, type?) {
    //找到outline位置,根据idx 塞到content.sections
    //层级太深 可能会出现数据不更新的情况
    const key1 = 'content'
    const key2 = 'sections'
    const id = Number(item.section_idx) - 1
    if (!this.originMessageMap[key]) {
      //todo 无outline
      return
    }
    let index = this.originMessageMap[key].index
    if (!type) {
      type = 'children'
    }
    let collectorInfoRetrievalItem = {}
    let sections = this.dialogHistory[index][key1][key2]
    if (item.agent === 'collector_info_retrieval') {
      this.signUrlType(item)
      if (!this.collectorInfoRetrievalMap[id]) {
        this.collectorInfoRetrievalMap[id] = {}
        this.collectorInfoRetrievalMap[id][item.content.query] = {
          index: sections[id][type]?.length || 0,
          children: [item]
        }
      } else {
        if (this.collectorInfoRetrievalMap[id][item.content.query]) {
          this.collectorInfoRetrievalMap[id][item.content.query].children.push(item)
        } else {
          this.collectorInfoRetrievalMap[id][item.content.query] = {
            index: sections[id][type]?.length,
            children: [item]
          }
        }

      }
      collectorInfoRetrievalItem = {
        title: item.content.query,
        children: this.collectorInfoRetrievalMap[id][item.content.query].children,
      }
    }

    if (sections && sections[id]) {
      if (type === 'children') {
        if (sections[id][type]) {
          sections[id][type].push(item)
        } else {
          sections[id][type] = [item]
        }
      } else {
        //todo 判断map里有没有  塞 还是push
        if (sections[id][type]) {
          const collectorInfoRetrievalIndex = this.collectorInfoRetrievalMap[id][item.content.query].index
          //找到index
          if (sections[id][type][collectorInfoRetrievalIndex]) {
            sections[id][type][collectorInfoRetrievalIndex] = collectorInfoRetrievalItem
          } else {
            sections[id][type].push(collectorInfoRetrievalItem)
          }

        } else {
          sections[id][type] = [collectorInfoRetrievalItem]
        }
      }
    }
    this.dialogHistory[index][key1][key2] = sections
  }

  handleSubReporter(key, item) {
    //找到outline位置,根据idx 塞到content.sections
    //层级太深 可能会出现数据不更新的情况
    //后续多个outline key变成 key_id
    const key1 = 'content'
    const key2 = 'sections'
    const id = Number(item.section_idx) - 1
    if (!this.originMessageMap[key]) {
      //todo 无outline
      return
    }
    let index = this.originMessageMap[key].index
    let sub_reporter = this.originMessageMap[key]['sub_' + item.section_idx]
    let sections = this.dialogHistory[index][key1][key2]
    if (sub_reporter) {
      //如果存在追加
      sub_reporter.item.content += item.content
    } else {
      //如果不存在添加
      // 同时需要判断有没有children,正常是有的
      sub_reporter = {
        index: sections[id]?.children?.length ? sections[id]?.children.length : 0,
        item: item
      }
    }
    this.originMessageMap[key]['sub_' + item.section_idx] = sub_reporter
    if (sections[id]?.children) {
      sections[id].children[sub_reporter.index] = sub_reporter.item
    } else {
      sections[id] = {...sections[id], children: [sub_reporter.item]}
    }
    this.dialogHistory[index][key1][key2] = sections
  }

  handleSessionStatus(key, item) {
    //找到outline位置,根据idx 塞到content.sections
    //层级太深 可能会出现数据不更新的情况
    const key1 = 'content'
    const key2 = 'sections'
    const id = Number(item.section_idx) - 1
    if (!this.originMessageMap[key]) {
      //todo 无outline
      return
    }
    let index = this.originMessageMap[key].index
    let sections = this.dialogHistory[index][key1][key2]
    if (item.agent === 'end') {
      sections[id].status = 'finished'
    }
    this.dialogHistory[index][key1][key2] = sections
  }

  pusEndInfo(key, item) {
    //找到outline位置,根据idx 塞到content.sections
    //层级太深 可能会出现数据不更新的情况
    const key1 = 'content'
    item.content = changePythonJson(item.content)
    if (item.event === 'error') {
      this.handleError(item)
      if (this.originMessageMap[key]) {
        let index = this.originMessageMap[key]?.index
        this.dialogHistory[index].status = 'finished'
        this.dialogHistory[index].collapsed = true
      }
      return
    }
    if (this.originMessageMap[key]) {
      let index = this.originMessageMap[key]?.index
      let content = this.dialogHistory[index][key1]
      this.dialogHistory[index].status = 'finished'
      this.dialogHistory[index].collapsed = true
      content.status = 'finished'
      if (item.content !== 'ALL END') {
        content.end = item
      }
      this.dialogHistory[index][key1] = content
    } else {
      this.dialogHistory.push({
        content: {
          role: 'assistant',
          end: item,
          status: 'finished',
          collapsed: true
        }
      })
    }
    this.placeholder = this.i18n.transform('deep_research_chat.deep_research_16');
  }


  signUrlType(item) {
    if (item.content && item.content.url) {
      item.content.tip = item.content.url.indexOf('localdataset') > -1 ? 'knowledge' : 'http'
    }
  }


  private postStream(param) {
    this.sseInstance?.close();
    this.sseInstance = this.agentRepoServe.deepResearchChatSSE(
      param,
      this.agentInfo.id,
      this.uuid,
      'DEBUG',
      this.lang,
      {
        onMessage: (token: any) => {
          try {
            const chunkDataObj =
              token.data && flowCommonLogic.stringToObject(token.data);
            const event = chunkDataObj.event;
            const content = chunkDataObj.content;
            if (event === 'error') {
              this.handleError(chunkDataObj)
            } else if (chunkDataObj.content) {
              this.handleMessage(chunkDataObj)
            } else if (chunkDataObj.event === 'statistic_data') {
              this.chatRuntime(chunkDataObj)
            }
          } catch (error) {

          }
          this.scrollToBottom();
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
          if (error?.data) {
            try {
              const errInfo = JSON.parse(error.data);
              MessageComponent.showError(errInfo.error_msg, 3000);
            } catch {
              MessageComponent.showError(
                this.i18n.transform('NetErrorTips'),
                3000,
              );
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

  private startChatStream(param) {
    this.placeholder = this.i18n.transform('deep_research_chat.deep_research_15')
    this.sseInstance?.close();
    this.sseInstance = this.deepResearchServ.deepResearchChatSSE(
      param,
      this.agentInfo.id,
      this.uuid,
      'DEBUG',
      this.lang,
      {
        onMessage: (token: any) => {
          try {
            const chunkDataObj =
              token.data && flowCommonLogic.stringToObject(token.data);
            if (chunkDataObj.content) {
              chunkDataObj.role = chunkDataObj.role ? chunkDataObj.role : 'assistant';
              this.handleMessage(chunkDataObj)
            } else if (chunkDataObj.event === 'error') {
              this.handleError(chunkDataObj)
            } else if (chunkDataObj.event === 'statistic_data') {
              this.chatRuntime(chunkDataObj)
            }
          } catch (error) {
          }
          this.scrollToBottom();
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
              MessageComponent?.showError(errInfo.error_msg, 3000);
            } catch {
              MessageComponent.showError(
                this.i18n.transform('NetErrorTips'),
                3000,
              );
            }
          }
          this.scrollToBottom();
          this.cdr.markForCheck();
        },
        onTimeout: () => {
          this.isShowStopIcon = false;
          this.isTimeoutOrError = true;
          this?.scrollToBottom();
          this.isRequesting = false;
          this.isLoading = false;
          this?.cdr.markForCheck();
        },
      },
    );
  }


  /** 获取预览调试的run接口入参 */
  private getDebugRunParam(messages: SendData, isUserInput: boolean = false) {
    const {content, uploadData} = messages;
    let query = content;
    if (uploadData?.[0]?.url && isUserInput) {
      query = `${JSON.stringify(uploadData.map((item) => item.url))} ${query}`;
    }

    let debugRunParam: any = {
      inputs: {
        message: query,
      },
    };
    if (this.dialogHistory.length > 1) {
      debugRunParam = {inputs: {...debugRunParam.inputs, interrupt_feedback: 'accepted'}}
    }

    if (this.isRuntime) {
      debugRunParam.query = query
    }
    return debugRunParam;
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

  // 控制对话框伸缩或者扩展
  public handleClickExpandDialog() {
    this.isExpandDialog = !this.isExpandDialog;
    this.viewArticle = false;
    this.expandDialogEnv.emit(this.isExpandDialog);
  }

  // 是否查看deepResearch产物
  public handleClickViewArticle(content) {
    const {conversation_id, message_id, section_idx} = content
    if (!this.markDownInfo?.conversation_id
      || (this.markDownInfo.conversation_id === conversation_id
        && this.markDownInfo.message_id === message_id
        && this.markDownInfo.section_idx === section_idx)) {
      this.viewArticle = !this.viewArticle;
    }
    this.markDownInfo = content
    this.isExpandDialog = true;
    this.viewArticleEnv.emit({show: this.viewArticle, content: this.viewArticle ? content.content : '', title: this.viewArticle ? content.title : ''});
  }

  getTaskHistroy(taskInfo) {
    this.deepResearchServ.getFullMessageList({task_id: taskInfo.id}).then((res) => {
      //处理res this.dialogHistory
      this.sendDisable = false;
      if (res.data) {
        this.handleMessageList(res.data)
      }

      if (taskInfo.status >= 30) {
        this.startDisabled = true
      }
      if (taskInfo.status > 20) {
        this.sendDisable = true;
      }
      if (taskInfo.status >= 40) {
        this.placeholder = this.i18n.transform('deep_research_chat.deep_research_16')
      }
      if (taskInfo.status === 10 || taskInfo.status === 30) {
        this.placeholder = this.i18n.transform('deep_research_chat.deep_research_15')
        this.startChatStream({task_id: taskInfo.id, query: "continue"})
      }
    })
  }


  handleMessageList(list) {
    list.forEach((item, index) => {
      if (item.type === 1) {
        this.dialogHistory.push({
          role: 'user',
          content: item.content,
          task_id: item.task_id
        })
      }
      if (index === 0 && item.mag_file_infos.length && item.mag_file_infos[0]?.file_name) {
        this.fileInfo = {fileName: item.mag_file_infos[0].file_name, id: item.mag_file_infos[0].file_id, disabled: true}
      }
      if (item.type === 2) {
        item.chunks.forEach(event => {
          if (event.event === 'statistic_data') {
            this.chatRuntime(event)
          } else {
            let newMessage = this.handleAnswerMessage(event, this.dialogHistory.length)
            if (newMessage) {
              newMessage.task_id = item.task_id
              this.dialogHistory.push(newMessage)
            }
          }
        })
      }
    })

  }

  public handleEndError(item) {
    const content = this.i18n.transform('NetErrorTips') + this.getError(item.content);
    this.handleMessage({
      role: 'assistant',
      content,
      event: item.agent
    })
  }

  public handleError(chunkDataObj) {
    const content = this.i18n.transform('NetErrorTips');
    this.handleMessage({
      role: 'assistant',
      content,
      event: chunkDataObj.event
    })
    this.placeholder = this.i18n.transform('deep_research_chat.deep_research_16');
  }

  public getError(content) {
    if (typeof content === 'string') {
      return content
    }
    if (content.exception_info || content.response_content) {
      return content.exception_info || content.response_content
    }
    return '';
  }

  showTemplate() {

  }

  chatRuntime(data: any) {
    let hours = Math.floor(data?.latency?.overall / 3600);
    let minutes = Math.floor((data?.latency?.overall % 3600) / 60);
    let secs = Math.round(data?.latency?.overall % 60);
    if (data?.latency?.overall) {
      if (data?.latency?.overall >= 60 * 60) {
        this.conversationRuntime = hours + 'h' + (minutes>0?(minutes + 'min'):'') +(secs >0?(secs+ 's'):'')
      } else if (data?.latency?.overall >= 60) {
        this.conversationRuntime = minutes + 'min' +(secs >0?(secs+ 's'):'')
      } else {
        this.conversationRuntime = data?.latency?.overall + 's'
      }
    }
  }

  public handleClickAddTemplateModal(data: any) {
    if (data === 'change') {
      const drawerRef = this.nzDrawerService.create({
        nzTitle: '',
        nzFooter: null,
        nzWidth: halfModalWidth(this.windowWidth),
        nzClosable: false,
        nzMaskClosable: false,
        nzPlacement: 'right',
        nzContent: AppFileHalfModalComponent,
        nzContentParams: {
          isTemplate: true,
        },
      });
      const contentComponent = drawerRef.getContentComponent();
      if (contentComponent?.uploadFileEnv) {
        contentComponent.uploadFileEnv.subscribe((result: any) => {
          if (!result) {
            return;
          }
          this.fileInfo = result;
          drawerRef.close();
        });
      }
    } else {
      this.fileInfo = {};
    }
  }

  handleDisabledSender(data: boolean) {
    this.disabledSender.emit(data);
  }

  public getIsExpandDialog() {
    if(this.isExpandDialog) {
      return this.i18n.transform('collapse', {
        ns: I18nNamespace.TRACE,
      });

    } else {
      return this.i18n.transform('expand', {
        ns: I18nNamespace.TRACE,
      });
    }
  }
}
