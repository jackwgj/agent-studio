import { Component, OnInit, OnDestroy, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NzModalModule, NzModalRef, NZ_MODAL_DATA } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { Subject, takeUntil } from 'rxjs';
import { SchedulerService } from './scheduler.service';
import type { ScheduledTask } from './scheduler.service';
import { ScheduleRulePickerComponent, ScheduleValue } from './schedule-rule-picker.component';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { ModelManagementService } from '@services/repositories/model-management-new';

@Component({
  selector: 'app-scheduler-form-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NzModalModule,
    NzFormModule,
    NzInputModule,
    NzSelectModule,
    NzSwitchModule,
    NzDatePickerModule,
    NzButtonModule,
    NzIconModule,
    NzToolTipModule,
    NzInputNumberModule,
    ScheduleRulePickerComponent,
  ],
  template: `
    <div class="form-modal-title">{{ editTask ? '编辑自动化' : '新建自动化' }}</div>
    <div class="form-modal-body">
      <form nz-form [formGroup]="form" nzLayout="vertical">
          <!-- 任务名称 -->
          <nz-form-item>
            <nz-form-label nzRequired>任务名称</nz-form-label>
            <nz-form-control nzErrorTip="请输入任务名称">
              <input nz-input formControlName="name" placeholder="请输入任务名称" maxlength="100" />
            </nz-form-control>
          </nz-form-item>

          <!-- 任务描述 -->
          <nz-form-item>
            <nz-form-label>任务描述</nz-form-label>
            <nz-form-control>
              <textarea nz-input formControlName="description" placeholder="任务描述（选填）"
                [nzAutosize]="{ minRows: 2, maxRows: 4 }"></textarea>
            </nz-form-control>
          </nz-form-item>

          <!-- 调度配置 -->
          <nz-form-item>
            <nz-form-label nzRequired>调度类型</nz-form-label>
            <nz-form-control>
              <nz-radio-group formControlName="scheduleType" (ngModelChange)="onScheduleTypeChange()">
                <label nz-radio value="once">一次性任务</label>
                <label nz-radio value="recurring">周期重复任务</label>
              </nz-radio-group>
            </nz-form-control>
          </nz-form-item>

          <!-- 一次性任务：选时间点 -->
          <nz-form-item *ngIf="form.get('scheduleType')?.value === 'once'">
            <nz-form-label nzRequired>执行时间</nz-form-label>
            <nz-form-control>
              <nz-date-picker
                formControlName="onceTime"
                nzShowTime
                nzFormat="yyyy-MM-dd HH:mm"
                style="width: 100%;"
                placeholder="选择执行时间"
              ></nz-date-picker>
            </nz-form-control>
          </nz-form-item>

          <!-- 周期重复任务：调度规则选择器 -->
          <nz-form-item *ngIf="form.get('scheduleType')?.value === 'recurring'">
            <nz-form-label nzRequired>调度规则</nz-form-label>
            <nz-form-control>
              <app-schedule-rule-picker formControlName="scheduleRule"></app-schedule-rule-picker>
            </nz-form-control>
          </nz-form-item>

          <!-- 生效时间范围 -->
          <nz-form-item>
            <nz-form-label>生效时间范围</nz-form-label>
            <nz-form-control>
              <div class="time-range-row">
                <nz-date-picker
                  formControlName="validFrom"
                  nzShowTime
                  nzFormat="yyyy-MM-dd HH:mm"
                  style="width: calc(50% - 12px);"
                  placeholder="开始时间（选填）"
                ></nz-date-picker>
                <span class="time-range-sep">至</span>
                <nz-date-picker
                  formControlName="validUntil"
                  nzShowTime
                  nzFormat="yyyy-MM-dd HH:mm"
                  style="width: calc(50% - 12px);"
                  placeholder="截止时间（选填）"
                ></nz-date-picker>
              </div>
            </nz-form-control>
          </nz-form-item>

          <!-- 执行配置 -->
          <nz-form-item>
            <nz-form-label nzRequired>执行方式</nz-form-label>
            <nz-form-control>
              <nz-select formControlName="executorType" style="width: 100%;" placeholder="选择执行方式">
                <nz-option nzValue="llm_prompt" nzLabel="大模型调用"></nz-option>
                <nz-option nzValue="agent_run" nzLabel="运行智能体"></nz-option>
                <nz-option nzValue="workflow_run" nzLabel="运行工作流"></nz-option>
                <nz-option nzValue="http_call" nzLabel="HTTP 请求"></nz-option>
              </nz-select>
            </nz-form-control>
          </nz-form-item>

          <!-- 选择智能体/工作流 -->
          <nz-form-item *ngIf="form.get('executorType')?.value === 'agent_run'">
            <nz-form-label nzRequired>选择智能体</nz-form-label>
            <nz-form-control>
              <nz-select
                formControlName="agentId"
                style="width: 100%;"
                placeholder="选择智能体"
                nzShowSearch
                [nzOptions]="agentOptions"
                (nzOnSearch)="searchAgents($event)"
              ></nz-select>
            </nz-form-control>
          </nz-form-item>

          <nz-form-item *ngIf="form.get('executorType')?.value === 'workflow_run'">
            <nz-form-label nzRequired>选择工作流</nz-form-label>
            <nz-form-control>
              <nz-select
                formControlName="workflowId"
                style="width: 100%;"
                placeholder="选择工作流"
                nzShowSearch
                [nzOptions]="workflowOptions"
                (nzOnSearch)="searchWorkflows($event)"
              ></nz-select>
            </nz-form-control>
          </nz-form-item>

          <!-- HTTP 请求配置 -->
          <nz-form-item *ngIf="form.get('executorType')?.value === 'http_call'">
            <nz-form-label nzRequired>请求 URL</nz-form-label>
            <nz-form-control>
              <input nz-input formControlName="httpUrl" placeholder="https://example.com/api" />
            </nz-form-control>
          </nz-form-item>

          <!-- 模型选择 -->
          <nz-form-item *ngIf="form.get('executorType')?.value === 'llm_prompt' || form.get('executorType')?.value === 'agent_run'">
            <nz-form-label>模型选择</nz-form-label>
            <nz-form-control>
              <nz-select formControlName="modelId" style="width: 100%;" placeholder="选择模型（选填）" nzShowSearch>
                <nz-option *ngFor="let m of modelOptions" [nzValue]="m.id" [nzLabel]="m.name"></nz-option>
              </nz-select>
            </nz-form-control>
          </nz-form-item>

          <!-- Prompt 输入 -->
          <nz-form-item *ngIf="form.get('executorType')?.value === 'llm_prompt'">
            <nz-form-label nzRequired>Prompt</nz-form-label>
            <nz-form-control>
              <textarea
                nz-input
                formControlName="prompt"
                [nzAutosize]="{ minRows: 4, maxRows: 12 }"
                placeholder="输入定时执行的提示词"
              ></textarea>
            </nz-form-control>
          </nz-form-item>

          <!-- 通知配置 -->
          <nz-form-item>
            <nz-form-label>通知配置</nz-form-label>
            <nz-form-control>
              <div class="notify-row">
                <label class="notify-item">
                  <nz-switch formControlName="notifyOnSuccess" nzSize="small"></nz-switch>
                  <span>任务成功通知</span>
                </label>
                <label class="notify-item">
                  <nz-switch formControlName="notifyOnFailure" nzSize="small"></nz-switch>
                  <span>任务失败通知</span>
                </label>
              </div>
            </nz-form-control>
          </nz-form-item>

          <!-- 重试次数 -->
          <nz-form-item>
            <nz-form-label>失败重试次数</nz-form-label>
            <nz-form-control>
              <nz-input-number formControlName="maxRetries" [nzMin]="0" [nzMax]="10" [nzStep]="1" style="width: 120px;"></nz-input-number>
            </nz-form-control>
          </nz-form-item>
        </form>
      </div>
      <div class="form-modal-footer">
        <button nz-button (click)="onCancel()">取消</button>
        <button nz-button nzType="primary" [nzLoading]="saving" (click)="onSubmit()">保存</button>
      </div>
  `,
  styles: [`
    .form-modal-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin-bottom: 16px;
    }
    .form-modal-body {
      max-height: 60vh;
      overflow-y: auto;
      padding: 8px 0;
    }
    .form-modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 20px;
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;
    }
    .time-range-row {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .time-range-sep {
      color: #999;
      font-size: 13px;
    }
    .notify-row {
      display: flex;
      gap: 24px;
    }
    .notify-item {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
    }
    .notify-item span {
      font-size: 13px;
      color: #333;
    }
  `],
})
export class SchedulerFormModalComponent implements OnInit, OnDestroy {
  get editTask(): ScheduledTask | null {
    return this.modalData?.editTask ?? null;
  }

