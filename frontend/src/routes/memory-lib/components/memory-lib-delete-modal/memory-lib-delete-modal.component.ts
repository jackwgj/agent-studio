import { ChangeDetectionStrategy, Component, computed, inject, Input, OnInit, signal, viewChild } from '@angular/core';
import { FormsModule, NgForm, ValidationErrors } from '@angular/forms';
import { I18NEXT_NAMESPACE, I18NextEagerPipe, I18NextModule } from 'angular-i18next';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { I18nNamespace } from '@i18n';
import { IMappings } from '@routes/agent-center/types/common.types';
import { MemoryLibReferenceType } from '@routes/memory-lib/memory-lib-enums';
import type { IMemoryLibItem } from '@routes/memory-lib/memory-lib-interfaces';
import { MCPService } from '@services/agent-center/mcp.service';
import { HttpService } from '@services/http.service';

@Component({
  selector: 'memory-lib-delete-modal',
  standalone: true,
  imports: [
    I18NextModule,
    FormsModule,
    NzButtonModule,
    NzSpinModule,
    NzAlertModule,
    NzTableModule,
    NzPaginationModule,
    NzInputModule,
    NzFormModule,
    NzToolTipModule
  ],
  templateUrl: './memory-lib-delete-modal.component.html',
  styleUrl: './memory-lib-delete-modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.MEMORY_LIB, I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class MemoryLibDeleteModalComponent implements OnInit {
  @Input() deletedMemoryLib!: IMemoryLibItem;

  readonly i18n = inject(I18NextEagerPipe);
  readonly http = inject(HttpService);
  readonly mcpRepoServe = inject(MCPService);
  readonly modalRef = inject(NzModalRef); // 注入以修改弹窗外壳配置

  templateFormControl = viewChild<NgForm>('templateFormControl');

  loading = signal(false);
  deleteLoading = signal(false);
  isReferenced = signal(true);

  currentPage = signal(1);
  pageSize = signal({
    options: [10, 20, 50, 100],
    size: 10,
  });
  totalNumber = signal(0);

  srcData = signal<{ data: any[] }>({
    data: []
  });

  workspaceId = this.http.getWorkspaceId();

  resourceTypeMap = new Map([
    [MemoryLibReferenceType.AGENT, this.i18n.transform('single_agent_app')],
    [MemoryLibReferenceType.WORKFLOW, this.i18n.transform('workflow')],
    [MemoryLibReferenceType.CONTROLLER, this.i18n.transform('multiple_agent')],
  ]);

  columns = [
    { title: this.i18n.transform('name') },
    { title: this.i18n.transform('type') },
  ];

  deleteConfirmText = 'DELETE';
  deleteConfirmValue = '';

  isConfirmed = computed(() => this.deleteConfirmText === this.deleteConfirmValue);

  setLoading = (isLoading: boolean) => {
    this.deleteLoading.set(isLoading);
  };

  checkTemplateForm = (): ValidationErrors | null => {
    if (this.isReferenced()) {
      const form = this.templateFormControl()?.form;
      if (form?.invalid) {
        Object.values(form.controls).forEach(control => {
          if (control?.invalid) {
            control.markAsDirty();
            control.updateValueAndValidity({ onlySelf: true });
          }
        });
        return { invalid: true };
      }
    }
    return null;
  };

  #queryReferencedResource() {
    this.loading.set(true);
    this.mcpRepoServe
      .getReferenceList(this.deletedMemoryLib?.memory_repo_id, {
        limit: this.pageSize().size,
        showlatest: true,
        offset: (this.currentPage() - 1) * this.pageSize().size,
        resource_type: 'memory',
      })
      .then((res: IMappings) => {
        this.srcData.update(current => ({
          ...current,
          data: res.relations,
        }));
        this.totalNumber.set(res.count);
      })
      .catch(() => {
        this.srcData.update(current => ({
          ...current,
          data: [],
        }));
      })
      .finally(() => {
        this.loading.set(false);
        this.isReferenced.set(Boolean(this.srcData().data?.length));
      });
  }

  ngOnInit() {
    // 动态给 Modal 外壳设置标题与危险操作按钮颜色
    this.modalRef.updateConfig({
      nzTitle: this.i18n.transform('memory.management.modal.deleteTitle'),
      nzOkDanger: true
    });
    this.#queryReferencedResource();
  }

  pageIndexChange(pageIndex: number) {
    this.currentPage.set(pageIndex);
    this.#queryReferencedResource();
  }

  pageSizeChange(pageSize: number) {
    this.pageSize.update(current => ({
      ...current,
      size: pageSize,
    }));
    this.currentPage.set(1);
    this.#queryReferencedResource();
  }

  toResourceDetail(row: any) {
    const url = window.location.href?.split('/home');
    const prefixUrl = url[0];
    const { app_type, app_id } = row;

    if (app_type === MemoryLibReferenceType.WORKFLOW) {
      window.open(`${prefixUrl}/home/agent-center/app-flow/flow?id=${app_id}&workspace_id=${this.workspaceId}`, '_blank');
    }

    if (app_type === MemoryLibReferenceType.AGENT) {
      window.open(`${prefixUrl}/home/agent-center/app-agent/detail?agentId=${app_id}&workspace_id=${this.workspaceId}`, '_blank');
    }

    if (app_type === MemoryLibReferenceType.CONTROLLER) {
      window.open(`${prefixUrl}/home/agent-center/app-flow/flow?id=${app_id}&workspace_id=${this.workspaceId}&type=multi`, '_blank');
    }
  }

  autoInputConfirm() {
    this.deleteConfirmValue = this.deleteConfirmText;
  }

  // 这两个由于交由 nzModal 的默认控制处理按钮逻辑，所以在此处实际已不再调用，可保留空桩
  dismiss() {}
  close() {}
}
