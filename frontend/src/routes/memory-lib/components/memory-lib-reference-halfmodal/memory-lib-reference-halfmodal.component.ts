import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { Subject, takeUntil } from 'rxjs';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzSpinModule } from 'ng-zorro-antd/spin';

import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { MemoryLibReferenceType } from '@routes/memory-lib/memory-lib-enums';
import { I18nNamespace } from '@i18n';
import { CommonNoDataComponent } from '@shared/components/common-no-data/common-no-data.component';
import { MODULES } from '@shared/modules';
import { MCPService } from '@services/agent-center/mcp.service';
import { IMappings } from '@routes/agent-center/types/common.types';
import { HttpService } from '@services/http.service';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';

@Component({
  selector: 'memory-lib-reference-halfmodal',
  templateUrl: './memory-lib-reference-halfmodal.component.html',
  styleUrls: ['./memory-lib-reference-halfmodal.component.scss'],
  standalone: true,
  imports: [MODULES, NzTabsModule, NzTableModule, NzSpinModule, CommonNoDataComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.MEMORY_LIB],
    },
  ],
})
export class MemoryLibReferenceHalfmodalComponent implements OnInit, OnDestroy {
  @Input() memLibId = '';

  isLoading = false;
  cdnAssetUrl = cdnAssetUrl;
  currentActiveTab = 'agent';
  activeTabIndex = 0;

  tabs: any = [
    { id: MemoryLibReferenceType.AGENT, title: this.i18n.transform('single_agent_app') },
    { id: MemoryLibReferenceType.WORKFLOW, title: this.i18n.transform('workflow_app') },
    { id: MemoryLibReferenceType.CONTROLLER, title: this.i18n.transform('multi_agent_app') },
  ];

  columns = [
    { title: this.i18n.transform('name') },
    { title: this.i18n.transform('memory.detail.reference.version') },
  ];

  relations = [];
  srcData = { data: [] };
  currentPage = 1;
  totalNumber = 0;
  pageSize = { size: 10 };

  isDevelopSpace = false;
  workspaceId = '';
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private i18n: I18NextEagerPipe,
    private readonly agentConfigService: AgentConfigService,
    private readonly mcpRepoServe: MCPService,
    private readonly http: HttpService
  ) {
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.isDevelopSpace = (params.from && params.from === 'develop-space') || this.router.url.includes('develop-space');
    });
  }

  ngOnInit(): void {
    this.workspaceId = this.http.getWorkspaceId();
    this.setTabs();
    this.getReferenceList();
  }

  setTabs() {
    this.tabs = this.tabs.filter(item => {
      if (item.id === MemoryLibReferenceType.AGENT) {
        return this.agentConfigService.isSupportMemoryInSingleAgent();
      }
      return true;
    });
    this.activeTabIndex = 0;
    this.currentActiveTab = this.tabs[0].id;
  }

  // 响应式分页切换
  onPageChange(page: number) {
    this.currentPage = page;
    this.getReferenceList();
  }

  // 响应式 Tab 切换
  onTabChange(index: number) {
    this.currentActiveTab = this.tabs[index].id;
    this.currentPage = 1; // 切换 Tab 重置页码
    this.getReferenceList();
  }

  getReferenceList() {
    this.isLoading = true;
    this.mcpRepoServe
      .getReferenceList(this.memLibId, {
        limit: this.pageSize.size,
        showlatest: true,
        offset: (this.currentPage - 1) * this.pageSize.size,
        app_type: this.currentActiveTab,
        resource_type: 'memory',
      })
      .then((res: IMappings) => {
        this.relations = res.relations;
        this.srcData.data = this.relations.filter(item => item.app_type === this.currentActiveTab);
        this.totalNumber = res.count;
        this.isLoading = false;
      })
      .catch(() => {
        this.isLoading = false;
      });
  }

  toResourceDetail(row: any) {
    const url = window.location.href?.split('/home');
    const prefixUrl = url[0];
    const { app_type, app_id } = row;

    if (app_type === MemoryLibReferenceType.AGENT) {
      window.open(`${prefixUrl}/home/agent-center/app-agent/detail?agentId=${app_id}&workspace_id=${this.workspaceId}`, '_blank');
    } else if (app_type === MemoryLibReferenceType.WORKFLOW) {
      window.open(`${prefixUrl}/home/agent-center/app-flow/flow?id=${app_id}&workspace_id=${this.workspaceId}`, '_blank');
    } else if (app_type === MemoryLibReferenceType.CONTROLLER) {
      window.open(`${prefixUrl}/home/agent-center/app-flow/flow?id=${app_id}&workspace_id=${this.workspaceId}&type=multi`, '_blank');
    } else {}
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  close() {}
  dismiss() {}
}