  form!: FormGroup;
  saving = false;
  agentOptions: any[] = [];
  workflowOptions: any[] = [];
  modelOptions: any[] = [];

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private modal: NzModalRef,
    private message: NzMessageService,
    private schedulerService: SchedulerService,
    private appAgentRepo: AppAgentRepoService,
    private modelManagementService: ModelManagementService,
    @Inject(NZ_MODAL_DATA) private modalData: any,
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadAgents();
    this.loadWorkflows();
    this.loadModels();
    if (this.editTask) {
      this.populateForm();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initForm(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: [''],
      scheduleType: ['recurring', [Validators.required]],
      onceTime: [null],
      scheduleRule: [null],
      validFrom: [null],
      validUntil: [null],
      executorType: ['llm_prompt', [Validators.required]],
      agentId: [null],
      workflowId: [null],
      httpUrl: [''],
      modelId: [null],
      prompt: [''],
      notifyOnSuccess: [false],
      notifyOnFailure: [true],
      maxRetries: [3],
    });
  }

  private populateForm(): void {
    if (!this.editTask) return;
    const t = this.editTask;

    this.form.patchValue({
      name: t.name,
      description: t.description || '',
      scheduleType: t.repeat_type === 'once' ? 'once' : 'recurring',
      executorType: t.executor_type || 'llm_prompt',
      agentId: t.executor_config?.agent_id || null,
      workflowId: t.executor_config?.workflow_id || null,
      httpUrl: t.executor_config?.url || '',
      modelId: t.model_id || null,
      prompt: t.prompt || '',
      notifyOnSuccess: t.notification?.notify_on_success ?? false,
      notifyOnFailure: t.notification?.notify_on_failure ?? true,
      maxRetries: t.max_retries ?? 3,
      validFrom: t.valid_from ? new Date(t.valid_from) : null,
      validUntil: t.valid_until ? new Date(t.valid_until) : null,
    });

    if (t.repeat_type === 'once') {
      this.form.patchValue({ onceTime: t.schedule_config?.run_at ? new Date(t.schedule_config.run_at) : null });
    } else {
      let scheduleValue: ScheduleValue;
      if (t.schedule_type === 'cron') {
        scheduleValue = { type: 'cron', cronExpression: t.schedule_config?.expression || '' };
      } else if (t.schedule_type === 'natural_language') {
        scheduleValue = { type: 'natural_language', naturalLanguageText: t.schedule_config?.text || '' };
      } else {
        scheduleValue = { type: 'cron', cronExpression: t.schedule_config?.expression || '' };
      }
      this.form.patchValue({ scheduleRule: scheduleValue });
    }
  }

  onScheduleTypeChange(): void {}

  onCancel(): void {
    this.modal.close();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      Object.values(this.form.controls).forEach(c => c.markAsDirty());
      return;
    }
    const val = this.form.value;

    let scheduleConfig: any;
    let scheduleType: string;
    let repeatType: string;

    if (val.scheduleType === 'once') {
      if (!val.onceTime) {
        this.message.warning('请选择执行时间');
        return;
      }
      scheduleType = 'cron';
      repeatType = 'once';
      const d = new Date(val.onceTime);
      scheduleConfig = {
        type: 'cron',
        config: {
          expression: `${d.getMinutes()} ${d.getHours()} ${d.getDate()} ${d.getMonth() + 1} *`,
          run_at: val.onceTime.toISOString(),
        },
      };
    } else {
      const rule: ScheduleValue = val.scheduleRule;
      if (!rule) {
        this.message.warning('请配置调度规则');
        return;
      }
      repeatType = 'always';
      if (rule.type === 'cron') {
        scheduleType = 'cron';
        scheduleConfig = { type: 'cron', config: { expression: rule.cronExpression } };
      } else if (rule.type === 'natural_language') {
        scheduleType = 'natural_language';
        scheduleConfig = { type: 'natural_language', config: { text: rule.naturalLanguageText } };
      } else {
        scheduleType = 'cron';
        const expr = this.visualToCron(rule.visual!);
        scheduleConfig = { type: 'cron', config: { expression: expr } };
      }
    }

    const executorConfig: any = { type: val.executorType };
    if (val.executorType === 'agent_run') executorConfig.config = { agent_id: val.agentId };
    else if (val.executorType === 'workflow_run') executorConfig.config = { workflow_id: val.workflowId };
    else if (val.executorType === 'http_call') executorConfig.config = { url: val.httpUrl };
    else if (val.executorType === 'llm_prompt') executorConfig.config = {};

    const payload: any = {
      name: val.name,
      description: val.description || '',
      workspace_id: this.schedulerService.getWorkspaceId(),
      schedule: scheduleConfig,
      repeat: { type: repeatType },
      valid_from: val.validFrom ? val.validFrom.toISOString() : null,
      valid_until: val.validUntil ? val.validUntil.toISOString() : null,
      executor: executorConfig,
      model_id: val.modelId || null,
      prompt: val.prompt || null,
      notification: {
        notify_on_success: val.notifyOnSuccess,
        notify_on_failure: val.notifyOnFailure,
        channels: ['in_app'],
      },
      max_retries: val.maxRetries,
    };

    this.saving = true;
    const req$ = this.editTask
      ? this.schedulerService.updateTask(this.editTask.id, payload)
      : this.schedulerService.createTask(payload);

    req$.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.message.success(this.editTask ? '任务更新成功' : '任务创建成功');
        this.modal.close(true);
      },
      error: () => {
        this.message.error(this.editTask ? '更新失败' : '创建失败');
        this.saving = false;
      },
    });
  }

  private visualToCron(v: any): string {
    const h = v.hour ?? 9;
    const m = v.minute ?? 0;
    switch (v.frequency) {
      case 'hourly': return `${m} * * * *`;
      case 'daily': return `${m} ${h} * * *`;
      case 'workdays': return `${m} ${h} * * 1-5`;
      case 'weekly': {
        const days = (v.weekdays || []).join(',');
        return `${m} ${h} * * ${days || '1'}`;
      }
      case 'monthly': {
        if (v.monthDayType === 'last') return `${m} ${h} L * *`;
        return `${m} ${h} ${v.monthDay || 1} * *`;
      }
      default: return `${m} ${h} * * *`;
    }
  }

  searchAgents(keyword: string): void {
    const params: any = {};
    if (keyword) params.name = keyword;
    this.appAgentRepo.getAgentList(params, 0, 50).then((res: any) => {
      this.agentOptions = (res?.agent_list || []).map((a: any) => ({
        value: a.id,
        label: a.name,
      }));
    }).catch(() => {});
  }

  searchWorkflows(keyword: string): void {
    const params: any = {};
    if (keyword) params.name = keyword;
    this.appAgentRepo.getWorkflowList(params, 0, 50).then((res: any) => {
      this.workflowOptions = (res?.workflow_list || []).map((w: any) => ({
        value: w.workflow_id,
        label: w.name,
      }));
    }).catch(() => {});
  }

  loadAgents(): void { this.searchAgents(''); }
  loadWorkflows(): void { this.searchWorkflows(''); }

  loadModels(): void {
    this.modelManagementService
      .getAvailableModelList({
        groupby: 'provider',
        publish_status: 'online',
        model_type: 'LLM,IMAGE-TO-TEXT',
        with_router: false,
      })
      .then((res: any) => {
        const options: any[] = [];
        (res?.data ?? []).forEach((provider: any) => {
          (provider.models ?? []).forEach((model: any) => {
            options.push({
              id: model.id,
              name: model.model_name,
            });
          });
        });
        this.modelOptions = options;
      })
      .catch(() => {});
  }
}
