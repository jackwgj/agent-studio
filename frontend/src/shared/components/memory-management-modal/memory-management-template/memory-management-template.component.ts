import { Component, Input, OnInit, signal, SimpleChanges } from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { cloneDeep } from 'lodash';
import { MessageComponent } from '@shared/services/cfdata.service';
import { MemoryStrategyType } from '@routes/memory-lib/memory-lib-enums';
import { I18nNamespace } from '@i18n';
import { MemoryManagementService } from '@services/memory-management.service';
import { StructMemEditCellComponent } from '@shared/components/memory-management-modal/memory-management-template/struct-mem-edit-cell/struct-mem-edit-cell.component';
import { type ConversationState, IMemoryInfo, IMemoryManagementData } from '@shared/components/memory-management/memory-management.interface';
import { MemoryManagementUtils } from '@shared/components/memory-management/memory-management.utils';
import { NewCommonNoDataWithBtnComponent } from '@shared/components/new-common-no-data-with-btn/new-common-no-data-with-btn.component';
import { MODULES } from '@shared/modules';
import { MemoryLibApiService } from '@routes/memory-lib/memory-lib-api.service';
import { IMemoryLibItem } from '@routes/memory-lib/memory-lib-interfaces';
import { NzModalRef } from 'ng-zorro-antd/modal';

enum ActionType {
  EDIT = 'EDIT',
  SAVE = 'SAVE',
  CANCEL = 'CANCEL',
  DELETE = 'DELETE',
}

@Component({
  selector: 'memory-management-template',
  templateUrl: 'memory-management-template.component.html',
  standalone: true,
  styleUrls: ['./memory-management-template.component.less'],
  imports: [MODULES, StructMemEditCellComponent, NewCommonNoDataWithBtnComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.COMMON, I18nNamespace.MEMORY_LIB],
    },
  ],
})
export class MemoryManagementTemplateComponent implements OnInit {
  // 当对话中的记忆更新时，用更新的记忆
  @Input() updatedMemoryInfo: IMemoryInfo[];

  @Input() conversationState: ConversationState;

  @Input() memoryLibId: string;

  @Input() isShowAllReset = false;

  loading = false;
  isExtract = false;
  isRetrieve = false;
  delConfirmTitle = this.i18n.transform('memory_del_warn');
  memoryLoading = false;
  editingRow?: any;
  displayedData: Array<any> = [];
  srcData: any = {
    data: [],
    state: {
      searched: false,
      sorted: false,
      paginated: false,
    },
  };
  columns: Array<any> = [
    {
      title: this.i18n.transform('memory_content'),
    },
    {
      title: this.i18n.transform('operation'),
      width: '100px',
    },
  ];

  // 记录修改的记忆 用于调接口
  changeIdSet = new Set<string>();

  // Per-tab local data cache — preserves edits/deletes across tab switches.
  // Keyed by tab id (MemoryStrategyType).  When a tab has local changes
  // (entries in changeIdSet or deletions not yet persisted), the cached
  // data is used instead of re-fetching from the backend.
  private tabDataCache = new Map<string, Array<any>>();

  allTabs: Array<any> = [
    {
      title: this.i18n.transform('memory.detail.name.semanticMemory', { ns: I18nNamespace.MEMORY_LIB }),
      id: 'summary',
      active: false,
      isShow: () => true,
    },
    {
      title: this.i18n.transform('memory.detail.name.userProfile', { ns: I18nNamespace.MEMORY_LIB }),
      id: MemoryStrategyType.USER_PROFILE,
      active: false,
      isShow: () => this.memoryDetail?.long_term_memory_strategies?.some(item => item.type === MemoryStrategyType.USER_PROFILE),
    },
  ];
  tabs: Array<any>;
  tabIndex = 0;

  currentTabId = signal<string>('summary');
  currentPage = signal(1);
  pageSize = signal<any>({
    options: [10, 20, 50, 100],
    size: 10,
  });
  totalNumber = signal(0);

  private memoryDetail: IMemoryLibItem;

  constructor(
    private i18n: I18NextEagerPipe,
    private memoryLibApiService: MemoryLibApiService,
    private memoryManagementService: MemoryManagementService,
    private modalRef: NzModalRef,
  ) {}

  ngOnInit() {
    if (this.updatedMemoryInfo?.length) {
      this.srcData.data = cloneDeep(this.updatedMemoryInfo);
    }
    if (this.memoryLibId) {
      this.memoryLibApiService.queryMemoryLibDetail(this.memoryLibId).then((res) => {
        this.memoryDetail = res;
        this.initTab();
      }).catch(() => {
        // Detail fetch may fail (e.g. invalid workspace_id in dev env).
        // Still init tabs — the summary tab is always visible.
        this.initTab();
      });
    }
    this.isRetrieve = Boolean(this.conversationState.isRetrieve);
    this.isExtract = Boolean(this.conversationState.isExtract);
  }

  dismiss() {
    this.modalRef.close();
  }

  close() {
    this.loading = true;
    const data = this.getData();
    const changedMemories = data.memories;
    if (changedMemories && changedMemories.length > 0 && this.memoryLibId) {
      this.memoryManagementService
        .changeMemoryContent(
          { memories: changedMemories },
          { memory_repo_id: this.memoryLibId },
        )
        .then(() => {
          this.tabDataCache.clear();
          this.modalRef.close('ok');
        })
        .catch(() => {
          this.loading = false;
        });
    } else {
      this.tabDataCache.clear();
      this.modalRef.close('ok');
    }
  }

  deleteAll() {
    if (!this.memoryLibId) {
      return;
    }

    this.memoryLoading = true;
    this.memoryManagementService
      .clearMemory({ memory_repo_id: this.memoryLibId })
      .then(() => {
        this.srcData.data = [];
        this.tabDataCache.set(this.currentTabId(), []);
        this.totalNumber.set(0);
        this.memoryLoading = false;
      })
      .catch(() => {
        this.memoryLoading = false;
      });
  }

