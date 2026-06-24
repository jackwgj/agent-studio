import {
  Component,
  Input,
  SimpleChanges,
  ViewChild,
  inject
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { AgentBotPageService } from "@routes/agent-center/app-agent/agent-bot-page/agent-bot-page.service";
import { MemoryManagementComponent } from "@shared/components/memory-management/memory-management.component";
import {
  ConversationState,
  IMemoryManagementData
} from "@shared/components/memory-management/memory-management.interface";
import { MODULES } from "@shared/modules";
import { I18nNamespace } from "@i18n";
import { I18NEXT_NAMESPACE } from "angular-i18next";
import { TextFieldModule } from "@angular/cdk/text-field";
import flowCommonLogic from "../../../../agent-center/app-flow/common-logic-workflow";

import { AssetInterpreterIconComponent } from "@shared/components/assets/asset-interpreter-icon/asset-interpreter-icon.component";
import { AssetLogAgentIconComponent } from "@shared/components/assets/asset-log-agent-icon/asset-log-agent-icon.component";
import {
  VarConfigComponent
} from "../var-config/var-config.component";
import {
  SenderComponent
} from "@shared/components/sender/sender.component";
import { VarMemoryModalComponent } from "@routes/agent-center/app-agent/components/var-memory-modal/var-memory-modal.component";
import { LongMemoryModalComponent } from "@routes/agent-center/app-agent/components/long-memory-modal/long-memory-modal.component";
import { PreviewDebugComponent } from "@routes/agent-center/app-agent/components/preview-debug/preview-debug.component";
import { ChatItemPlanComponent } from "@routes/agent-center/app-agent/components/chat-item-plan/chat-item-plan.component";
import { PrologueQuestionsAreaComponent } from "@routes/app-center/components/prologue-questions-area/prologue-questions-area.component";
import { takeUntil } from "rxjs";
import { sceneMessageType, waitNodeType, SummaryResponse } from "@routes/agent-center/app-agent/components/preview-debug-plan/preview-debug-plan.interface";
import { NzMessageService } from "ng-zorro-antd/message";

@Component({
  selector: "preview-debug-plan",
  templateUrl: "./preview-debug-plan.component.html",
  styleUrls: ["./preview-debug-plan.component.less", "../../../../../styles/text-field-prebuilt.css"],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    TextFieldModule,
    AssetInterpreterIconComponent,
    VarConfigComponent,
    SenderComponent,
    VarMemoryModalComponent,
    LongMemoryModalComponent,
    ChatItemPlanComponent,
    PrologueQuestionsAreaComponent,
    MemoryManagementComponent
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT, I18nNamespace.TRACE, I18nNamespace.MEMORY_LIB]
    }
  ]
})
export class PreviewDebugPlanComponent extends PreviewDebugComponent {
  @ViewChild("sender") sender!: SenderComponent;
  @Input() shortCode?: string = "";
  @Input() agentWebInfo: any;

  agentBotPageService = inject(AgentBotPageService);

  msgServ = inject(NzMessageService);

  //上一次会话以中断节点结束，记录中断节点的index 跟当前index 比较 相邻则提取 思考ing
  public waitNode = {
    index: -1,
    sceneMessage: { role: "assistant", status: "loading", plans: [] }
  };
  public inputList = [];

  public step_idx = 0;

  conversationState: ConversationState = {
    isExtract: true,
    isRetrieve: true
  };

  get memoryLibId() {
    return this.agentWebInfo?.memoryLibId || this.agentBotPageService?.getMemoryLib()?.memory_repo_id || "";
  }

  override ngOnInit() {
    this.userQueryLimit =
      this.configServ.getConfigs().user_query_limit ?? 100000;

    this.previewDebugServ
      .updateSendMeta()
      .pipe(takeUntil(this.destroy$))
      .subscribe((res: boolean) => {
        if (res) {
          this.sendDisabled = false;
        }

      });
  }

