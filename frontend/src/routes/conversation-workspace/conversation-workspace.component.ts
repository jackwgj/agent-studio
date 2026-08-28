import {
  Component,
  Inject,
  OnDestroy,
  OnInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { COMMON_MODULES, LIB_MODULES } from '@shared/modules';
import { ModelManagementService } from '@services/repositories/model-management-new';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { ApplicationType } from '@enums/agent-center.enum';
import { HttpService } from '@services/http.service';
import {
  ConversationExecutionTarget,
  ConversationSendRequest,
  ConversationSkillItem,
  ConversationFileReference,
  ConversationArtifactReference,
  ConversationEvent,
  ConversationEventType,
  ConversationRunNode,
  ChatSegment,
} from './conversation-skill.model';
import {
  ActiveSessionState,
  ConversationWorkspaceService,
  SessionItem,
  SessionListState,
} from './conversation-workspace.service';
import { SkillSelectorComponent } from './skill-selector/skill-selector.component';
import { AppMarkdownAnswerComponent } from '@shared/components/app-markdown-answer/app-markdown-answer.component';
import { UploadFileIconComponent } from '@shared/components/upload-file-icon/upload-file-icon.component';
import { v4 as uuidV4 } from 'uuid';
import { CommonUtils } from 'src/utils/common.util';

interface ChatMessage {
  role: 'user' | 'assistant';
  segments: ChatSegment[];
  userContent?: string;
  userFiles?: Array<Pick<ConversationFileReference, 'fileName'>>;
  artifacts?: ConversationArtifactReference[];
  /** 非主 Agent 内容默认折叠，不主动展示 */
  detailSegments?: ChatSegment[];
  runs: ConversationRunNode[];
  loading?: boolean;
  error?: boolean;
}

interface ActivatedSkill {
  skillId: string;
  name: string;
  versionId: string;
}

interface ConversationAttempt {
  id: number;
  workspaceId: string;
  initialSessionId: string;
  conversationId?: string;
  query: string;
  inputSnapshot: string;
  recommendationSnapshot: ConversationSkillItem[];
  phase: 'creating' | 'running';
  opened: boolean;
  settled: boolean;
  source?: { close?: () => void };
  assistantMsg?: ChatMessage;
}

interface PendingRouteConversation {
  conversationId: string;
  workspaceId: string;
}

interface PendingActiveSession {
  session: SessionItem;
  workspaceId: string;
}

@Component({
  selector: 'app-conversation-workspace',
  templateUrl: './conversation-workspace.component.html',
  styleUrls: ['./conversation-workspace.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [COMMON_MODULES, LIB_MODULES, SkillSelectorComponent, AppMarkdownAnswerComponent, UploadFileIconComponent],
  standalone: true,
})
export class ConversationWorkspaceComponent implements OnInit, OnDestroy {
  currentSession: SessionItem | null = null;
  messages: ChatMessage[] = [];
  inputText = '';
  streaming = false;
  selectedModel = '';
  modelOptions: any[] = [];
  executionTargets: ConversationExecutionTarget[] = [
    { id: 'default-team', name: '默认团队', type: 'SUPERVISOR' },
  ];
  selectedExecutionTarget = 'default-team';
  selectedExecutionPath: string[] = ['default-team'];
  executionTargetOptions: any[] = [
    { value: 'default-team', label: '默认团队', isLeaf: true },
    { value: 'digital-employees', label: '数字员工', children: [] },
    { value: 'digital-squads', label: '数字小队', children: [] },
  ];
  skillCatalog: ConversationSkillItem[] = [];
  skillCatalogUnavailable = false;
  recommendedSkills: ConversationSkillItem[] = [];
  activatedSkills: ActivatedSkill[] = [];
  uploadedFiles: ConversationFileReference[] = [];
  readonly acceptedFileTypes = '.txt,.md,.json,.xml,.html,.docx,.pdf,.xlsx,.xls,.csv';
  private readonly maxFiles = 10;
  private readonly maxFileSize = 60 * 1024 * 1024;
  @ViewChild('fileInput') private fileInput?: ElementRef<HTMLInputElement>;
  @ViewChild('messageList') private messageList?: ElementRef<HTMLElement>;
  @ViewChild(SkillSelectorComponent) private skillSelector?: SkillSelectorComponent;
  private modelAbortController: AbortController | null = null;
  private subscriptions = new Subscription();
  private workspaceId = '';
  private catalogRequestId = 0;
  private detailRequestId = 0;
  private nextAttemptId = 0;
  private activeAttempt: ConversationAttempt | null = null;
  private destroyed = false;
  private readonly workspaceRouteProvenanceKey = 'conversation-workspace-route-workspace';
  private workspaceRouteProvenance = '';
  private sessionListState: SessionListState = { workspaceId: '', generation: 0, sessions: [] };
  private pendingRouteConversation: PendingRouteConversation | null = null;
  private pendingActiveSession: PendingActiveSession | null = null;
  private documentMinWidthBeforeWorkspace: string | null = null;
  private historyScrollFrameIds: number[] = [];
  private readonly workspaceChangeHandler = () => this.handleWorkspaceChange();

  constructor(
    @Inject(DOCUMENT) private document: Document,
    private conversationWorkspaceService: ConversationWorkspaceService,
    private modelManagementService: ModelManagementService,
    private appAgentRepoService: AppAgentRepoService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private http: HttpService,
  ) {}

  ngOnInit(): void {
    this.enableResponsiveDocumentWidth();
    this.workspaceId = this.http.getWorkspaceId();
    this.workspaceRouteProvenance = sessionStorage.getItem(this.workspaceRouteProvenanceKey) ?? '';
    this.loadSkillCatalog();
    window.addEventListener('WorkspaceChange', this.workspaceChangeHandler);
    this.loadModels();
    this.loadExecutionTargets();
    this.subscribeToSessionListState();
    this.subscribeToRoute();
    this.subscribeToActiveSession();
  }

  /** Skill 目录失败不影响普通对话。 */
  private loadSkillCatalog(preserveCatalogOnFailure = false): void {
    const requestWorkspaceId = this.workspaceId;
    const requestId = ++this.catalogRequestId;
    this.conversationWorkspaceService
      .listSkills()
      .then((skills) => {
        if (!this.isCurrentCatalogRequest(requestWorkspaceId, requestId)) {
          return;
        }
        this.skillCatalog = skills;
        this.skillCatalogUnavailable = false;
        this.cdr.markForCheck();
      })
      .catch(() => {
        if (!this.isCurrentCatalogRequest(requestWorkspaceId, requestId)) {
          return;
        }
        if (!preserveCatalogOnFailure) {
          this.skillCatalog = [];
        }
        this.skillCatalogUnavailable = true;
        this.cdr.markForCheck();
      });
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.cancelHistoryScrollRestore();
    this.restoreDocumentMinWidth();
    this.clearPendingRouteConversation();
    this.clearPendingActiveSession();
    this.invalidateCatalogRequests();
    this.invalidateDetailRequests();
    try {
      this.cancelActiveAttempt();
    } finally {
      try {
        this.modelAbortController?.abort();
      } finally {
        try {
          this.subscriptions.unsubscribe();
        } finally {
          window.removeEventListener('WorkspaceChange', this.workspaceChangeHandler);
        }
      }
    }
  }

  /** 官方控制台全局固定了 html 最小宽度；仅在工作台存活期间解除，离开后完整恢复。 */
  private enableResponsiveDocumentWidth(): void {
    if (this.documentMinWidthBeforeWorkspace !== null) {
      return;
    }
    const documentElement = this.document.documentElement;
    this.documentMinWidthBeforeWorkspace = documentElement.style.minWidth;
    documentElement.style.minWidth = '0px';
  }

  private restoreDocumentMinWidth(): void {
    if (this.documentMinWidthBeforeWorkspace === null) {
      return;
    }
    const documentElement = this.document.documentElement;
    if (this.documentMinWidthBeforeWorkspace) {
      documentElement.style.minWidth = this.documentMinWidthBeforeWorkspace;
    } else {
      documentElement.style.removeProperty('min-width');
    }
    this.documentMinWidthBeforeWorkspace = null;
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
        if (this.destroyed) {
          return;
        }
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

  /** 复用 AgentCenter 现有资源接口，映射工作台执行目标。
   *  该接口 type 为精确匹配（不支持逗号多值），故按单/多智能体各查一次再合并去重。 */
  private loadExecutionTargets(): void {
    Promise.all([
      this.appAgentRepoService.getAgentList(
        { type: ApplicationType.SINGLE_AGENT, status: 'published' },
        0,
        100,
      ),
      this.appAgentRepoService.getAgentList(
        { type: ApplicationType.MULTI_AGENT, status: 'published' },
        0,
        100,
      ),
    ])
      .then(([single, multi]) => {
        if (this.destroyed) {
          return;
        }
        const seen = new Set<string>();
        const targets: ConversationExecutionTarget[] = [];
        [single, multi].forEach((res) => {
          (res?.agent_list ?? []).forEach((agent: any) => {
            if (
              ![ApplicationType.SINGLE_AGENT, ApplicationType.MULTI_AGENT].includes(agent.type)
            ) {
              return;
            }
            const id = String(agent.id ?? agent.agent_id);
            const name = agent.name ?? agent.agent_name ?? id;
            if (!id || !name || seen.has(id)) {
              return;
            }
            seen.add(id);
            targets.push({
              id,
              name,
              type: agent.type === ApplicationType.MULTI_AGENT ? 'MULTI_AGENT' : 'SINGLE_AGENT',
            });
          });
        });
        this.executionTargets = [
          { id: 'default-team', name: '默认团队', type: 'SUPERVISOR' },
          ...targets,
        ];
        this.executionTargetOptions = [
          { value: 'default-team', label: '默认团队', isLeaf: true },
          {
            value: 'digital-employees',
            label: '数字员工',
            children: targets
              .filter((target) => target.type === 'SINGLE_AGENT')
              .map((target) => ({ value: target.id, label: target.name, isLeaf: true })),
          },
          {
            value: 'digital-squads',
            label: '数字小队',
            children: targets
              .filter((target) => target.type === 'MULTI_AGENT')
              .map((target) => ({ value: target.id, label: target.name, isLeaf: true })),
          },
        ];
        this.cdr.markForCheck();
      })
      .catch(() => void 0);
  }

  /** 路由 queryParams：conversation_id 打开会话 / new 新建草稿 / 兜底新建草稿 */
  private subscribeToRoute(): void {
    this.subscriptions.add(
      this.route.queryParams.subscribe((params) => {
        if (params['conversation_id']) {
          this.handleConversationRoute(params['conversation_id'], params['workspace_id']);
        } else if (params['new']) {
          this.clearPendingRouteConversation();
          this.conversationWorkspaceService.newDraftSession();
        } else if (!this.conversationWorkspaceService.activeSession$.value) {
          this.clearPendingRouteConversation();
          this.conversationWorkspaceService.newDraftSession();
        }
      }),
    );
  }

  /** 带 workspace 归属的共享会话列表是无 workspace_id 历史路由的唯一证明。 */
  private subscribeToSessionListState(): void {
    this.subscriptions.add(
      this.conversationWorkspaceService.sessionListState$.subscribe((state) => {
        this.sessionListState = state;
        this.resolvePendingRouteConversation();
        this.resolvePendingActiveSession();
      }),
    );
  }

  /** 订阅带 workspace 归属的当前会话：旧空间残留须先由列表证明。 */
  private subscribeToActiveSession(): void {
    this.subscriptions.add(
      this.conversationWorkspaceService.activeSessionState$.subscribe((state) => {
        const workspaceChanged = this.transitionWorkspace(this.http.getWorkspaceId());
        if (!this.isCurrentWorkspaceActiveState(state)) {
          this.queuePendingActiveSession(state.session);
          return;
        }
        this.clearPendingActiveSession();
        this.clearPendingRouteConversation();
        this.applyActiveSession(state.session, workspaceChanged);
      }),
    );
  }

  private applyActiveSession(session: SessionItem | null, workspaceChanged: boolean): void {
    if (this.isDraftPromotion(session)) {
      this.currentSession = session;
      this.cdr.markForCheck();
      return;
    }
    if (!workspaceChanged && this.isSameConversation(session, this.currentSession)) {
      this.currentSession = session;
      this.cdr.markForCheck();
      return;
    }
    this.cancelActiveAttempt();
    this.invalidateDetailRequests();
    this.clearSkillRoundState();
    this.currentSession = session;
    const id = session?.conversation_id;
    if (!id) {
      this.messages = [];
    } else {
      this.messages = [];
      this.loadSessionDetail(id);
    }
    this.cdr.markForCheck();
  }

  /** 打开会话（列表命中直接用；深链则先占位、详情加载后补标题） */
  private openConversation(id: string): void {
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
    const target = this.executionTargets.find((item) => item.id === this.selectedExecutionTarget);
    if (!query || this.isSending || !target || (target.type === 'SUPERVISOR' && !this.selectedModel)) {
      return;
    }
    if (!this.currentSession?.conversation_id) {
      this.clearPendingRouteConversation();
      this.clearPendingActiveSession();
    }
    const attempt = this.createAttempt(query, inputSnapshot, recommendationSnapshot);
    if (!this.currentSession?.conversation_id) {
      this.conversationWorkspaceService
        .createSession({ title: this.currentSession?.title ?? '' })
        .then((session) => {
          if (!this.isCurrentAttempt(attempt) || attempt.phase !== 'creating') {
            return;
          }
          attempt.conversationId = session.conversation_id;
          this.currentSession = session;
          this.conversationWorkspaceService.setActiveSession(session);
          this.conversationWorkspaceService.refreshSessions();
          this.startRun(attempt, session.conversation_id);
        })
        .catch(() => {
          if (this.isCurrentAttempt(attempt) && attempt.phase === 'creating') {
            this.releaseAttempt(attempt);
          }
        });
      return;
    }
    attempt.conversationId = this.currentSession.conversation_id;
    this.startRun(attempt, attempt.conversationId);
  }

  /** Enter 发送（Shift+Enter 换行） */
  public onKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Enter' || event.shiftKey) {
      return;
    }
    event.preventDefault();
    this.send();
  }

  public openFilePicker(): void {
    if (this.isSending || this.uploadedFiles.length >= this.maxFiles) {
      return;
    }
    this.fileInput?.nativeElement.click();
  }

  public onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    input.value = '';
    const remaining = this.maxFiles - this.uploadedFiles.length;
    files.slice(0, remaining).forEach((file) => this.uploadFile(file));
  }

  public removeUploadedFile(fileId: string): void {
    this.uploadedFiles = this.uploadedFiles.filter((file) => file.fileId !== fileId);
    this.cdr.markForCheck();
  }

  private uploadFile(file: File): void {
    const extension = file.name.split('.').pop()?.toLowerCase() ?? '';
    const accepted = this.acceptedFileTypes
      .split(',')
      .map((item) => item.replace('.', ''));
    if (!accepted.includes(extension) || file.size > this.maxFileSize) {
      this.uploadedFiles = [
        ...this.uploadedFiles,
        { fileId: uuidV4(), fileName: file.name, objectKey: '', size: 0, checksum: '', progress: 'failed' },
      ];
      this.cdr.markForCheck();
      return;
    }

    const fileId = uuidV4();
    const item: ConversationFileReference = {
      fileId,
      fileName: file.name,
      objectKey: '',
      size: 0,
      checksum: '',
      progress: 'loading',
    };
    this.uploadedFiles = [...this.uploadedFiles, item];
    const formData = new FormData();
    formData.append('file', file);
    formData.append('file_name_base64', this.encodeFileName(file.name));
    this.conversationWorkspaceService.uploadInputFile(formData)
      .then((result) => {
        this.uploadedFiles = this.uploadedFiles.map((current) =>
          current.fileId === fileId
            ? {
              ...current,
              objectKey: result?.objectKey ?? '',
              fileName: current.fileName,
              size: result?.size ?? 0,
              checksum: result?.checksum ?? '',
              progress: result?.objectKey ? 'succeeded' : 'failed',
            }
            : current,
        );
      })
      .catch(() => {
        this.uploadedFiles = this.uploadedFiles.map((current) =>
          current.fileId === fileId ? { ...current, progress: 'failed' } : current,
        );
      })
      .finally(() => this.cdr.markForCheck());
  }

  private encodeFileName(fileName: string): string {
    const bytes = new TextEncoder().encode(fileName);
    let binary = '';
    bytes.forEach((byte) => binary += String.fromCharCode(byte));
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  }

  public onExecutionPathChange(path: string[]): void {
    this.selectedExecutionPath = path;
    const selectedId = path[path.length - 1];
    const target = this.executionTargets.find((item) => item.id === selectedId);
    if (target) {
      this.selectedExecutionTarget = target.id;
      this.cdr.markForCheck();
    }
  }

  public get hasPendingUploads(): boolean {
    return this.uploadedFiles.some((file) => file.progress === 'loading');
  }

  public get isSending(): boolean {
    return Boolean(this.activeAttempt && !this.activeAttempt.settled);
  }

  private createAttempt(
    query: string,
    inputSnapshot: string,
    recommendationSnapshot: ConversationSkillItem[],
  ): ConversationAttempt {
    const attempt: ConversationAttempt = {
      id: ++this.nextAttemptId,
      workspaceId: this.workspaceId,
      initialSessionId: this.currentSession?.conversation_id ?? '',
      query,
      inputSnapshot,
      recommendationSnapshot,
      phase: this.currentSession?.conversation_id ? 'running' : 'creating',
      opened: false,
      settled: false,
    };
    this.activeAttempt = attempt;
    this.cdr.markForCheck();
    return attempt;
  }

  private startRun(attempt: ConversationAttempt, conversationId: string): void {
    if (!this.isCurrentAttempt(attempt)) {
      return;
    }
    attempt.phase = 'running';
    attempt.conversationId = conversationId;
    this.activatedSkills = [];
    const assistantMsg: ChatMessage = {
      role: 'assistant',
      userContent: attempt.query,
      userFiles: this.uploadedFiles
        .filter((file) => file.progress === 'succeeded' && file.fileName)
        .map(({ fileName }) => ({ fileName })),
      segments: [],
      detailSegments: [],
      runs: [],
      loading: true,
    };
    attempt.assistantMsg = assistantMsg;
    this.messages.push(assistantMsg);
    this.streaming = true;
    this.cdr.markForCheck();

    let source: { close?: () => void } | undefined;
    const selectedTarget = this.executionTargets.find(
      (target) => target.id === this.selectedExecutionTarget,
    );
    const request: ConversationSendRequest = {
      query: attempt.query,
      recommended_skill_ids: attempt.recommendationSnapshot.map((item) => item.skillId),
      select_type: selectedTarget?.type === 'SUPERVISOR' ? 'SUPERVISOR' : 'APP',
      ...(selectedTarget?.type === 'SUPERVISOR'
        ? { model_deployment_id: this.selectedModel }
        : { app_id: selectedTarget?.id }),
      ...this.buildFileReferences(),
    };
    try {
      source = this.conversationWorkspaceService.chatSSE(
        conversationId,
        request,
        {
          onOpen: () => {
            if (!this.isCurrentAttempt(attempt)) {
              return;
            }
            attempt.opened = true;
            if (this.inputText === attempt.inputSnapshot && this.sameSkills(this.recommendedSkills, attempt.recommendationSnapshot)) {
              this.inputText = '';
              this.recommendedSkills = [];
              this.uploadedFiles = [];
              this.skillSelector?.clearRecommendations();
            }
            this.cdr.markForCheck();
          },
          onMessage: (token: any) => {
            if (this.isCurrentAttempt(attempt)) {
              this.handleMessage(token, assistantMsg);
            }
          },
          onDone: () => {
            this.settleRun(attempt, 'done');
          },
          onError: () => {
            this.settleRun(attempt, 'failed');
          },
          onTimeout: () => {
            this.settleRun(attempt, 'failed');
          },
          onAbort: () => {
            this.settleRun(attempt, 'failed');
          },
        },
      );
    } catch {
      this.settleRun(attempt, 'failed');
      return;
    }
    if (!this.isCurrentAttempt(attempt)) {
      this.safeClose(source);
      return;
    }
    attempt.source = source;
  }

  /** 只发送上传成功且包含服务端引用的当前轮附件。 */
  private buildFileReferences(): Pick<ConversationSendRequest, 'file_ids'> {
    const file_ids = this.uploadedFiles
      .filter((file) => file.progress === 'succeeded' && file.objectKey && file.fileName && file.size > 0 && file.checksum)
      .map(({ objectKey, fileName, size, checksum }) => ({
        object_key: objectKey,
        file_name: fileName,
        size,
        checksum,
      }));
    return file_ids.length ? { file_ids } : {};
  }

  private settleRun(attempt: ConversationAttempt, outcome: 'done' | 'failed'): void {
    if (!this.isCurrentAttempt(attempt)) {
      return;
    }
    attempt.settled = true;
    try {
      this.safeClose(attempt.source);
    } finally {
      if (attempt.assistantMsg) {
        attempt.assistantMsg.loading = false;
        attempt.assistantMsg.error = outcome === 'failed';
      }
      this.activeAttempt = null;
      this.streaming = false;
      if (outcome === 'done') {
        this.conversationWorkspaceService.refreshSessions();
      } else if (!attempt.opened) {
        this.loadSkillCatalog(true);
      }
      this.cdr.markForCheck();
    }
  }

  /** 实时流与历史恢复共用同一 canonical reducer，避免刷新前后视图语义漂移。 */
  private handleMessage(token: any, assistantMsg: ChatMessage): void {
    try {
      const parsed = this.normalizeEvent(JSON.parse(token.data) as ConversationEvent);
      this.reduceCanonicalEvent(assistantMsg, parsed, true);
    } catch {
      // Ignore non-JSON sentinels such as [DONE].
    }
    this.cdr.markForCheck();
  }

  private normalizeEvent(event: ConversationEvent): ConversationEvent {
    const source = event as ConversationEvent & Record<string, unknown>;
    const stringValue = (...values: unknown[]): string | undefined => {
      const value = values.find((candidate) => typeof candidate === 'string');
      return typeof value === 'string' ? value : undefined;
    };
    const parentRunId = stringValue(event.data?.parentRunId, source['parentRunId'], source['parent_run_id']);
    return {
      ...event,
      data: {
        ...(event.data ?? {}),
        runId: stringValue(event.data?.runId, source['runId'], source['run_id']),
        parentRunId: parentRunId ?? null,
        executionType: stringValue(event.data?.executionType, source['executionType'], source['execution_type']),
        executionId: stringValue(event.data?.executionId, source['executionId'], source['execution_id']),
      },
    };
  }

  private reduceCanonicalEvent(assistantMsg: ChatMessage, parsed: ConversationEvent, realtime: boolean): void {
    const event = parsed.event;
    const d = parsed.data ?? {};
    const run = this.ensureRun(assistantMsg, d, event === ConversationEventType.RUN_START);
    const isRoot = this.isRootRunEvent(assistantMsg, d);
    switch (event) {
        case ConversationEventType.RUN_START:
          if (run) run.status = d.status ?? 'running';
          break;
        case ConversationEventType.MESSAGE: {
          const content = String(d.delta ?? d.content ?? d.text ?? '');
          if (run && content) this.appendSegment(run.segments, 'message', content);
          break;
        }
        case ConversationEventType.REASONING:
          if (run) this.appendSegment(run.detailSegments, 'reasoning', String(d.content ?? d.delta ?? ''));
          break;
        case ConversationEventType.TOOL_CALL:
          if (run && d.toolId) run.detailSegments.push({ type: 'tool', content: '', toolId: String(d.toolId), toolName: d.toolName, arguments: d.arguments });
          break;
        case ConversationEventType.TOOL_RESULT:
          if (run && d.toolId) {
            const tool = [...run.detailSegments].reverse().find((segment) => segment.type === 'tool' && segment.toolId === String(d.toolId) && !segment.content);
            if (tool) {
              tool.content = this.displayValue(d.result ?? d.output ?? d.content);
              tool.toolStatus = d.status;
            } else if (!realtime) {
              run.detailSegments.push({
                type: 'tool',
                content: this.displayValue(d.result ?? d.output ?? d.content),
                toolId: String(d.toolId),
                toolName: d.toolName,
                toolStatus: d.status,
              });
            }
          }
          break;
        case ConversationEventType.WORKFLOW_NODE:
          if (run) run.workflowNodes.push({ nodeId: d.nodeId, nodeName: d.nodeName, nodeType: d.nodeType, nodeIndex: d.nodeIndex, status: d.status, input: d.input, output: d.output, content: d.content, errorCode: d.errorCode, errorMessage: d.errorMessage });
          break;
        case ConversationEventType.ERROR:
          if (run) run.status = 'error';
          if (isRoot) {
            assistantMsg.error = true;
            assistantMsg.loading = false;
            if (realtime) this.settleCanonicalRoot(assistantMsg, 'failed');
          }
          break;
        case ConversationEventType.RUN_END:
          if (run) run.status = d.status ?? 'completed';
          if (isRoot) {
            const failed = !['success', 'completed', 'done'].includes(String(d.status ?? 'success').toLowerCase());
            assistantMsg.loading = false;
            assistantMsg.error = failed;
            if (realtime) this.settleCanonicalRoot(assistantMsg, failed ? 'failed' : 'done');
          }
          break;
        case ConversationEventType.SKILL_ACTIVATED:
          if (d.skillId) {
            const activation = { skillId: String(d.skillId), name: String(d.name ?? d.skillId), versionId: String(d.versionId ?? '') };
            if (run && !run.activatedSkills.some((item) => item.skillId === activation.skillId && item.versionId === activation.versionId)) {
              run.activatedSkills.push(activation);
            }
            if (!this.activatedSkills.some((item) => item.skillId === activation.skillId && item.versionId === activation.versionId)) {
              this.activatedSkills.push(activation);
            }
          }
          break;
        case ConversationEventType.ARTIFACT: {
          const artifact = this.toArtifact(d, String(d.executionId ?? ''), String(d.runId ?? ''));
          if (artifact && !assistantMsg.artifacts?.some((item) => item.objectKey === artifact.objectKey)) {
            assistantMsg.artifacts ??= [];
            assistantMsg.artifacts.push(artifact);
          }
          break;
        }
        default:
          break;
    }
  }

  private settleCanonicalRoot(message: ChatMessage, outcome: 'done' | 'failed'): void {
    const attempt = this.activeAttempt;
    if (attempt?.assistantMsg === message && this.isCurrentAttempt(attempt)) {
      this.settleRun(attempt, outcome);
    }
  }

  private isRootRunEvent(message: ChatMessage, data: any): boolean {
    const runId = data.runId ? String(data.runId) : '';
    if (!runId) return true;
    const run = this.findRun(message.runs, runId);
    return !String(data.parentRunId ?? run?.parentRunId ?? '');
  }

  private ensureRun(message: ChatMessage, data: any, createRoot = false): ConversationRunNode | undefined {
    const runId = data.runId ? String(data.runId) : (createRoot ? `legacy-${message.runs.length + 1}` : this.rootRunId(message));
    if (!runId) return undefined;
    const existing = this.findRun(message.runs, runId);
    if (existing) return existing;
    const run: ConversationRunNode = { runId, parentRunId: data.parentRunId ?? null, executionType: data.executionType ?? 'unknown', agentId: data.agentId, workflowId: data.workflowId, status: data.status ?? 'running', segments: [], detailSegments: [], workflowNodes: [], activatedSkills: [], children: [] };
    if (run.parentRunId) {
      const parent = this.findRun(message.runs, String(run.parentRunId));
      if (parent) parent.children.push(run); else message.runs.push(run);
    } else {
      message.runs.push(run);
      for (const candidate of [...message.runs]) {
        if (candidate !== run && candidate.parentRunId === run.runId) {
          message.runs.splice(message.runs.indexOf(candidate), 1);
          run.children.push(candidate);
        }
      }
    }
    return run;
  }

  private findRun(runs: ConversationRunNode[], runId: string): ConversationRunNode | undefined {
    for (const run of runs) { if (run.runId === runId) return run; const child = this.findRun(run.children, runId); if (child) return child; }
    return undefined;
  }

  private rootRunId(message: ChatMessage): string | undefined {
    return message.runs.find((run) => !run.parentRunId)?.runId;
  }

  private displayValue(value: unknown): string { return typeof value === 'string' ? value : value == null ? '' : JSON.stringify(value); }

  private appendSegment(segments: ChatSegment[], type: 'message' | 'reasoning', content: string): void {
    if (!content) return;
    const last = segments[segments.length - 1];
    if (last?.type === type) last.content += content; else segments.push({ type, content });
  }

  private mapDetailToMessages(rows: any[]): ChatMessage[] {
    const turns = new Map<string, ChatMessage>();
    for (const row of rows ?? []) {
      const executionId = row.execution_id ?? row.run_id ?? `legacy-${row.created_at ?? Math.random()}`;
      let turn = turns.get(executionId);
      if (!turn) { turn = { role: 'assistant', segments: [], detailSegments: [], runs: [] }; turns.set(executionId, turn); }
      if (row.role === 'user') { turn.userContent = row.content ?? ''; turn.userFiles = this.parseUserFiles(row.file_ids ?? row.fileIds); continue; }
      if (row.event === 'artifact') {
        const artifacts = this.parseArtifacts(row.file_ids ?? row.fileIds, executionId);
        for (const artifact of artifacts) {
          this.reduceCanonicalEvent(turn, this.historyEvent(row, ConversationEventType.ARTIFACT, { ...artifact }), false);
        }
        continue;
      }
      this.reduceCanonicalEvent(turn, this.historyEvent(row), false);
    }
    return Array.from(turns.values());
  }

  private historyEvent(row: any, eventOverride?: ConversationEventType, dataOverride?: Record<string, unknown>): ConversationEvent {
    const event = eventOverride ?? row.event ?? row.type ?? (row.role === 'tool' ? ConversationEventType.TOOL_RESULT : ConversationEventType.MESSAGE);
    const runId = row.run_id ?? row.runId ?? `legacy-run-${row.execution_id ?? row.executionId ?? 'unknown'}`;
    const data: any = {
      runId,
      parentRunId: row.parent_run_id ?? row.parentRunId ?? null,
      executionType: row.execution_type ?? row.executionType ?? 'unknown',
      executionId: row.execution_id ?? row.executionId,
      agentId: row.agent_id ?? row.agentId,
      workflowId: row.workflow_id ?? row.workflowId,
      nodeId: row.node_id ?? row.nodeId,
      nodeName: row.node_name ?? row.nodeName,
      nodeType: row.node_type ?? row.nodeType,
      nodeIndex: row.node_index ?? row.nodeIndex,
      toolId: row.tool_call_id ?? row.toolCallId ?? row.tool_id ?? row.toolId,
      toolName: row.tool_name ?? row.toolName ?? row.tool_id ?? row.toolId,
      arguments: row.tool_args ?? row.arguments,
      result: row.result ?? row.output ?? row.content,
      status: row.status,
      delta: event === ConversationEventType.MESSAGE ? row.content ?? row.delta ?? row.text : undefined,
      content: row.content,
      errorCode: row.error_code ?? row.errorCode,
      errorMessage: row.error_message ?? row.errorMessage,
      skillId: row.skill_id ?? row.skillId,
      name: row.name,
      versionId: row.version_id ?? row.versionId,
      ...dataOverride,
    };
    return this.normalizeEvent({ event, data });
  }

  private parseUserFiles(value: unknown): Array<Pick<ConversationFileReference, 'fileName'>> {
    if (!value) {
      return [];
    }
    try {
      const parsed = typeof value === 'string' ? JSON.parse(value) : value;
      if (!Array.isArray(parsed)) {
        return [];
      }
      return parsed
        .map((item) => {
          if (typeof item === 'string') {
            return { fileName: item };
          }
          return { fileName: item?.fileName ?? item?.file_name ?? '' };
        })
        .filter((item) => Boolean(item.fileName));
    } catch {
      return [];
    }
  }

  private parseArtifacts(value: unknown, executionId: string): ConversationArtifactReference[] {
    if (!value) {
      return [];
    }
    try {
      const parsed = typeof value === 'string' ? JSON.parse(value) : value;
      return Array.isArray(parsed)
        ? parsed.map((item) => this.toArtifact(item, executionId, String(item?.runId ?? item?.run_id ?? ''))).filter(Boolean) as ConversationArtifactReference[]
        : [];
    } catch {
      return [];
    }
  }

  private toArtifact(value: any, fallbackExecutionId?: string, fallbackRunId?: string): ConversationArtifactReference | null {
    const objectKey = value?.objectKey ?? value?.object_key;
    const fileName = value?.fileName ?? value?.file_name;
    if (!objectKey || !fileName) {
      return null;
    }
    return {
      objectKey,
      fileName,
      size: Number(value?.size ?? 0),
      mediaType: value?.mediaType ?? value?.media_type ?? 'application/octet-stream',
      checksum: value?.checksum ?? '',
      executionId: value?.executionId ?? value?.execution_id ?? fallbackExecutionId ?? '',
      runId: value?.runId ?? value?.run_id ?? fallbackRunId ?? '',
      downloadState: 'idle',
    };
  }

  public downloadArtifact(artifact: ConversationArtifactReference): void {
    const conversationId = this.currentSession?.conversation_id;
    if (!conversationId || artifact.downloadState === 'loading') {
      return;
    }
    artifact.downloadState = 'loading';
    this.conversationWorkspaceService.downloadArtifact(conversationId, artifact.objectKey)
      .then((content) => {
        artifact.downloadState = 'idle';
        CommonUtils.downloadFile(content, artifact.fileName, true);
        this.cdr.markForCheck();
      })
      .catch(() => {
        artifact.downloadState = 'failed';
        this.cdr.markForCheck();
      });
  }

  public formatArtifactSize(size: number): string {
    if (size < 1024) {
      return `${size} B`;
    }
    if (size < 1024 * 1024) {
      return `${(size / 1024).toFixed(1)} KB`;
    }
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }

  /** run 表 handoff 工具进入详情；主界面只展示 message。 */
  private rowToSegment(row: any): ChatSegment | null {
    const event = row.event ?? row.type;
    if (row.role === 'tool' || event === 'tool_call' || event === 'tool_result') {
      return {
        type: 'tool',
        content: this.displayValue(row.content ?? row.result ?? row.output),
        toolId: row.tool_id ?? row.toolId,
        toolName: row.tool_name ?? row.toolName,
        arguments: row.arguments,
        toolStatus: row.status,
      };
    }
    if (event === 'reasoning') return { type: 'reasoning', content: row.content ?? row.delta ?? '' };
    if (event === 'message') return { type: 'message', content: row.content ?? row.delta ?? row.text ?? '' };
    return null;
  }

  private clearSkillRoundState(): void {
    this.recommendedSkills = [];
    this.activatedSkills = [];
    this.skillSelector?.clearRecommendations();
  }

  private handleWorkspaceChange(): void {
    const nextWorkspaceId = this.http.getWorkspaceId();
    if (!this.transitionWorkspace(nextWorkspaceId, true)) {
      return;
    }
    this.workspaceRouteProvenance = nextWorkspaceId;
    sessionStorage.setItem(this.workspaceRouteProvenanceKey, nextWorkspaceId);
    this.conversationWorkspaceService.clearSessions(nextWorkspaceId);
    this.router.navigate(['/home/conversation'], {
      queryParams: { new: Date.now(), workspace_id: nextWorkspaceId },
      replaceUrl: true,
    }).then(
      (navigated) => {
        if (!navigated) {
          this.markForCheckIfAlive();
        }
      },
      () => this.markForCheckIfAlive(),
    );
  }

  private transitionWorkspace(nextWorkspaceId: string, invalidateActiveSession = false): boolean {
    if (nextWorkspaceId === this.workspaceId) {
      return false;
    }
    this.clearPendingRouteConversation();
    this.clearPendingActiveSession();
    this.cancelActiveAttempt();
    this.invalidateDetailRequests();
    this.workspaceId = nextWorkspaceId;
    this.currentSession = null;
    this.skillCatalog = [];
    this.messages = [];
    this.clearSkillRoundState();
    this.loadSkillCatalog();
    if (invalidateActiveSession) {
      this.conversationWorkspaceService.setActiveSession(null);
    }
    this.cdr.markForCheck();
    return true;
  }

  private isCurrentCatalogRequest(workspaceId: string, requestId: number): boolean {
    return !this.destroyed && workspaceId === this.workspaceId && requestId === this.catalogRequestId;
  }

  private isDraftPromotion(session: SessionItem | null): boolean {
    const attempt = this.activeAttempt;
    return Boolean(
      attempt &&
      !attempt.settled &&
      attempt.phase === 'creating' &&
      attempt.conversationId &&
      session?.conversation_id === attempt.conversationId &&
      attempt.workspaceId === this.workspaceId,
    );
  }

  private isSameConversation(next: SessionItem | null, current: SessionItem | null): boolean {
    return Boolean(next?.conversation_id && next.conversation_id === current?.conversation_id);
  }

  private handleConversationRoute(conversationId: string, routeWorkspaceId: unknown): void {
    const routeWorkspace = typeof routeWorkspaceId === 'string' ? routeWorkspaceId : '';
    this.clearPendingRouteConversation();
    this.clearPendingActiveSession();
    if (routeWorkspace) {
      if (routeWorkspace === this.workspaceId) {
        this.openConversation(conversationId);
      }
      return;
    }

    // 没有任何历史归属时，保留旧版首次深链的兼容入口；其余情况必须经当前空间列表验证。
    if (!this.workspaceRouteProvenance) {
      this.openConversation(conversationId);
      return;
    }

    this.pendingRouteConversation = {
      conversationId,
      workspaceId: this.workspaceId,
    };
    this.resolvePendingRouteConversation();
  }

  private resolvePendingRouteConversation(): void {
    const pending = this.pendingRouteConversation;
    if (!pending || this.destroyed || pending.workspaceId !== this.workspaceId ||
      this.sessionListState.workspaceId !== pending.workspaceId) {
      return;
    }
    const belongsToCurrentWorkspace = this.sessionListState.sessions.some(
      (session) => session.conversation_id === pending.conversationId,
    );
    if (!belongsToCurrentWorkspace) {
      return;
    }
    this.pendingRouteConversation = null;
    this.openConversation(pending.conversationId);
  }

  private clearPendingRouteConversation(): void {
    this.pendingRouteConversation = null;
  }

  private isCurrentWorkspaceActiveState(state: ActiveSessionState): boolean {
    return state.workspaceId === this.workspaceId;
  }

  private queuePendingActiveSession(session: SessionItem | null): void {
    if (session?.conversation_id) {
      this.pendingActiveSession = { session, workspaceId: this.workspaceId };
    }
  }

  private resolvePendingActiveSession(): void {
    const pending = this.pendingActiveSession;
    if (!pending || this.destroyed || pending.workspaceId !== this.workspaceId ||
      this.sessionListState.workspaceId !== pending.workspaceId ||
      !this.sessionListState.sessions.some((session) => session.conversation_id === pending.session.conversation_id)) {
      return;
    }
    this.pendingActiveSession = null;
    this.conversationWorkspaceService.setActiveSession(pending.session);
  }

  private clearPendingActiveSession(): void {
    this.pendingActiveSession = null;
  }

  private markForCheckIfAlive(): void {
    if (!this.destroyed) {
      this.cdr.markForCheck();
    }
  }

  private loadSessionDetail(conversationId: string): void {
    const requestWorkspaceId = this.workspaceId;
    const requestId = ++this.detailRequestId;
    this.conversationWorkspaceService
      .detailSession(conversationId)
      .then((detail) => {
        if (!this.isCurrentDetailRequest(requestWorkspaceId, conversationId, requestId)) {
          return;
        }
        this.messages = this.mapDetailToMessages(detail?.messages ?? []);
        if (!this.currentSession?.title) {
          this.currentSession = { ...this.currentSession!, title: detail?.title ?? '' };
        }
        this.cdr.markForCheck();
        this.scheduleHistoryScrollToBottom();
      })
      .catch(() => void 0);
  }

  /** 历史详情和 Markdown 均完成布局后，稳定定位到最新一轮。 */
  private scheduleHistoryScrollToBottom(): void {
    this.cancelHistoryScrollRestore();
    const view = this.document.defaultView;
    if (!view) {
      return;
    }
    const firstFrame = view.requestAnimationFrame(() => {
      this.historyScrollFrameIds = this.historyScrollFrameIds.filter((id) => id !== firstFrame);
      const secondFrame = view.requestAnimationFrame(() => {
        this.historyScrollFrameIds = this.historyScrollFrameIds.filter((id) => id !== secondFrame);
        if (this.destroyed) {
          return;
        }
        const list = this.messageList?.nativeElement;
        if (list) {
          list.scrollTop = list.scrollHeight;
        }
      });
      this.historyScrollFrameIds.push(secondFrame);
    });
    this.historyScrollFrameIds.push(firstFrame);
  }

  private cancelHistoryScrollRestore(): void {
    const view = this.document.defaultView;
    if (view) {
      this.historyScrollFrameIds.forEach((id) => view.cancelAnimationFrame(id));
    }
    this.historyScrollFrameIds = [];
  }

  private invalidateDetailRequests(): void {
    this.cancelHistoryScrollRestore();
    this.detailRequestId += 1;
  }

  private invalidateCatalogRequests(): void {
    this.catalogRequestId += 1;
  }

  private isCurrentDetailRequest(workspaceId: string, conversationId: string, requestId: number): boolean {
    return !this.destroyed &&
      workspaceId === this.workspaceId &&
      requestId === this.detailRequestId &&
      this.currentSession?.conversation_id === conversationId;
  }

  private isCurrentAttempt(attempt: ConversationAttempt): boolean {
    if (this.activeAttempt !== attempt || attempt.settled || attempt.workspaceId !== this.workspaceId) {
      return false;
    }
    const expectedSessionId = attempt.conversationId ?? attempt.initialSessionId;
    return (this.currentSession?.conversation_id ?? '') === expectedSessionId;
  }

  private cancelActiveAttempt(): void {
    const attempt = this.activeAttempt;
    if (!attempt || attempt.settled) {
      return;
    }
    attempt.settled = true;
    try {
      this.safeClose(attempt.source);
    } finally {
      if (attempt.assistantMsg) {
        attempt.assistantMsg.loading = false;
      }
      this.activeAttempt = null;
      this.streaming = false;
    }
  }

  private safeClose(source?: { close?: () => void }): void {
    try {
      source?.close?.();
    } catch {
      // 关闭失败不应阻断本地运行态和订阅的收口。
    }
  }

  private releaseAttempt(attempt: ConversationAttempt): void {
    attempt.settled = true;
    if (this.activeAttempt === attempt) {
      this.activeAttempt = null;
      this.streaming = false;
    }
    this.cdr.markForCheck();
  }

  private sameSkills(left: ConversationSkillItem[], right: ConversationSkillItem[]): boolean {
    return left.length === right.length && left.every((item, index) => item.skillId === right[index].skillId);
  }

  /** 气泡角色标签：我 / 助手 / 子Agent·{agentId前8位} */
  public roleLabel(message: ChatMessage): string {
    return message.role === 'user' ? '我' : '助手';
  }
}
