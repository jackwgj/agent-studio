import {
  Component,
  OnDestroy,
  OnInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TextFieldModule } from '@angular/cdk/text-field';
import { Subscription } from 'rxjs';
import { COMMON_MODULES, LIB_MODULES } from '@shared/modules';
import { ModelManagementService } from '@services/repositories/model-management-new';
import { ConversationWorkspaceService, SessionItem } from './conversation-workspace.service';

/** 消息段：message 输出段 / reasoning 思考段 / tool 工具轨迹（按轮持久化的行形态） */
interface ChatSegment {
  type: 'message' | 'reasoning' | 'tool';
  content: string;
  /** 工具名（仅 type=tool） */
  toolId?: string;
}

interface ChatMessage {
  role: 'user' | 'assistant';
  segments: ChatSegment[];
  userContent?: string;
  /** 非主 Agent 内容默认折叠，不主动展示 */
  detailSegments?: ChatSegment[];
  subAgents?: ChatMessage[];
  loading?: boolean;
  error?: boolean;
  subExecutionId?: string;
  agentId?: string;
  isSubAgent?: boolean;
}

@Component({
  selector: 'app-conversation-workspace',
  templateUrl: './conversation-workspace.component.html',
  styleUrls: ['./conversation-workspace.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [COMMON_MODULES, LIB_MODULES, TextFieldModule],
  standalone: true,
})
export class ConversationWorkspaceComponent implements OnInit, OnDestroy {
  currentSession: SessionItem | null = null;
  messages: ChatMessage[] = [];
  inputText = '';
  streaming = false;
  selectedModel = '';
  modelOptions: any[] = [];
  private sseInstance: any = null;
  private modelAbortController: AbortController | null = null;
  private subscriptions = new Subscription();

  constructor(
    private conversationWorkspaceService: ConversationWorkspaceService,
    private modelManagementService: ModelManagementService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadModels();
    this.subscribeToRoute();
    this.subscribeToActiveSession();
  }

  ngOnDestroy(): void {
    this.sseInstance?.close?.();
    this.modelAbortController?.abort();
    this.subscriptions.unsubscribe();
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
        this.currentSession = session;
        const id = session?.conversation_id;
        if (!id) {
          this.messages = [];
        } else if (!this.streaming) {
          this.messages = [];
          this.conversationWorkspaceService.detailSession(id).then((detail) => {
            this.messages = this.mapDetailToMessages(detail?.messages ?? []);
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
    const query = this.inputText.trim();
    if (!query || this.streaming || !this.selectedModel) {
      return;
    }
    if (!this.currentSession?.conversation_id) {
      this.conversationWorkspaceService
        .createSession({ title: this.currentSession?.title ?? '' })
        .then((session) => {
          this.currentSession = session;
          this.doRun(query);
          this.conversationWorkspaceService.setActiveSession(session);
          this.conversationWorkspaceService.refreshSessions();
        });
      return;
    }
    this.doRun(query);
  }

  /** Enter 发送（Shift+Enter 换行） */
  public onKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Enter' || event.shiftKey) {
      return;
    }
    event.preventDefault();
    this.send();
  }

  private doRun(query: string): void {
    this.inputText = '';
    const turn: ChatMessage = {
      role: 'assistant',
      userContent: query,
      segments: [],
      detailSegments: [],
      subAgents: [],
      loading: true,
    };
    this.messages.push(turn);
    const assistantMsg = turn;
    this.streaming = true;
    this.cdr.markForCheck();

    this.sseInstance = this.conversationWorkspaceService.chatSSE(
      this.currentSession!.conversation_id,
      {
        query,
        model_deployment_id: this.selectedModel,
      },
      {
        onMessage: (token: any) => this.handleMessage(token, assistantMsg),
        onDone: () => {
          assistantMsg.loading = false;
          this.streaming = false;
          this.conversationWorkspaceService.refreshSessions();
          this.cdr.markForCheck();
        },
        onError: () => {
          assistantMsg.loading = false;
          assistantMsg.error = true;
          this.streaming = false;
          this.cdr.markForCheck();
        },
        onTimeout: () => {
          assistantMsg.loading = false;
          assistantMsg.error = true;
          this.streaming = false;
          this.cdr.markForCheck();
        },
      },
    );
  }

  /**
   * 流式消息处理（按轮持久化协议）：
   * message/reasoning 增量按 subExecutionId 路由主/子气泡并追加到对应段；
   * sub_start 新建子 Agent 气泡；tool_call 建工具段、tool_result 回填结果；
   * sub_done/run_done 仅收尾（结束 loading，不替换内容——入库即流式聚合，"所见即所存"）；
   * error 标记失败；user_message/usage 忽略。
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
            this.appendVisibleOrDetailSegment(target, 'message', delta);
            target.loading = false;
          }
          break;
        }
        case 'reasoning': {
          const content = d.content ?? '';
          const target = d.subExecutionId
            ? (this.findSubBubble(d.subExecutionId) ?? assistantMsg)
            : assistantMsg;
          if (target && content) {
            this.appendDetailSegment(target, 'reasoning', content);
          }
          break;
        }
        case 'sub_start': {
          // 子 Agent 内容挂在当前主 Agent 交互框内，默认折叠
          const sub: ChatMessage = {
            role: 'assistant',
            segments: [],
            detailSegments: [],
            loading: true,
            subExecutionId: d.subExecutionId,
            agentId: d.agentId,
            isSubAgent: true,
          };
          assistantMsg.subAgents ??= [];
          assistantMsg.subAgents.push(sub);
          break;
        }
        case 'sub_done': {
          const sub = this.findSubBubble(d.subExecutionId);
          if (sub) {
            sub.loading = false; // 完成信号，不替换内容（入库即流式聚合）
          }
          break;
        }
        case 'run_done': {
          assistantMsg.loading = false;
          break;
        }
        case 'tool_call': {
          // 工具轨迹默认折叠，不主动暴露
          const target = d.subExecutionId
            ? (this.findSubBubble(d.subExecutionId) ?? assistantMsg)
            : assistantMsg;
          if (target && d.toolName) {
            target.detailSegments ??= [];
            target.detailSegments.push({ type: 'tool', content: '', toolId: d.toolName });
          }
          break;
        }
        case 'tool_result': {
          const target = d.subExecutionId
            ? (this.findSubBubble(d.subExecutionId) ?? assistantMsg)
            : assistantMsg;
          if (target) {
            const toolSeg = [...(target.detailSegments ?? [])].reverse().find((s) => s.type === 'tool');
            if (toolSeg) {
              toolSeg.content = d.result ?? '';
            }
          }
          break;
        }
        case 'error': {
          assistantMsg.error = true;
          assistantMsg.loading = false;
          break;
        }
        default:
          // user_message/run_start/usage 仅透传不渲染
          break;
      }
    } catch (e) {
      // 非 JSON 数据（如 [DONE]），忽略
    }
    this.cdr.markForCheck();
  }

  /** 追加主 Agent 可见输出；reasoning/tool 进入默认折叠详情。 */
  private appendVisibleOrDetailSegment(message: ChatMessage, type: 'message', delta: string): void {
    this.appendSegment(message, type, delta);
  }

  private appendSegment(message: ChatMessage, type: 'message' | 'reasoning', delta: string): void {
    const last = message.segments[message.segments.length - 1];
    if (last && last.type === type) {
      last.content += delta;
    } else {
      message.segments.push({ type, content: delta });
    }
  }

  private appendDetailSegment(message: ChatMessage, type: 'message' | 'reasoning', content: string): void {
    message.detailSegments ??= [];
    const last = message.detailSegments[message.detailSegments.length - 1];
    if (last && last.type === type) {
      last.content += content;
    } else {
      message.detailSegments.push({ type, content });
    }
  }

  /** 按 execution_id 恢复：每次用户交互生成一个独立框。 */
  private mapDetailToMessages(rows: any[]): ChatMessage[] {
    const turns = new Map<string, ChatMessage>();
    for (const row of rows ?? []) {
      const executionId = row.execution_id ?? `legacy-${row.created_at ?? Math.random()}`;
      let turn = turns.get(executionId);
      if (!turn) {
        turn = { role: 'assistant', segments: [], detailSegments: [], subAgents: [] };
        turns.set(executionId, turn);
      }
      if (row.role === 'user') {
        turn.userContent = row.content ?? '';
        continue;
      }
      const seg = this.rowToSegment(row);
      if (!seg) {
        continue;
      }
      if (row.sub_execution_id) {
        let sub = turn.subAgents!.find((item) => item.subExecutionId === row.sub_execution_id);
        if (!sub) {
          sub = {
            role: 'assistant',
            segments: [],
            detailSegments: [],
            subExecutionId: row.sub_execution_id,
            agentId: row.agent_id,
            isSubAgent: true,
          };
          turn.subAgents!.push(sub);
        }
        sub.segments.push(seg);
      } else if (seg.type === 'message') {
        turn.segments.push(seg);
      } else {
        turn.detailSegments!.push(seg);
      }
    }
    return Array.from(turns.values());
  }

  /** run 表 handoff 工具进入详情；主界面只展示 message。 */
  private rowToSegment(row: any): ChatSegment | null {
    if (row.role === 'tool') {
      return { type: 'tool', content: row.content ?? '', toolId: row.tool_id };
    }
    if (row.event === 'reasoning') {
      return { type: 'reasoning', content: row.content ?? '' };
    }
    if (row.event === 'message') {
      return { type: 'message', content: row.content ?? '' };
    }
    return null;
  }

  private findSubBubble(subExecutionId: string): ChatMessage | undefined {
    for (const message of this.messages) {
      const sub = message.subAgents?.find((item) => item.subExecutionId === subExecutionId);
      if (sub) {
        return sub;
      }
    }
    return undefined;
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