  getData(): IMemoryManagementData {
    const changeData = this.srcData.data.filter(mem => this.changeIdSet.has(mem.id)) as IMemoryInfo[];
    return {
      enable_retrieve: this.isRetrieve,
      enable_extract: this.isExtract,
      ...MemoryManagementUtils.reverseStandardizedMemoryInfo(changeData),
    };
  }

  trackByFn(_: number, item: any) {
    return item.id;
  }

  onSelect(item: any, row: any): void {
    if (item.id === ActionType.EDIT) {
      this.editingRow = { ...row };
      this.editableRows(false);
    }
  }

  onSelectEditing(item: any, row: any): void {
    if (item.id === ActionType.SAVE) {
      if (this.editingRow?.content && this.editingRow?.content?.length <= 1000) {
        this.srcData.data = this.srcData.data.map((current: any) => {
          if (this.editingRow && current.id === row.id) {
            current = this.editingRow;
          }
          return current;
        });
        this.changeIdSet.add(row.id);
        // Update cache to reflect the edit
        this.tabDataCache.set(this.currentTabId(), [...this.srcData.data]);
        this.editingRow = undefined;
        this.editableRows(true);
      }
    } else {
      this.editingRow = undefined;
      this.editableRows(true);
    }
  }

  getDisplayMemContent(mem: any) {
    return MemoryManagementUtils.getMemDisplayContent(mem as IMemoryInfo);
  }

  confirmIsDisabled() {
    return Boolean(this.editingRow);
  }

  confirmInvalidTip() {
    if (this.editingRow) {
      return this.i18n.transform('mem_save_warn_tip');
    }
    return '';
  }

  deleteMem(row: IMemoryInfo) {
    if (row) {
      this.memoryLoading = true;
      const delData = MemoryManagementUtils.reverseStandardizedMemoryInfo([row]);
      this.memoryManagementService
        .deleteMemory(delData?.memories?.map(item => item.memory_id) ?? [], {
          memory_repo_id: this.memoryLibId,
        })
        .then(() => {
          MessageComponent.showSuccess(this.i18n.transform('delete_success'));
          this.srcData.data = this.srcData.data.filter((current: any): boolean => current.id !== row.id);
          this.changeIdSet.delete(row.id);
          // Update cache to reflect the deletion
          this.tabDataCache.set(this.currentTabId(), [...this.srcData.data]);
          this.memoryLoading = false;
        })
        .catch(() => {
          this.memoryLoading = false;
        });
    }
  }

  handleTabChange(tab) {
    if (tab.active) {
      // Save current tab data before switching
      this.saveCurrentTabData();

      const tabId = tab.id as MemoryStrategyType;
      this.currentTabId.set(tabId);

      // Use cached data if available (preserves local edits/deletes),
      // otherwise fetch from backend.
      // Note: a cached empty array is valid (user deleted all items),
      // so we check has() rather than checking length.
      const cached = this.tabDataCache.get(tabId);
      if (this.tabDataCache.has(tabId)) {
        this.srcData.data = cached!;
        this.totalNumber.set(cached!.length);
      } else {
        this.queryMemories();
      }
    }
  }

  handleTabIndexChange(index: number) {
    this.tabs.forEach(item => item.active = false);
    const tab = this.tabs[index];
    this.tabs[index].active = true;
    if (tab) {
      this.handleTabChange(tab);
    }
  }

  pageUpdate(pageEvent: any) {
    const { currentPage, size } = pageEvent;
    this.currentPage.set(currentPage);
    this.pageSize.update(currentConfig => ({
      ...currentConfig,
      size,
    }));
    this.queryMemories();
  }

  pageIndexChange(page: number) {
    this.currentPage.set(page);
    this.queryMemories();
  }

  pageSizeChange(size: number) {
    this.pageSize.update(currentConfig => ({
      ...currentConfig,
      size,
    }));
    this.currentPage.set(1);
    this.queryMemories();
  }

  private editableRows(_editable: boolean): void {
    // items array removed — edit/delete buttons now use direct template bindings
  }

  /** Persist current tab's data into the per-tab cache. */
  private saveCurrentTabData(): void {
    const currentId = this.currentTabId();
    // Only cache if we have data or if a cache entry already exists (to preserve deletions).
    // Avoid caching empty data on first load before any query has been made.
    if (currentId && (this.srcData.data.length > 0 || this.tabDataCache.has(currentId))) {
      this.tabDataCache.set(currentId, [...this.srcData.data]);
    }
  }

  private queryMemories(): void {
    if (!this.memoryLibId || this.updatedMemoryInfo?.length) {
      return;
    }

    this.memoryLoading = true;
    this.memoryManagementService.queryMemory({
      offset: (this.currentPage() - 1) * this.pageSize().size,
      limit: this.pageSize().size,
      memory_repo_id: this.memoryLibId,
      memory_type: this.currentTabId(),
    }).then(res => {
      const data = MemoryManagementUtils.standardizedMemoryInfo(res);
      this.srcData.data = data;
      // Cache the fresh data for this tab
      this.tabDataCache.set(this.currentTabId(), [...data]);
      this.memoryLoading = false;
      this.totalNumber.set(res.total ?? 0);
    }).catch(() => {
      this.srcData.data = [];
      this.totalNumber.set(0);
      this.memoryLoading = false;
    });
  }

  private initTab(): void {
    this.tabs = this.allTabs.filter(tab => tab.isShow());
    if (!this.tabs.length) {
      return;
    }
    this.tabs[0].active = true;
    this.handleTabChange(this.tabs[0]);
  }
}
