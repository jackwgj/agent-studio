import { Component, Input, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { Network } from '@model';
import { TASK_TYPE_MAP } from '@routes/knowledge-center/knowledge.const';
import { TaskQuery } from '@routes/knowledge-center/knowledge.types';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { BehaviorSubject, Subject, catchError, expand, from, map, of, switchMap, takeUntil, takeWhile, timer } from 'rxjs';
import { FormateTimePipe } from 'src/pipes/formate-time.pipe';
import { BatchDeleteComponent } from '../batch-delete.component';
import { FormsModule } from '@angular/forms';
import { NzTableModule, NzTableQueryParams } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzModalService, NzModalModule } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'knowledge-task-manage',
  templateUrl: './task-manage.component.html',
  styleUrls: ['./task-manage.component.less'],
  standalone: true,
  imports: [
    MODULES,
    BatchDeleteComponent,
    FormsModule,
    NzTableModule,
    NzButtonModule,
    NzInputModule,
    NzSelectModule,
    NzBadgeModule,
    NzTypographyModule,
    NzIconModule,
    NzEmptyModule,
    NzModalModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.AGENT_CENTER, I18nNamespace.KNOWLEDGE],
    },
    FormateTimePipe,
    NzModalService,
    NzMessageService
  ],
})
export class TaskManageComponent implements OnInit, OnDestroy {
  @Input() kbId!: string;
  @Input() isMarket = false;

  @ViewChild('batchDeleteModal') batchDeleteModal: BatchDeleteComponent;

  // 表格状态
  tableData: any[] = [];
  loading = false;
  total = 0;
  pageIndex = 1;
  pageSize = 10;

  // 搜索参数
  searchName = '';
  searchStatus: string | null = null;

  // 复选框相关
  setOfCheckedId = new Set<string>();
  allChecked = false;
  indeterminate = false;

  emptyImage = `${Network.ASSETS_PATH}images/noData.svg`;

  // 数据请求控制
  private destroy$ = new Subject<void>();
  private loadTrigger$ = new BehaviorSubject<void>(undefined);

  constructor(
    private kbRepoServ: KnowledgeRepoService,
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
    private i18n: I18NextEagerPipe,
    public kbAbilitiesService: KbAbilitiesService,
    private formateTimePipe: FormateTimePipe
  ) {}

