import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  NgZone,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { MODULES } from '@shared/modules';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { NewTaskService } from './new-task.service';

interface IAgentOption {
  agent_id: string;
  agent_name: string;
  description?: string;
  icon?: string;
}

interface IProgressEvent {
  type: string;
  text: string;
  time: string;
}

/** 新建任务向导页：选择智能体 → 配置参数 → 执行进度 */
@Component({
  selector: 'new-task',
  templateUrl: './new-task.component.html',
  styleUrls: ['./new-task.component.less'],
  standalone: true,
  imports: [CommonModule, FormsModule, MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class NewTaskComponent implements OnInit, OnDestroy {
  /** 向导步骤：0 选择智能体 / 1 配置参数 / 2 执行进度 */
  step = 0;

  /** 智能体列表 */
  agents: IAgentOption[] = [];
  agentsLoading = false;
  agentsLoadFailed = false;
  selectedAgentId = '';
  manualAgentId = '';

  /** 任务参数 */
  taskName = '';
  taskInput = '';
  timeoutSeconds = 300;

  /** 提交与进度 */
  submitting = false;
  submitError = '';
  taskId = '';
  taskStatus = '';
  taskError = '';
  progressEvents: IProgressEvent[] = [];
  outputText = '';

  private eventSource: EventSource | null = null;
  private pollTimer: any = null;

  constructor(
    public router: Router,
    private i18n: I18NextEagerPipe,
    private agentRepo: AppAgentRepoService,
    private newTaskService: NewTaskService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone,
  ) {}

  ngOnInit(): void {
    this.loadAgents();
  }

  ngOnDestroy(): void {
    this.closeStream();
  }

  /** 加载当前工作空间的智能体列表；失败时提供手动输入兜底 */
  async loadAgents() {
    this.agentsLoading = true;
    this.agentsLoadFailed = false;
    try {
      const res: any = await this.agentRepo.getAgentList({}, 0, 100);
      const list = res?.agent_list || res?.data?.agent_list || [];
      this.agents = list.map((a: any) => ({
        agent_id: a.agent_id || a.id,
        agent_name: a.agent_name || a.name,
        description: a.agent_description || a.description || '',
        icon: a.icon || '',
      }));
      if (this.agents.length && !this.selectedAgentId) {
        this.selectedAgentId = this.agents[0].agent_id;
      }
    } catch (e) {
      this.agents = [];
      this.agentsLoadFailed = true;
    } finally {
      this.agentsLoading = false;
      this.cdr.markForCheck();
    }
  }

  selectAgent(agentId: string) {
    this.selectedAgentId = agentId;
  }

  get effectiveAgentId(): string {
    return this.selectedAgentId || this.manualAgentId.trim();
  }

  get canNext(): boolean {
    if (this.step === 0) {
      return !!this.effectiveAgentId;
    }
    if (this.step === 1) {
      return !!this.taskName.trim() && !!this.taskInput.trim();
    }
    return false;
  }

  prev() {
    if (this.step > 0 && this.step < 2) {
      this.step -= 1;
    }
  }

  next() {
    if (this.step === 0 && this.canNext) {
      if (!this.taskName) {
        const agent = this.agents.find(
          (a) => a.agent_id === this.effectiveAgentId,
        );
        if (agent) {
          this.taskName = `${agent.agent_name}-${new Date()
            .toISOString()
            .slice(0, 10)}`;
        }
      }
      this.step = 1;
    }
  }

  /** 提交任务：创建即执行，转入进度步骤并订阅 SSE */
  async submit() {
    if (!this.canNext || this.submitting) {
      return;
    }
    this.submitting = true;
    this.submitError = '';
    try {
      const res: any = await this.newTaskService.createTask({
        agent_id: this.effectiveAgentId,
        name: this.taskName.trim(),
        params: {
          input: this.taskInput.trim(),
          timeout_seconds: this.timeoutSeconds,
        },
      });
      const data = res?.data || res;
      if (!data?.id) {
        this.submitError = res?.message || this.i18n.transform('new_task_submit_failed');
        return;
      }
      this.taskId = data.id;
      this.taskStatus = data.status || 'pending';
      this.step = 2;
      this.pushEvent('info', this.i18n.transform('new_task_created'));
      this.openStream();
      this.startPolling();
    } catch (e: any) {
      this.submitError =
        e?.message || e?.error?.message || this.i18n.transform('new_task_submit_failed');
    } finally {
      this.submitting = false;
      this.cdr.markForCheck();
    }
  }

  /** 订阅 SSE 进度（同源自动携带 Cookie），断线降级为轮询 */
  private openStream() {
    this.closeStream();
    const url = this.newTaskService.streamUrl(this.taskId);
    this.zone.runOutsideAngular(() => {
      this.eventSource = new EventSource(url, { withCredentials: true });
      this.eventSource.onmessage = (ev) => {
        this.zone.run(() => this.handleSseFrame(ev.data));
      };
      this.eventSource.onerror = () => {
        this.zone.run(() => {
          // 任务终态时后端会正常关流；非终态断线交由轮询兜底
          if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null;
          }
        });
      };
    });
  }

  /** 解析平台 SSE 帧：{type: token|tool_call|thought|error|done, data, seq} */
  private handleSseFrame(raw: string) {
    let frame: any;
    try {
      frame = JSON.parse(raw);
    } catch {
      return;
    }
    const dataText =
      typeof frame.data === 'string'
        ? frame.data
        : JSON.stringify(frame.data ?? '');
    switch (frame.type) {
      case 'token':
        this.outputText += dataText;
        break;
      case 'thought':
        this.pushEvent('thought', dataText);
        break;
      case 'tool_call':
        this.pushEvent('tool_call', dataText);
        break;
      case 'error':
        this.taskError = dataText;
        this.pushEvent('error', dataText);
        break;
      case 'done':
        this.pushEvent('done', this.i18n.transform('new_task_done'));
        this.refreshTask();
        this.closeStream();
        break;
      default:
        break;
    }
    this.cdr.markForCheck();
  }

  /** 状态轮询兜底（SSE 不可用时依然可见状态流转） */
  private startPolling() {
    this.stopPolling();
    this.pollTimer = setInterval(() => this.refreshTask(), 3000);
  }

  private stopPolling() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  }

  async refreshTask() {
    if (!this.taskId) {
      return;
    }
    try {
      const res: any = await this.newTaskService.getTask(this.taskId);
      const data = res?.data || res;
      if (data?.status) {
        this.taskStatus = data.status;
        if (data.error) {
          this.taskError = data.error;
        }
        if (this.isFinal) {
          this.stopPolling();
        }
        this.cdr.markForCheck();
      }
    } catch {
      /* 轮询失败静默，下一轮重试 */
    }
  }

  async cancel() {
    if (!this.taskId || this.isFinal) {
      return;
    }
    try {
      await this.newTaskService.cancelTask(this.taskId);
      this.pushEvent('info', this.i18n.transform('new_task_cancelled'));
      this.refreshTask();
    } catch {
      /* 忽略取消失败，状态由轮询纠正 */
    }
  }

  get isFinal(): boolean {
    return ['succeeded', 'failed', 'cancelled'].includes(this.taskStatus);
  }

  get statusColor(): string {
    switch (this.taskStatus) {
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

  get progressPercent(): number {
    switch (this.taskStatus) {
      case 'pending':
        return 10;
      case 'running':
        return 60;
      case 'succeeded':
      case 'failed':
      case 'cancelled':
        return 100;
      default:
        return 0;
    }
  }

  /** 再来一单：重置向导 */
  restart() {
    this.closeStream();
    this.stopPolling();
    this.step = 0;
    this.taskId = '';
    this.taskStatus = '';
    this.taskError = '';
    this.taskName = '';
    this.taskInput = '';
    this.outputText = '';
    this.progressEvents = [];
    this.submitError = '';
  }

  close() {
    this.router.navigate(['/home/overview']);
  }

  private pushEvent(type: string, text: string) {
    this.progressEvents.push({
      type,
      text,
      time: new Date().toLocaleTimeString(),
    });
  }

  private closeStream() {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
    this.stopPolling();
  }
}