  override ngOnChanges(changes: SimpleChanges): void {
    const { shortCode, agentWebInfo } = changes;
    if (shortCode && shortCode.currentValue) {
      this.canSend();
    }
    super.ngOnChanges(changes);
  }

  override canSend() {
    // 通过是否存在非空的model字段，判断是否为第三方模型
    if (this.shortCode) {
      this.sendDisabled = false;
    } else {
      super.canSend();
    }

  }

  override postStream(messages: any, currentIndex: number) {
    // 开始新一轮问答前，先清空追问问题
    this.dialogHistory = this.dialogHistory.map((conversation) =>
      conversation.filter((item) => item.role !== "followUpQuestions")
    );
    this.sensitiveFlag = false;
    this.isRequesting = true;
    this.isLoading = true;
    this.isShowStopIcon = true;
    this.isTimeoutOrError = false;
    this.isStopped = false;
    this.isUserScrolling = false;
    messages.model_deployment_id = this.deploymentId ?? "";
    if (this.configServ.isSupportUserPersona() && this.configServ.isSupportMemoryInSingleAgent()) {
      messages.long_term_memory = { // 记忆提取配置
        enable_retrieve: this.conversationState.isRetrieve,
        enable_extract: this.conversationState.isExtract,
        memory_repo_id: this.memoryLibId
      };
    }
    if (this.shortCode) {
      this.sseInstance = this.appAgentServe.webPageChatSSE(
        messages,
        "agents",
        this.shortCode,
        this.uuid,
        "PUBLISHED",
        this.lang,
        this.getPlanSSECallbacks(currentIndex, false),
        "plan"
      );
    } else {
      this.sseInstance = this.appAgentServe.knowledgeChatSSE(
        messages,
        this.appId,
        this.uuid,
        "DEBUG",
        this.lang,
        this.getPlanSSECallbacks(currentIndex, false),
        this.versionId,
        "agents",
        false,
        "plan"
      );
    }
  }