  ngOnInit() {
    this.setupDataPolling();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupDataPolling() {
    this.loadTrigger$.pipe(
      takeUntil(this.destroy$),
      switchMap(() => this.fetchAndPollTasks())
    ).subscribe(res => {
      this.tableData = res.data;
      this.total = res.total;
      this.loading = false;
      this.refreshCheckedStatus();
    });
  }

  private fetchAndPollTasks() {
    this.loading = true;
    return this.getTasks().pipe(
      expand(val => {
        const hasProcessing = val.tasks.some((file: any) => ['PENDING', 'RUNNING'].includes(file.status.toUpperCase()));
        if (hasProcessing) {
          // 有处理中的任务时，每15秒轮询一次
          return timer(15_000).pipe(switchMap(() => this.getTasks()));
        }
        return of(null);
      }),
      takeWhile(val => val !== null),
      map((val: any) => {
        val?.tasks?.forEach((item: any) => {
          item.create_time = this.formateTimePipe.transform(item.create_time);
        });
        return {
          total: val?.total ?? 0,
          data: val?.tasks ?? [],
        };
      }),
      catchError(() => {
        return of({
          total: 0,
          data: [],
        });
      })
    );
  }

  public getTasks() {
    const query: TaskQuery = {
      offset: (this.pageIndex - 1) * this.pageSize,
      limit: this.pageSize,
    };
    if (this.searchStatus) query.task_status = this.searchStatus;
    if (this.searchName) query.file_name = this.searchName;

    return from(this.kbRepoServ.listTasks(this.kbId, query));
  }

  // ==== 交互事件处理 ====

  public onQueryParamsChange(params: NzTableQueryParams) {
    this.pageIndex = params.pageIndex;
    this.pageSize = params.pageSize;
    this.loadTrigger$.next();
  }

  public onSearch() {
    this.pageIndex = 1;
    this.loadTrigger$.next();
  }

  // 判断批量操作是否禁用
  public operationDisableFn(): boolean {
    return this.setOfCheckedId.size === 0 || this.kbAbilitiesService.isResourceDisabled;
  }

  public onBatchDelete() {
    const selectedRows = this.tableData.filter(d => this.setOfCheckedId.has(d.id));
    this.batchDeleteModal.openModal(
      [
        { headerName: this.i18n.transform('task_manage'), field: 'name' },
        { headerName: this.i18n.transform('status'), field: '_statusText' }
      ],
      // 为了让弹窗复用展示，将状态转化为文本传入
      selectedRows.map(row => ({ ...row, _statusText: this.getStatusText(row.status) }))
    );
  }

  public async handleBatchDelete(ctx: any) {
    try {
      const selectedRows = ctx.tableData || [];
      const task_ids = selectedRows.map((r: any) => r.id);
      const { total_count = 0, deleted_count = 0 } = (await this.kbRepoServ.deleteTask(this.kbId, { task_ids })) ?? {};
      this.nzMessage.success(
        this.i18n.transform('batch_delete_success', {
          totalCount: total_count,
          deletedCount: deleted_count,
        })
      );
      this.setOfCheckedId.clear();
      this.loadTrigger$.next();
      this.batchDeleteModal.closeModal();
    } finally {
      ctx.confirmLoading = false;
    }
  }

  public onDelete(data: any) {
    if (this.kbAbilitiesService.isResourceDisabled) return;

    this.nzModal.confirm({
      nzTitle: this.i18n.transform('task_delete_title'),
      nzContent: this.i18n.transform('task_delete_content', { id: data.id }),
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOkText: this.i18n.transform('delete'),
      nzOnOk: async () => {
        try {
          await this.kbRepoServ.deleteTask(this.kbId, { task_ids: [data.id] });
          this.nzMessage.success(this.i18n.transform('delete_success'));
          this.loadTrigger$.next();
          return true;
        } catch (error) {
          return false;
        }
      }
    });
  }

  // ==== 复选框逻辑 ====

  onAllChecked(checked: boolean): void {
    this.tableData.forEach(({ id }) => this.updateCheckedSet(id, checked));
    this.refreshCheckedStatus();
  }

  onItemChecked(id: string, checked: boolean): void {
    this.updateCheckedSet(id, checked);
    this.refreshCheckedStatus();
  }

  private updateCheckedSet(id: string, checked: boolean): void {
    if (checked) {
      this.setOfCheckedId.add(id);
    } else {
      this.setOfCheckedId.delete(id);
    }
  }

  private refreshCheckedStatus(): void {
    this.allChecked = this.tableData.length > 0 && this.tableData.every(({ id }) => this.setOfCheckedId.has(id));
    this.indeterminate = this.tableData.some(({ id }) => this.setOfCheckedId.has(id)) && !this.allChecked;
  }

  public getTaskTypeName(type: string): string {
    return this.i18n.transform(TASK_TYPE_MAP[type] || type);
  }

  public getStatusText(status: string): string {
    const map: Record<string, string> = {
      'PENDING': this.i18n.transform('pending'),
      'RUNNING': this.i18n.transform('running'),
      'SUCCESS': this.i18n.transform('success'),
      'ERROR': this.i18n.transform('error')
    };
    return map[status?.toUpperCase()] || status;
  }

  public getStatusIcon(status: string): any {
    const map: Record<string, string> = {
      'PENDING': 'default',
      'RUNNING': 'processing',
      'SUCCESS': 'success',
      'ERROR': 'error'
    };
    return map[status?.toUpperCase()] || 'default';
  }
}
