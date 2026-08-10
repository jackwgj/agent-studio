import {
  Component,
  OnDestroy,
  OnInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from '@angular/core';
import { COMMON_MODULES, LIB_MODULES } from '@shared/modules';
import { ModelManagementService } from '@services/repositories/model-management-new';
import { ConversationWorkspaceService } from './conversation-workspace.service';

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  loading?: boolean;
  error?: boolean;
}

interface SessionItem {
  conversation_id: string;
  title: string;
  status: string;
  updated_at?: string;
}

@Component({
  selector: 'app-conversation-workspace',
  templateUrl: './conversation-workspace.component.html',
  styleUrls: ['./conversation-workspace.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [COMMON_MODULES, LIB_MODULES],
  standalone: true,
})
export class ConversationWorkspaceComponent implements OnInit, OnDestroy {
  sessions: SessionItem[] = [];
  currentSession: SessionItem | null = null;
  messages: ChatMessage[] = [];
  inputText = '';
  streaming = false;
  selectedModel = '';
  modelOptions: any[] = [];
  private sseInstance: any = null;
  private modelAbortController: AbortController | null = null;

  constructor(
    private conversationWorkspaceService: ConversationWorkspaceService,
    private modelManagementService: ModelManagementService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadModels();
    this.refreshSessions();
  }

  ngOnDestroy(): void {
    this.sseInstance?.close?.();
    this.modelAbortController?.abort();
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

  /** 刷新历史栏（updated_on 倒序） */
  private refreshSessions(): void {
    this.conversationWorkspaceService.listSessions(0, 100).then((res) => {
      this.sessions = res?.items ?? [];
      this.cdr.markForCheck();
    });
  }

  /** 新建会话 */
  public newSession(): void {
    if (this.streaming) {
      return;
    }
    this.conversationWorkspaceService.createSession({}).then((session) => {
      this.currentSession = session;
      this.messages = [];
      this.refreshSessions();
      this.cdr.markForCheck();
    });
  }

  /** 恢复会话（加载全部消息） */
  public selectSession(session: SessionItem): void {
    if (this.streaming) {
      return;
    }
    this.currentSession = session;
    this.conversationWorkspaceService
      .detailSession(session.conversation_id)
      .then((detail) => {
        this.messages = (detail?.messages ?? []).map((msg: any) => ({
          role: msg.role === 'user' ? 'user' : 'assistant',
          content: msg.content ?? '',
        }));
        this.cdr.markForCheck();
      });
  }

  /** 删除会话 */
  public deleteSession(session: SessionItem, event: Event): void {
    event.stopPropagation();
    this.conversationWorkspaceService.deleteSession(session.conversation_id).then(() => {
      if (this.currentSession?.conversation_id === session.conversation_id) {
        this.currentSession = null;
        this.messages = [];
      }
      this.refreshSessions();
    });
  }

  /** 发送消息（多轮对话入口） */
  public send(): void {
    const query = this.inputText.trim();
    if (!query || this.streaming) {
      return;
    }
    if (!this.currentSession) {
      // 无会话时先创建
      this.conversationWorkspaceService.createSession({}).then((session) => {
        this.currentSession = session;
        this.doRun(query);
        this.refreshSessions();
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
    this.messages.push({ role: 'user', content: query });
    const assistantMsg: ChatMessage = { role: 'assistant', content: '', loading: true };
    this.messages.push(assistantMsg);
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
          this.refreshSessions();
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

  /** 流式消息处理（与平台 webPageChatSSE 消费端一致的事件解析） */
  private handleMessage(token: any, assistantMsg: ChatMessage): void {
    try {
      const { event, content, data } = JSON.parse(token.data);
      if (event === 'message') {
        assistantMsg.content += content ?? data?.text ?? '';
        assistantMsg.loading = false;
      } else if (event === 'summary_response') {
        if (!assistantMsg.content) {
          assistantMsg.content += content ?? data?.answer ?? '';
        }
      } else if (event === 'plugin_end') {
        // 工具调用结果：折叠展示（内容并入正文避免丢失）
        const toolContent = content ?? data?.content;
        if (toolContent && typeof toolContent === 'string' && toolContent.trim()) {
          assistantMsg.content += `\n[工具调用结果] ${toolContent}`;
        }
      } else if (event === 'error') {
        assistantMsg.content = assistantMsg.content || '运行出错，请稍后重试';
        assistantMsg.error = true;
        assistantMsg.loading = false;
      }
    } catch (e) {
      // 非 JSON 数据（如 [DONE]），忽略
    }
    this.cdr.markForCheck();
  }
}