  private getPlanSSECallbacks(currentIndex: number, isRegenerate: boolean) {
    return {
      onMessage: (token: any) => {
        try {
          const chunkDataObj =
            token.data && flowCommonLogic.stringToObject(token.data);
          this.handleMessage(chunkDataObj, currentIndex);
        } catch (error) {
        }
        this.scrollToBottom();
        if (this.isRequesting) {
          this.isShowStopIcon = true;
        }
        this.cdr.markForCheck();
      },
      onDone: () => {
        this.isRequesting = false;
        this.isLoading = false;
        if (this.enable) {
          this.getNextQuestions(currentIndex);
        } else {
          this.isRecentQuestionVisible = false;
        }
        this.isShowStopIcon = false;
        this.showMemo = true;
        const hasHistory =
          this.dialogHistory.length > 0 &&
          this.dialogHistory[0].some((item) => item.role === "assistant");
        this.dialogHistory[currentIndex][0].isFinished = true;
        this.agentDataServe.setHasQAFlag(hasHistory);
        this.scrollToBottom();
        this.cdr.markForCheck();
      },
      onError: (error: any) => {
        this.dialogHistory[currentIndex][0].reqErrorStatus = String(error?.source?.xhr?.status);
        // 对话运行失败时不能点赞踩的标识
        this.dialogHistory[currentIndex][0].isError = true;
        this.isRequesting = false;
        this.isLoading = false;
        this.isShowStopIcon = false;
        this.isTimeoutOrError = true;
        if (error.data) {
          try {
            const errInfo = JSON.parse(error.data);
            const apiError = errInfo?.error_msg
              ? this.i18n.transform("ApiError", {
                errCode: errInfo.error_code,
                reason: errInfo.error_msg
              })
              : "";
            this.msgServ.error(apiError, { nzDuration: 3000 });
          } catch {
            this.msgServ.error(this.i18n.transform("NetErrorTips"), { nzDuration: 3000 });
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
        this.msgServ.error(this.i18n.transform("streaming_timeout"), { nzDuration: 3000 });
        this.scrollToBottom();
        this.cdr.markForCheck();
      }
    };
  }

  handleMessage(chunkDataObj: any, currentIndex: number): void {
    try {
      const { event, data, latency, content, createdTime, reasoning_content } = chunkDataObj || {};
      const effectiveContent = content || (data && data.text);
      const effectiveReasoningContent = reasoning_content || (data && data.reasoning_content);
      switch (event) {
        case "scene_match":
          this.handleSceneMatch(data, currentIndex);
          break;
        case "plan_end":
          this.handlePlanEnd(data, currentIndex);
          break;
        case "step_start":
          this.handleStepStart(data, currentIndex);
          break;
        case "step_end":
          this.handleStepEnd(data, currentIndex);
          break;
        case "plugin_start":
          this.handlePluginStart(chunkDataObj, currentIndex);
          break;
        case "plugin_end":
          this.handlePluginEnd(chunkDataObj, currentIndex);
          break;
        case "task_complete":
          this.handleTaskComplete(currentIndex);
          break;
        case "message":
          this.handleMessageEvent(effectiveContent, currentIndex, chunkDataObj, effectiveReasoningContent);
          break;
        case "agent_interrupted":
          this.handleAgentInterrupted(currentIndex);
          break;
        case "statistic_data":
          this.handleStatisticData(latency, currentIndex);
          break;
        case "summary_response":
          this.handleSummaryResponse(content, createdTime, currentIndex, chunkDataObj);
          break;
        case "sensitive":
          this.handleSensitiveData(data, currentIndex);
          break;
        case "error":
          this.handleEventError(currentIndex);
          break;
        default:
          break;
      }
    } finally {
      this.isLoading = false;
    }
  }

  /**
   * 处理场景匹配事件
   */
  private handleSceneMatch(data: any, currentIndex: number): void {
    const sceneMessage: sceneMessageType = {
      plans: [{
        title: data?.scene_name ? this.i18n.transform("plan_model.tip_1") + data.scene_name : this.i18n.transform("plan_model.tip_4"),
        status: "finished"
      }, {
        id: "plan",
        title: this.i18n.transform("plan_model.tip_2"),
        status: "loading",
        steps: []
      }],
      role: "assistant",
      status: ""
    };

    this.dialogHistory[currentIndex] = [
      ...this.dialogHistory[currentIndex],
      sceneMessage
    ];
  }

  /**
   * 处理计划结束事件
   */
  private handlePlanEnd(data: any, currentIndex: number): void {
    if (data?.steps?.length) {
      const sceneMessage = this.getSceneMessage(currentIndex);
      if (sceneMessage) {
        const lastPlan = sceneMessage.plans[sceneMessage.plans.length - 1];
        lastPlan.steps = data.steps;
        lastPlan.status = "finished";
        this.dialogHistory[currentIndex] = [...this.dialogHistory[currentIndex]];
      }
    }
  }

  /**
   * 处理步骤开始事件
   */
  private handleStepStart(stepData: any, currentIndex: number): void {
    const sceneMessage = this.getSceneMessage(currentIndex);
    this.step_idx = stepData.step_idx;

    if (!stepData?.step_idx) return;
    if (stepData.step_idx === 1 && sceneMessage) {
      // 第一个步骤开始时创建新计划
      sceneMessage.plans.push({
        title: this.i18n.transform("plan_model.tip_3"),
        status: "loading",
        actionSteps: [
          { title: `1.${stepData?.step_name}`, actions: [] }
        ]
      });
    } else if (this.isNeedExtractNode(currentIndex)) {
      // 处理节点提取场景
      const extractSceneMessage = this.waitNode.sceneMessage;
      extractSceneMessage.plans.push({
        title: this.i18n.transform("plan_model.tip_3"),
        status: "loading",
        actionSteps: [
          { title: `1.${stepData?.step_name}`, actions: [] }
        ]
      });
      this.waitNode.index = -1;
      this.waitNode.sceneMessage = { role: "assistant", status: "loading", plans: [] };
      this.dialogHistory[currentIndex] = [
        ...this.dialogHistory[currentIndex],
        extractSceneMessage
      ];
    } else {
      // 添加到现有计划的步骤
      const lastPlan = sceneMessage.plans[sceneMessage.plans.length - 1];
      lastPlan.actionSteps.push({
        title: `${lastPlan.actionSteps.length + 1}.${stepData?.step_name}`,
        actions: []
      });
    }
  }

  /**
   * 处理步骤结束事件
   */
  private handleStepEnd(stepData: any, currentIndex: number): void {
    const sceneMessage = this.getSceneMessage(currentIndex);
    this.step_idx = stepData.step_idx;
    const lastPlan = sceneMessage.plans[sceneMessage.plans.length - 1];
    let action = lastPlan.actionSteps[lastPlan.actionSteps.length - 1];
    action.result_summary = stepData.result_summary;
    this.dialogHistory[currentIndex] = [...this.dialogHistory[currentIndex]];
  }

  /**
   * 处理插件开始事件
   */
  private handlePluginStart(chunkDataObj: any, currentIndex: number): void {
    let tool = { name: "" };
    let type = "plugin";
    if (this.isUseSkill(chunkDataObj.plugin)) {
      type = "skill";
      tool.name = this.getSkillName(chunkDataObj.plugin.arguments);
    } else {
      tool = this.handlePluginInfo(chunkDataObj.plugin.name, chunkDataObj.type);
    }
    this.handleToolOrNode(currentIndex, tool.name, type);
  }


  /**
   * 处理插件结束事件
   */
  private handlePluginEnd(chunkDataObj: any, currentIndex: number): void {
    // Plugin end event - no content display needed, just keep the "正在调用插件" title
  }

  /**
   * 处理任务完成事件
   */
  private handleTaskComplete(currentIndex: number): void {
    this.updateStatus(currentIndex, "finished", true);
  }

  /**
   * 处理消息事件
   */
  private handleMessageEvent(content: string, currentIndex: number, chunkDataObj: any, reasoning_content?: string): void {
    if (reasoning_content && reasoning_content !== "\n\n" && reasoning_content !== "\n") {
      this.handleReasoningContent(currentIndex, reasoning_content);
    }

    if (this.dialogHistory[currentIndex]?.[0]) {
      this.dialogHistory[currentIndex][0].isFinished = true;
    }

    const hasExistingMessage = this.dialogHistory[currentIndex].some(
      (item) => item.role === "assistant" && (item.event === "message" || item.event === "sensitive")
    );

    if (hasExistingMessage) {
      this.updateStatus(currentIndex, "finished", true);
      this.handleWorkFlowEvent(currentIndex, chunkDataObj);
      return;
    }

    this.updateStatus(currentIndex, "finished", true);
    if (content && content.trim()) {
      const sceneMessage = this.getSceneMessage(currentIndex);
      if (sceneMessage) {
        sceneMessage.content = content;
        this.dialogHistory[currentIndex] = [...this.dialogHistory[currentIndex]];
      } else {
        this.dialogHistory[currentIndex].push({
          role: "assistant",
          content: content,
          event: "message"
        });
      }
    }
    this.handleWorkFlowEvent(currentIndex, chunkDataObj);
  }

  private handleReasoningContent(currentIndex: number, reasoning_content: string): void {
    const sceneMessage = this.getSceneMessage(currentIndex);
    if (!sceneMessage) return;
    const lastPlan = sceneMessage.plans[sceneMessage.plans.length - 1];
    if (!lastPlan?.actionSteps?.length) return;
    let actions = this.getAtions(currentIndex);
    let lastAction = actions[actions.length - 1];
    if (lastAction && lastAction.actionType === "reasoning") {
      lastAction.content = lastAction.content + reasoning_content;
    } else {
      actions.push({
        title: this.i18n.transform(this.getActionTitle("reasoning")),
        actionType: "reasoning",
        content: reasoning_content
      });
    }
    this.dialogHistory[currentIndex] = [...this.dialogHistory[currentIndex]];
  }

  /**
   * 处理代理中断事件
   */
  private handleAgentInterrupted(currentIndex: number): void {
    this.waitNode.index = currentIndex;
  }

  /**
   * 处理统计数据事件
   */
  private handleStatisticData(latency: number, currentIndex: number): void {
    const sceneMessage = this.dialogHistory[currentIndex].find(
      (item) => item.role === "assistant" && item.event === "error"
    );

    if (sceneMessage) {
      sceneMessage.time_consumption = latency;
    } else {
      const newItem = { role: "assistant", time_consumption: latency };
      this.dialogHistory[currentIndex] = [
        ...this.dialogHistory[currentIndex],
        newItem
      ];
    }

    this.endThink(currentIndex);
  }

  /**
   * 处理敏感词检测事件
   */
  private handleSensitiveData(data: any, currentIndex: number): void {
    this.sensitiveFlag = true;
    const { offset, text } = data;

    if (text && text.trim()) {
      const sceneMessage = this.getSceneMessage(currentIndex);

      if (sceneMessage && sceneMessage.content !== undefined) {
        const currentContent = sceneMessage.content;
        const safeOffset = Math.min(offset, currentContent.length);
        sceneMessage.content = currentContent.slice(0, safeOffset) + (text ?? "");
        sceneMessage.isFallbackReply = true;
        this.dialogHistory[currentIndex] = [...this.dialogHistory[currentIndex]];
      } else {
        this.dialogHistory[currentIndex].push({
          role: "assistant",
          content: text,
          event: "sensitive",
          isFallbackReply: true
        });
      }
    }

    this.endThink(currentIndex);
  }

  /**
   * 处理总结响应事件
   */
  private handleSummaryResponse(content: string, createdTime: string, currentIndex: number, chunkDataObj: any): void {
    if (createdTime) {
      this.dialogHistory[currentIndex][0].messageId = createdTime;
    }

    const hasAssistant = this.dialogHistory[currentIndex].some(item => item.role === "assistant");
    const quoteList = [];
    const finalContent = this.sensitiveFlag ? this.getLastMessageContent(currentIndex) : content;

    const updatedItem = this.dialogHistory[currentIndex].map((item, index) => {
      if (item.role === "assistant" && (!item.content || index === this.dialogHistory[currentIndex].length - 1)) {
        return {
          event: "summary_response",
          ...item,
          content: finalContent,
          res: finalContent,
          quoteList,
          hasAssistant: true,
          executionId: chunkDataObj.executionId,
          tagNum: 0
        };
      }
      return item;
    });

    if (!hasAssistant) {
      updatedItem.push({
        role: "assistant",
        executionId: chunkDataObj.executionId,
        tagNum: 0,
        hasAssistant: false,
        content,
        quoteList
      });
    }

    this.dialogHistory = [
      ...this.dialogHistory.slice(0, currentIndex),
      updatedItem,
      ...this.dialogHistory.slice(currentIndex + 1)
    ];

    this.endThink(currentIndex);
  }

  /**
   * 处理错误事件
   */
  private handleEventError(currentIndex: number): void {
    const existingMessage = this.dialogHistory[currentIndex].find(
      (item) => item.role === "assistant" && !item.plans && !item.content
    );

    const errorMessage = this.i18n.transform("run_error");

    if (existingMessage) {
      existingMessage.event = "error";
      existingMessage.content = errorMessage;
    } else {
      const errorItem = {
        event: "error",
        role: "assistant",
        content: errorMessage
      };
      this.dialogHistory[currentIndex] = [
        ...this.dialogHistory[currentIndex],
        errorItem
      ];
    }

    this.updateStatus(currentIndex, "finished", true);
    if (this.dialogHistory[currentIndex]?.[0]) {
      this.dialogHistory[currentIndex][0].isError = true;
    }
  }

  /**
   * 获取最后一条消息的内容
   */
  private getLastMessageContent(currentIndex: number): string {
    const lastMessage = this.dialogHistory[currentIndex]
      ?.filter((item) => item.role === "assistant" && item.content)
      ?.pop();
    return lastMessage?.content || "";
  }

  public getSceneMessage(currentIndex) {
    let sceneMessage: sceneMessageType = this.dialogHistory[currentIndex].find(
      (item) => item.role === "assistant"
    );
    return sceneMessage;
  }

  public handleClickQuestion(questionItem: string) {
    this.sender.setContent(questionItem);
  }

  updateStatus(currentIndex, status, isScene?: boolean) {
    let sceneMessage = this.getSceneMessage(currentIndex);
    let plan = sceneMessage.plans[sceneMessage.plans.length - 1];
    plan.status = status;
    if (isScene) {
      sceneMessage.status = status;
    }
    this.dialogHistory[currentIndex] = [...this.dialogHistory[currentIndex]];
  }


  isNeedExtractNode(currentIndex) {
    return this.waitNode?.index >= 0 && currentIndex - this.waitNode?.index === 1;

  }

  handleWorkFlowEvent(currentIndex, chunkDataObj) {
    const {
      data
    } = chunkDataObj || {};
    if (data) {
      let { text, card, node_type, node_name, is_finished } = data;
      let hasAssistant = false;

      const updatedItem = this.dialogHistory[currentIndex].map(
        (item) => {
          return item;
        }
      );

      // 新增一个卡片消息
      if (node_type === "Card") {
        this.handleCards(currentIndex, data);
      }

      if (text && node_type !== "Input" && node_type !== "End" && node_type !== "Message" && !is_finished) {
        if (node_name) {
          this.handleToolOrNode(currentIndex, node_name, "workflow");
        }
        const hasSummary = this.updateSummary(updatedItem);
        updatedItem.push({
          event: "summary_response",
          role: "assistant",
          executionId: chunkDataObj.executionId,
          tagNum: 0,
          hasAssistant: hasAssistant,
          content: text,
          hasSummary: hasSummary
        });
      }

      if (!is_finished) {
        if (text && (node_type === "End" || node_type === "Message")) {
          if (this.hasEndNode(data)) {
            if (this.step_idx !== updatedItem[updatedItem.length - 1].step_idx) {
              updatedItem[updatedItem.length - 1].step_idx = this.step_idx;
              this.handleToolOrNodeAndContent(currentIndex, node_name, "workflow", node_type);
            }
            this.addContent(currentIndex, node_type, text);
          } else {
            if (node_name) {
              updatedItem[updatedItem.length - 1].step_idx = this.step_idx;
              this.handleToolOrNodeAndContent(currentIndex, node_name, "workflow", node_type);
            }
            this.addContent(currentIndex, node_type, text);
          }
        }
      }


      if (text && node_type === "Input") {
        const { inputs } = text && flowCommonLogic.stringToObject(text);
        this.inputList = inputs?.map((item) => {
          return {
            ...item,
            uniqueId: `${currentIndex}-${item.name}`
          };
        });
      }

      // 遇到最后一个node_type等于'Input'时，回答中出现表单
      if (node_type === "Input" && is_finished === true) {
        const hasSummary = this.updateSummary(updatedItem);
        updatedItem.push({
          event: "summary_response",
          role: "assistant",
          executionId: chunkDataObj.executionId,
          tagNum: 0,
          isConfirmed: "init",
          isShowInputParams: true,
          inputList: this.inputList,
          hasSummary: hasSummary
        });
        this.sendDisabled = true;
      }

      this.dialogHistory = [
        ...this.dialogHistory.slice(0, currentIndex),
        updatedItem,
        ...this.dialogHistory.slice(currentIndex + 1)
      ];
    }
  }

  updateSummary(updatedItem) {
    const hasSummary = updatedItem.find(item => {
      return item.event === "summary_response";
    });
    return !!hasSummary;
  }

  handleToolOrNode(currentIndex, name, type) {
    let actions = this.getAtions(currentIndex);
    actions.push({
      title: this.i18n.transform(this.getActionTitle(type), { name: name }),
      content: ""
    });
  }

  handleToolOrNodeAndContent(currentIndex, name, type, nodeType) {
    let actions = this.getAtions(currentIndex);
    actions.push({
      title: this.i18n.transform(this.getActionTitle(type), { name: name }),
      nodeType: nodeType,
      content: ""
    });
  }

  getActionTitle(type) {
    switch (type) {
      case "plugin":
        return "plan_model.action_1";
      case "skill":
        return "plan_model.action_3";
      case "reasoning":
        return "plan_model.action_4";
      default:
        return "plan_model.action_2";
    }
  }

  addContent(currentIndex, nodeType, content) {
    let actions = this.getAtions(currentIndex);
    let node = actions[actions.length - 1];
    if (nodeType === node.nodeType) {
      node.content = node.content + content;
    }
  }

  getAtions(currentIndex) {
    let sceneMessage = this.getSceneMessage(currentIndex);
    let plan = sceneMessage.plans[sceneMessage.plans.length - 1];
    let actionSteps = plan.actionSteps[plan.actionSteps.length - 1];
    let actions = actionSteps?.actions;
    return actions || [];
  }

  hasEndNode(data) {
    return data.index !== 0;
  }

  inputConfirm(event, currentIndex) {
    this.sendQuestion(
      event
    );
  }

  memoryConfirm(memoryData: IMemoryManagementData) {
    this.conversationState.isRetrieve = memoryData?.enable_retrieve ?? true;
    this.conversationState.isExtract = memoryData?.enable_extract ?? true;
  }

  handleCards(currentIndex, data) {
    let { card, is_finished } = data;
    const updatedItem = this.dialogHistory[currentIndex].map(
      (item) => {
        return item;
      }
    );
    let sceneMessage: SummaryResponse = {
      event: "summary_response",
      role: "assistant"
    };

    sceneMessage.isCard = true;
    //流会返回 两个card message

    if (!is_finished) {
      if (!sceneMessage?.cards) {
        sceneMessage.cards = [];
      }
      const is_card = sceneMessage.cards.find(
        (item) => item.node_id === data.node_id
      );
      if (!is_card) {
        sceneMessage.cards.push(data);
      }
      sceneMessage.cards.forEach((item, index) => {
        if (item.node_id === data.node_id) {
          if (card.context?.desc) {
            if (card.context.desc?.stream === "true") {
              sceneMessage.cards[index].desc =
                `${sceneMessage.cards[index].desc ?? ""}` +
                data.card.context.desc.answer;
            } else {
              sceneMessage.cards[index].desc =
                data.card.context.desc ?? "";
            }
          } else {
            sceneMessage.cards[index].desc =
              is_card?.card.context.desc ?? "";
          }

          if (card.context?.title) {
            if (card.context.title?.stream === "true") {
              sceneMessage.cards[index].title = `${
                sceneMessage.cards[index].title ?? ""
              }${data.card.context.title.answer}`;
            } else {
              sceneMessage.cards[index].title =
                data.card.context.title ?? "";
            }
          } else {
            sceneMessage.cards[index].title =
              is_card?.card.context.title ?? "";
          }
        }
      });
      const hasSummary = this.updateSummary(updatedItem);
      updatedItem.push({ ...sceneMessage, hasSummary: hasSummary });
    }
  }
}
