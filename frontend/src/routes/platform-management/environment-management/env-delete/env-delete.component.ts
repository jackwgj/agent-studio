import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { SharedModule } from '@shared/shared.module';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { formatTime } from 'src/utils/commonFn';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { EnvManagementService } from '@routes/platform-management/environment-management/env-management.service';
import { Router } from '@angular/router';
import { NZ_MODAL_DATA, NzModalRef } from 'ng-zorro-antd/modal';

@Component({
  selector: 'env-delete',
  templateUrl: './env-delete.component.html',
  styleUrls: ['./env-delete.component.less'],
  standalone: true,
  imports: [CommonModule, MODULES, SharedModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON,I18nNamespace.AGENT_CENTER,I18nNamespace.PLATFORM_MANAGEMENT,],
    },
  ],
})
export class EnvDeleteComponent implements OnInit {
  data: any;
  envInfo: any[] = [];

  loading = false;
  displayedData: any[] = [];
  srcData: { data: any[]; state: undefined } = {
    data: [], // 源数据
    state: undefined,
  };

  listColumns: any[] = [
    { title: this.i18n.transform('name')},
    { title: this.i18n.transform('type') },
    { title: this.i18n.transform('version')},
    { title: this.i18n.transform('affiliated_space')},
  ];
  currentPage: number = 1;
  totalNumber: number = 0;
  pageSize: { options: Array<number>; size: number } = {
    options: [10, 20, 50, 100],
    size: 10,
  };

  deployTypeMap = new Map([
    ['agent', this.i18n.transform('single_agent_app')],
    ['workflow', this.i18n.transform('workflow_app')],
    ['multi', this.i18n.transform('multi_agent_app')],
  ]);

  constructor(
    private environmentManagementService: EnvManagementService,
    private i18n: I18NextEagerPipe,
    private router: Router,
    private modalRef: NzModalRef,
    @Inject(NZ_MODAL_DATA) public modalData: any
  ) {}

  ngOnInit() {
    if (this.modalData && this.modalData.context) {
      this.data = this.modalData.context.data;
      this.envInfo = this.modalData.context.envInfo || [];
    }
    this.srcData.data = this.envInfo;
  }

  close(): void {
    if (this.modalData && this.modalData.close) {
      this.modalData.close();
    } else {
      this.modalRef.close();
    }
  }

  dismiss(): void {
    if (this.modalData && this.modalData.dismiss) {
      this.modalData.dismiss();
    } else {
      this.modalRef.close();
    }
  }

  goToAgentDeploymen() {
    this.router.navigate(['/home/agent-observatory/agentDeployment']);
  }

  /**
   * 分页变化时重新加载数据
   */
  pageUpdate($event: any) {
    this.currentPage = $event.currentPage;
    this.pageSize.size = $event.size;
    this.queryAssociatedInstanceData();
  }

  queryAssociatedInstanceData() {
    this.environmentManagementService
      .queryAssociatedInstance(this.data.id, {
        page: this.totalNumber,
        pageSize: this.pageSize.size,
      })
      .then((response) => {
        this.srcData.data = response.env_info;
      });
  }

  /**
   * 用于 ngFor 的 trackBy 函数，提升性能
   */
  public trackByFn(index: number, item: any): number {
    return item.id;
  }

  protected readonly formatTime = formatTime;
  protected readonly changeUrl = cdnAssetUrl;
}
