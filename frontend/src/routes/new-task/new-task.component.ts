import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { MODULES } from '@shared/modules';
import { NzPopoverModule } from 'ng-zorro-antd/popover';
import { MarkdownComponent } from 'ngx-markdown';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { ContextService } from '@services/context.service';
import { NewTaskService } from './new-task.service';
import { ConversationStore, IAssetRef } from './conversation-store';
import { AssetPickerComponent } from './asset-picker.component';
import { PreviewPanelComponent } from './preview-panel.component';

/**
 * 新建任务工作台（new-task-chat）：三栏布局
 * - 左栏：任务列表（复用 GET /platform/v1/tasks）
 * - 中栏：多轮对话输入 + 「+」资产选择器
 * - 右栏：实时 Markdown 预览
 */
@Component({
  selector: 'new-task',
  templateUrl: './new-task.component.html',
  styleUrls: ['./new-task.component.less'],
  standalone: true,
  imports: [CommonModule, FormsModule, MODULES, NzPopoverModule, MarkdownComponent, AssetPickerComponent, PreviewPanelComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class NewTaskComponent implements OnInit, OnDestroy {
  /** 智能体列表（首轮创建时选择） */
  agents: { agent_id: string; agent_name: string }[] = [];
  selectedAgentId = '';
  manualAgentId = '';

  /** 左栏任务列表 */
  taskList: any[] = [];

  /** 中栏输入草稿 */
  draftInput = '';
  draftAssets: IAssetRef[] = [];
  assetPopoverVisible = false;

  constructor(
    private agentRepo: AppAgentRepoService,
    private newTaskService: NewTaskService,
    public store: ConversationStore,
    private ctx: ContextService,
    private cdr: ChangeDetectorRef,
  ) {}

  /** 当前团队空间 id（触发 ContextService 的 projectId 恢复） */
  private get projectId(): string {
    void this.ctx.baseUrl;
    return this.ctx.projectId || '';
  }

  private get workspaceId(): string {
    return this.ctx.currentWorkspaceId || '';
  }

  ngOnInit(): void {
    this.loadAgents();
    this.loadTaskList();
  }

  ngOnDestroy(): void {
    this.store.reset();
  }

  async loadAgents() {
    try {
      const res: any = await this.agentRepo.getAgentList({}, 0, 100);
      const list = res?.agent_list || res?.data?.agent_list || [];
      this.agents = list.map((a: any) => ({
        agent_id: a.agent_id || a.id,
        agent_name: a.agent_name || a.name,
      }));
      if (this.agents.length && !this.selectedAgentId) {
        this.selectedAgentId = this.agents[0].agent_id;
      }
    } catch {
      this.agents = [];
    }
  }

  get effectiveAgentId(): string {
    return this.selectedAgentId || this.manualAgentId.trim();
  }

  async loadTaskList() {
    try {
      const res: any = await this.newTaskService.listTasks(1, 50);
      const payload = res?.data ?? res;
      this.taskList = payload?.items ?? payload?.tasks ?? [];
      this.cdr.markForCheck();
    } catch {
      this.taskList = [];
    }
  }

  /** 新建：清空会话，回到空白对话 */
  onNewTask() {
    this.store.reset();
    this.draftInput = '';
    this.draftAssets = [];
    this.cdr.markForCheck();
  }

  /** 点击左栏任务：加载历史对话与右栏预览 */
  async onSelectTask(item: any) {
    await this.store.load(item.id);
    this.cdr.markForCheck();
  }

  /** 发送：无 taskId 则首轮创建；有 taskId 则多轮追加重跑 */
  async onSend() {
    const text = this.draftInput.trim();
    if (!text || this.store.streaming) {
      return;
    }
    if (!this.store.taskId) {
      // 路由智能体优先取资产选择器中选中的 agent；未选则回退顶部选择器；仍无则默认模型
      const agentAsset = this.draftAssets.find((a) => a.kind === 'agent');
      const agentId = agentAsset?.id || this.effectiveAgentId || 'default';
      const agentName =
        agentAsset?.name ||
        this.agents.find((a) => a.agent_id === this.effectiveAgentId)?.agent_name ||
        '任务';
      const name = `${agentName}-${new Date().toISOString().slice(0, 10)}`;
      await this.store.create(agentId, name, text, this.draftAssets, this.projectId, this.workspaceId);
    } else {
      await this.store.append(text, this.draftAssets);
    }
    this.draftInput = '';
    this.draftAssets = [];
    this.loadTaskList();
    this.cdr.markForCheck();
  }

  removeDraftAsset(index: number) {
    this.draftAssets = this.draftAssets.filter((_, i) => i !== index);
  }

  statusColor(status: string | null | undefined): string {
    switch (status) {
      case 'succeeded':
        return 'success';
      case 'failed':
        return 'error';
      case 'cancelled':
        return 'default';
      case 'running':
        return 'processing';
      default:
        return 'warning';
    }
  }
}
