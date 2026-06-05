import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { KbUtils } from '@routes/knowledge-center/kb.utils';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { IKbSearchRecord } from '@routes/agent-center/app-knowledge/knowledge.types';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { KnowledgeType } from "@routes/knowledge-center/knowledge-base-list/knowledge-base-list.map";
import { SetSidebarVisibilityService } from "@shared/services/set-sidebar-visibility.service";
import { copyToClipboard } from 'src/utils/commonFn';
import { MarkedPipe } from "../../../pipes/marked.pipe";
import { SafeHtmlPipe } from "../../../pipes/safehtml.pipe";
import { AsyncPipe } from "@angular/common";
import { PipesModule } from "../../../pipes/pipes.module";

import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'knowledge-hit-test',
  templateUrl: './hit-test.component.html',
  styleUrls: ['./hit-test.component.less'],
  standalone: true,
  imports: [
    MODULES,
    MarkedPipe,
    SafeHtmlPipe,
    AsyncPipe,
    PipesModule,
    FormsModule,
    NzButtonModule,
    NzIconModule,
    NzInputModule,
    NzTabsModule,
    NzSpinModule,
    NzProgressModule,
    NzPaginationModule,
    NzEmptyModule,
    NzDrawerModule,
    NzToolTipModule,
    NzModalModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.KNOWLEDGE],
    },
  ],
})
export class KnowledgeHitTestComponent implements OnInit {
  @ViewChild('scrollContent') scrollContent;

  public kbId = '';

  public display_name = '';

  base64 = '';
  isModalOpen = false;

  public kbResutList: any = [];

  public historyList: IKbSearchRecord[] = [];

  public isLoading = false;

  public isHistoryLoading = false;

  public isHistoryVisible = false;

  public isCardLoading = false;

  public query = '';

  public search_mode = '';

  public tabs = [];

  public currentPage = 1;

  public total = 0;

  public pageConfig = {
    options: [10, 20, 30, 50],
    size: 10,
  };

  public sliceCardDesc = '';

  public currentHistoryPage = 1;

  public totalHistory = 0;
  get perPageHistoryList() {
    return this.historyList.slice(
      (this.currentHistoryPage - 1) * 10,
      this.currentHistoryPage * 10,
    );
  }

  isMarket = false;

  constructor(
    private route: ActivatedRoute,
    private kbRepoServ: KnowledgeRepoService,
    private cdr: ChangeDetectorRef,
    private i18n: I18NextEagerPipe,
    private setSidebarVisibilityService: SetSidebarVisibilityService,
    private kbAbilitiesService: KbAbilitiesService,
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
    private router: Router,
  ) {}

  ngOnInit() {
    this.sliceCardDesc = this.i18n.transform('no_hit_tip');
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('init');
    this.route.queryParams.subscribe((params) => {
      this.kbId = params?.id ?? '';
      this.display_name = params?.display_name ?? '';

      this.isMarket = params?.isMarket === 'true';
      this.#setTabOptions();
    }).unsubscribe();
  }

  ngOnDestroy(): void {
    this.setSidebarVisibilityService.setSidebarsVisibilityByState('destroy');
  }

  public back() {
    if (this.kbAbilitiesService.isDevelopSpace){
      (window as unknown as { developSpaceKnowledge: boolean }).developSpaceKnowledge = true;
    }
    this.route.queryParams.subscribe((params) => {
      if (params?.isFromLibraryHome === 'true') {
        this.router.navigate(['/home/agent-center/library-home'], {
          state: {
            activeTab: 1,
          },
          queryParams: {
            tabId: 'knowledge',
          }
        });
      } else {
        window.history.back();
      }
    })
  }

  public openHistoryModal() {
    this.isHistoryVisible = true;
    this.getHistory();
  }

  public closeHistoryModal() {
    this.isHistoryVisible = false;
  }

  public async getHistory() {
    try {
      this.isHistoryLoading = true;
      const data = await this.kbRepoServ.listKbSearchRecords({
        offset: 0,
        limit: 1000,
        knowledge_repo_id: this.kbId,
      });

      if (data?.KnowledgeSearchRecord) {
        this.historyList = data.KnowledgeSearchRecord;
        this.totalHistory = data.count;
      }
    } finally {
      this.isHistoryLoading = false;
      this.cdr.detectChanges();
    }
  }

  public async getResults() {
    if (!this.query || !this.search_mode) {
      return;
    }
    try {
      this.isCardLoading = true;
      this.scrollContent?.nativeElement?.scrollIntoView();
      const data = await this.kbRepoServ.searchKbHitTesting({
        offset: (this.currentPage - 1) * this.pageConfig.size,
        limit: this.pageConfig.size,
        knowledge_repo_id: this.kbId,
        query: this.query,
        search_mode: this.search_mode,
      });

      if (data?.search_result_list) {
        this.kbResutList = await Promise.all(
          data.search_result_list.map(async (contentItem) => {
            const resultItem = { ...contentItem };
            if (contentItem.image_paths?.length > 0) {
              if (!contentItem.image_paths[0]?.startsWith('http')) {
                resultItem.kb_type = KnowledgeType.INTERNAL;
                resultItem.image = await Promise.all(
                  contentItem.image_paths.map(async (path) => {
                    try {
                      const res = await this.kbRepoServ.previewImg(path);
                      return await KbUtils.readFileAsBase64(res);
                    } catch (imgError) {
                      return null;
                    }
                  })
                );
                resultItem.image = resultItem.image.filter(img => img !== null);
              } else {
                resultItem.kb_type = KnowledgeType.EXTERNAL;
                resultItem.image = contentItem.image_paths;
              }
            }
            return resultItem;
          })
        );

        this.total = data.total;
      }
    } catch (error) {
      // Ensure loading state is cleared
    } finally {
      this.isCardLoading = false;
      this.cdr.detectChanges();
    }
  }

  public copyText(text: string) {
    copyToClipboard(text);
  }

  public async deleteHistory(history: IKbSearchRecord) {
    this.nzModal.confirm({
      nzIconType: 'exclamation-circle',
      nzContent: this.i18n.transform('history_delete_content', {
        query: history.query,
      }),
      nzTitle: this.i18n.transform('delete'),
      nzOnOk: async () => {
        await this.kbRepoServ.deleteKbSearchRecord(this.kbId, history.id);
        this.nzMessage.success(this.i18n.transform('delete_success'));
        this.getHistory();
      },
    });
  }

  public handleTabChange(index: number) {
    const tab = this.tabs[index];
    if (!tab) {
      return;
    }
    this.search_mode = tab.search_mode;
    this.currentPage = 1;
    this.getResults();
  }

  #setTabOptions() {
    this.kbAbilitiesService.getKBConnectionId(this.kbId).subscribe(connectionId => {
      this.kbAbilitiesService.getKbRetrieveModes(connectionId).subscribe(res => {
        this.tabs = res.map((mode, index) => {
          return {
            title: mode.title,
            active: !Boolean(index),
            search_mode: mode.value,
          }
        });
        this.search_mode = this.tabs[0]?.search_mode ?? '';
      });
    })
  }

  openModal(pic) {
    this.isModalOpen = true;
    this.base64 = pic;
    document.body.style.overflow = 'hidden';
  }

  closeModal(event: any) {
    if (event.target === event.currentTarget) {
      this.isModalOpen = false;
      document.body.style.overflow = '';
    }
  }

  protected readonly KnowledgeType = KnowledgeType;
}
