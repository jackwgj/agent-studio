import {
  Component,
  OnDestroy,
  OnInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  ViewChild,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { COMMON_MODULES, LIB_MODULES } from '@shared/modules';
import { ModelManagementService } from '@services/repositories/model-management-new';
import { HttpService } from '@services/http.service';
import { ConversationSendRequest, ConversationSkillItem } from './conversation-skill.model';
import { ConversationWorkspaceService, SessionItem } from './conversation-workspace.service';
import { SkillSelectorComponent } from './skill-selector/skill-selector.component';

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  loading?: boolean;
  error?: boolean;
  /** 子 Agent 气泡归属（sub_execution_id，每次 handoff 唯一） */
  subExecutionId?: string;
  /** 子 Agent id（气泡标签用） */
  agentId?: string;
  /** 是否子 Agent 气泡（独立渲染，方案 B：子 Agent 只收监督者 query） */
  isSubAgent?: boolean;
}

interface ActivatedSkill {
  skillId: string;
  name: string;
  versionId: string;
}

@Component({
  selector: 'app-conversation-workspace',
  templateUrl: './conversation-workspace.component.html',
  styleUrls: ['./conversation-workspace.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [COMMON_MODULES, LIB_MODULES, SkillSelectorComponent],
  standalone: true,
})
export class ConversationWorkspaceComponent implements OnInit, OnDestroy {
  currentSession: SessionItem | null = null;
  messages: ChatMessage[] = [];
  inputText = '';
  streaming = false;
  selectedModel = '';
  modelOptions: any[] = [];
  skillCatalog: ConversationSkillItem[] = [];
  skillCatalogUnavailable = false;
  recommendedSkills: ConversationSkillItem[] = [];
  activatedSkills: ActivatedSkill[] = [];
  @ViewChild(SkillSelectorComponent) private skillSelector?: SkillSelectorComponent;
  private sseInstance: any = null;
  private modelAbortController: AbortController | null = null;
  private subscriptions = new Subscription();
  private workspaceId = '';
  private readonly workspaceChangeHandler = () => this.handleWorkspaceChange();

  constructor(
    private conversationWorkspaceService: ConversationWorkspaceService,
    private modelManagementService: ModelManagementService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private http: HttpService,
  ) {}

  ngOnInit(): void {
    this.workspaceId = this.http.getWorkspaceId();
    this.loadSkillCatalog();
    window.addEventListener('WorkspaceChange', this.workspaceChangeHandler);
    this.loadModels();
    this.subscribeToRoute();
    this.subscribeToActiveSession();
  }

  /** Skill 目录失败不影响普通对话。 */
  private loadSkillCatalog(): void {
    this.conversationWorkspaceService
      .listSkills()
      .then((skills) => {
        this.skillCatalog = skills;
        this.skillCatalogUnavailable = false;
        this.cdr.markForCheck();
      })
      .catch(() => {
        this.skillCatalog = [];
        this.skillCatalogUnavailable = true;
        this.cdr.markForCheck();
      });
  }

  ngOnDestroy(): void {
    this.sseInstance?.close?.();
    this.modelAbortController?.abort();
    this.subscriptions.unsubscribe();
    window.removeEventListener('WorkspaceChange', this.workspaceChangeHandler);
  }

  /** 模型列表（复用平台 getAvailableModelList） */
  private loadModels(): void {
    this.modelAbortController?.abort();
    this.modelAbortController = new AbortController();
    this.modelManagementService
      .getAvailableModelList(
        {
          groupby: 'provider',
          publish_status: 'online',
          model_type: 'LLM,IMAGE-TO-TEXT',
          with_router: false,
        },
        this.modelAbortController.signal,
      )
      .then((res) => {
        const options: any[] = [];
        res?.data?.forEach((provider: any) => {
          (provider.models ?? []).forEach((model: any) => {
            options.push({
              deployment_id: model.id,
              label: `${provider.provider_name || ''} / ${model.model_name || model.id}`,
            });
          });
        });
        this.modelOptions = options;
        this.selectedModel = options[0]?.deployment_id ?? '';
        this.cdr.markForCheck();
      })
      .catch(() => void 0);
  }

  /** 路由 queryParams：conversation_id 打开会话 / new 新建草稿 / 兜底新建草稿 */
  private subscribeToRoute(): void {
    this.subscriptions.add(
      this.route.queryParams.subscribe((params) => {
        if (params['conversation_id']) {
          this.openConversation(params['conversation_id']);
        } else if (params['new']) {
          this.conversationWorkspaceService.newDraftSession();
        } else if (!this.conversationWorkspaceService.activeSession$.value) {
          this.conversationWorkspaceService.newDraftSession();
        }
      }),
    );
  }

  /** 订阅当前会话：有真实 id 拉取消息，草稿清空（流式中跳过，避免覆盖正在发送的气泡） */
  private subscribeToActiveSession(): void {
    this.subscriptions.add(
      this.conversationWorkspaceService.activeSession$.subscribe((session) => {
        this.syncWorkspaceFromActiveSession();
        if (!this.streaming) {
          this.clearSkillRoundState();
        }
        this.currentSession = session;
        const id = session?.conversation_id;
        if (!id) {
          this.messages = [];
        } else if (!this.streaming) {
          this.messages = [];
          this.conversationWorkspaceService.detailSession(id).then((detail) => {
            this.messages = (detail?.messages ?? []).map((msg: any) => ({
              role: msg.role === 'user' ? 'user' : 'assistant',
              content: msg.content ?? '',
              // 子 Agent 消息（t_conversation_sub_run 行）恢复为独立气泡；detail API 字段是下划线（MessageVo）
              ...(msg.sub_execution_id
                ? { subExecutionId: msg.sub_execution_id, agentId: msg.agent_id, isSubAgent: true }
                : {}),
            }));
            if (!this.currentSession?.title) {
              this.currentSession = { ...this.currentSession!, title: detail?.title ?? '' };
            }
            this.cdr.markForCheck();
          });
        }
        this.cdr.markForCheck();
      }),
    );
  }

  /** 打开会话（列表命中直接用；深链则先占位、详情加载后补标题） */
  private openConversation(id: string): void {
    if (this.streaming) {
      return;
    }
    const existing = this.conversationWorkspaceService.sessions$.value.find(
      (s) => s.conversation_id === id,
    );
    this.conversationWorkspaceService.setActiveSession(
      existing ?? { conversation_id: id, title: '', status: 'ACTIVE' },
    );
  }

  /** 发送消息（多轮对话入口；草稿先落库，仅首次真正交互才创建会话） */
  public send(): void {
    const inputSnapshot = this.inputText;
    const query = inputSnapshot.trim();
    const recommendationSnapshot = [...this.recommendedSkills];
    if (!query || this.streaming || !this.selectedModel) {
      return;
    }
    if (!this.currentSession?.conversation_id) {
      this.conversationWorkspaceService
        .createSession({ title: this.currentSession?.title ?? '' })
        .then((session) => {
          this.currentSession = session;
          this.doRun(query, inputSnapshot, recommendationSnapshot);
          this.conversationWorkspaceService.setActiveSession(session);
          this.conversationWorkspaceService.refreshSessions();
        });
      return;
    }
    this.doRun(query, inputSnapshot, recommendationSnapshot);
  }

  /** Enter 发送（Shift+Enter 换行） */
  public onKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Enter' || event.shiftKey) {
      return;
    }
    event.preventDefault();
    this.send();
  }

  private doRun(
    query: string,
    inputSnapshot: string,
    recommendationSnapshot: ConversationSkillItem[],
  ): void {
    this.activatedSkills = [];
    this.messages.push({ role: 'user', content: query });
    const assistantMsg: ChatMessage = { role: 'assistant', content: '', loading: true };
    this.messages.push(assistantMsg);
    this.streaming = true;
    let streamOpened = false;
    this.cdr.markForCheck();

    this.sseInstance = this.conversationWorkspaceService.chatSSE(
      this.currentSession!.conversation_id,
      {
        query,
        model_deployment_id: this.selectedModel,
        recommended_skill_ids: recommendationSnapshot.map((item) => item.skillId),
      } as ConversationSendRequest,
      {
        onOpen: () => {
          streamOpened = true;
          this.inputText = '';
          this.recommendedSkills = [];
          this.skillSelector?.clearRecommendations();
          this.cdr.markForCheck();
        },
        onMessage: (token: any) => this.handleMessage(token, assistantMsg),
        onDone: () => {
          assistantMsg.loading = false;
          this.streaming = false;
          this.conversationWorkspaceService.refreshSessions();
          this.cdr.markForCheck();
        },
        onError: () => {
          this.handleRunFailure(assistantMsg, streamOpened, inputSnapshot, recommendationSnapshot);
        },
        onTimeout: () => {
          this.handleRunFailure(assistantMsg, streamOpened, inputSnapshot, recommendationSnapshot);
        },
      },
    );
  }

  private handleRunFailure(
    assistantMsg: ChatMessage,
    streamOpened: boolean,
    inputSnapshot: string,
    recommendationSnapshot: ConversationSkillItem[],
  ): void {
    assistantMsg.loading = false;
    assistantMsg.error = true;
    this.streaming = false;
    if (!streamOpened) {
      this.inputText = inputSnapshot;
      this.recommendedSkills = recommendationSnapshot;
      this.loadSkillCatalog();
    }
    this.cdr.markForCheck();
  }

  /**
   * 流式消息处理（团队新协议）：
   * message 增量取 data.delta（按 subExecutionId 路由主/子气泡）；sub_start 新建子 Agent 气泡、
   * sub_done/run_done 权威完整文本收尾；tool_call 折叠展示；reasoning/user_message/usage 忽略（MVP）。
   */
  private handleMessage(token: any, assistantMsg: ChatMessage): void {
    try {
      const { event, data } = JSON.parse(token.data);
      const d = data ?? {};
      switch (event) {
        case 'message': {
          const delta = d.delta ?? '';
          const target = d.subExecutionId
            ? (this.findSubBubble(d.subExecutionId) ?? assistantMsg)
            : assistantMsg;
          if (target && delta) {
            target.content += delta;
            target.loading = false;
          }
          break;
        }
        case 'sub_start': {
          // 新子 Agent 气泡（按 sub_execution_id 独立）
          const sub: ChatMessage = {
            role: 'assistant',
            content: '',
            loading: true,
            subExecutionId: d.subExecutionId,
            agentId: d.agentId,
            isSubAgent: true,
          };
          this.messages.push(sub);
          break;
        }
        case 'sub_done': {
          const sub = this.findSubBubble(d.subExecutionId);
          if (sub && d.text) {
            sub.content = d.text; // 权威完整文本（整句）
            sub.loading = false;
          }
          break;
        }
        case 'tool_call': {
          // 工具调用折叠展示（附到当前上下文气泡）
          const target = d.subExecutionId
            ? (this.findSubBubble(d.subExecutionId) ?? assistantMsg)
            : assistantMsg;
          if (target && d.toolName) {
            target.content += `\n[工具调用] ${d.toolName}`;
          }
          break;
        }
        case 'run_done': {
          if (d.text) {
            assistantMsg.content = d.text; // 权威完整文本（整句）
          }
          assistantMsg.loading = false;
          break;
        }
        case 'error': {
          assistantMsg.content = assistantMsg.content || '运行出错，请稍后重试';
          assistantMsg.error = true;
          assistantMsg.loading = false;
          break;
        }
        case 'skill_activated': {
          if (!this.activatedSkills.some((item) => item.skillId === d.skillId && item.versionId === d.versionId)) {
            this.activatedSkills.push({ skillId: d.skillId, name: d.name, versionId: d.versionId });
          }
          break;
        }
        default:
          // user_message/run_start/reasoning/usage/tool_result 忽略（reasoning 可选展示，MVP 不展示）
          break;
      }
    } catch (e) {
      // 非 JSON 数据（如 [DONE]），忽略
    }
    this.cdr.markForCheck();
  }

  /** 按 sub_execution_id 查找子 Agent 气泡 */
  private findSubBubble(subExecutionId: string): ChatMessage | undefined {
    return this.messages.find((m) => m.subExecutionId === subExecutionId);
  }

  private clearSkillRoundState(): void {
    this.recommendedSkills = [];
    this.activatedSkills = [];
    this.skillSelector?.clearRecommendations();
  }

  private handleWorkspaceChange(): void {
    this.workspaceId = this.http.getWorkspaceId();
    this.clearSkillRoundState();
    this.loadSkillCatalog();
    this.cdr.markForCheck();
  }

  private syncWorkspaceFromActiveSession(): void {
    const workspaceId = this.http.getWorkspaceId();
    if (workspaceId === this.workspaceId) {
      return;
    }
    this.workspaceId = workspaceId;
    this.clearSkillRoundState();
    this.loadSkillCatalog();
  }

  /** 气泡角色标签：我 / 助手 / 子Agent·{agentId前8位} */
  public roleLabel(message: ChatMessage): string {
    if (message.role === 'user') {
      return '我';
    }
    if (message.isSubAgent) {
      return `子Agent${message.agentId ? '·' + message.agentId.slice(0, 8) : ''}`;
    }
    return '助手';
  }
}
