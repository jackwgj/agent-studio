import { Component, EventEmitter, inject, Input, OnInit, Output, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import {
  KnowledgeBaseListService
} from '@routes/knowledge-center/knowledge-base-list/knowledge-base-list.component.service';
import { ReferenceModalComponent } from '@routes/knowledge-center/knowledge/reference-modal/reference-modal.component';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { KbOperationFilterService } from '@services/knowledge-center/kb-operation-filter.service';
import { MODULES } from '@shared/modules';
import { DeleteRefsService } from '@shared/services/delete-refs.service';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import {
  knowledgeBaseDeleteTitleMap,
  knowledgeBaseSourceMap,
  knowledgeBaseStatusMap, KnowledgeType,
} from '@routes/knowledge-center/knowledge-base-list/knowledge-base-list.map';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { KnowledgeBaseEditComponent } from "@routes/knowledge-center/components/knowledge-base-edit/knowledge-base-edit.component";
import { KnowledgeFormDrawerComponent } from "@routes/knowledge-center/knowledge-form-drawer/knowledge-form-drawer.component";
import { I18nNamespace } from '@i18n';
import { PipesModule } from "../../../../pipes/pipes.module";
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzDrawerService } from 'ng-zorro-antd/drawer';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'knowledge-base-overview',
  templateUrl: './knowledge-base-overview.component.html',
  styleUrls: ['./knowledge-base-overview.component.less'],
  standalone: true,
  imports: [
    MODULES,
    KnowledgeBaseEditComponent,
    KnowledgeFormDrawerComponent,
    PipesModule,
    NzDropDownModule,
    NzIconModule,
    NzTagModule,
    NzToolTipModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.COMMON,
        I18nNamespace.AGENT_CENTER,
      ],
    },
    DeleteRefsService,
    NzModalService,
    NzDrawerService,
    NzMessageService
  ],
})
export class KnowledgeBaseOverviewComponent implements OnInit {
  @Input() knowledgeBaseData: any;
  @Input() pageType: string;
  @Output() refreshList = new EventEmitter();
  @ViewChild('knowledgeBaseEdit') knowledgeBaseEdit: KnowledgeBaseEditComponent;

  @Input() isFromDevelopSpace: boolean = false;
  @Input() isFromOverview: boolean = false;

  private readonly kbOperationFilterService = inject(KbOperationFilterService);
  constructor(
    private router: Router,
    private knowledgeRepoService: KnowledgeRepoService,
    public readonly i18n: I18NextEagerPipe,
    public kbBaseService: KnowledgeBaseListService,
    public kbAbilitiesService: KbAbilitiesService,
    private nzModal: NzModalService,
    private nzDrawer: NzDrawerService,
    private nzMessage: NzMessageService
  ) {}

  ngOnInit(): void {}

  public handleDetailView() {
    const queryParams: Record<string, string> = {};
    if (this.isFromOverview) {
      queryParams.from = 'overview';
    }
    if (this.knowledgeBaseData?.type !== 'external') {
      this.router.navigate([`/home/knowledge-center/knowledge/${this.knowledgeBaseData.knowledge_base_id}`], {
        queryParams: queryParams,
      });
    }
  }

  dataToItemsFn = (data: any): Array<any> => {
    return this.kbOperationFilterService.getKbOperations(data);
  };

  public clickMenu(e: Event) {
    e.stopPropagation();
  }

  // 列表操作
  public listAction($event: any, row: any) {
    if ($event.key === 'delete') {
      this.createListDeleteWarn(row);
    } else if ($event.key === 'runOrStop') {
      if (row.status === 'OPEN') {
        this.createListStopWarn(row);
      } else if (row.status === 'CLOSE') {
        this.knowledgeRepoService.startKb(row.knowledge_base_id).then(() => {
          this.refreshList.emit({ action: 'runOrStop' });
        });
      }
    } else if ($event.key === 'test') {
      this.router.navigate(
        [
          `/home/knowledge-center/knowledge/${row.knowledge_base_id}/hit-testing`,
        ],
        {
          queryParams: {
            display_name: row.name,
            id: row.knowledge_base_id,
          },
        },
      );
    } else if ($event.key === 'advanced') {
      this.openAdvancedHalfModal('advanced', row);
    } else if ($event.key === 'edit') {
      this.knowledgeBaseEdit.openEditKnowledgeBase(row);
    } else if ($event.key === 'kbReference') {
      this.nzDrawer.create({
        nzContent: ReferenceModalComponent,
        nzWidth: '900px',
        nzMaskClosable: true,
        nzData: {
          kbId: row.knowledge_base_id,
        }
      });
    }
  }

  createListDeleteWarn(data: any): void {
    const isInternal = data.type === KnowledgeType.INTERNAL;

    this.nzModal.confirm({
      nzType: 'warning',
      nzTitle: `${knowledgeBaseDeleteTitleMap.get(data.type)}`,
      nzContent: this.i18n.transform('confirm_action_tip', { action: knowledgeBaseDeleteTitleMap.get(data.type), name: data.name }),
      nzOkText: this.i18n.transform('ok'),
      nzOkDanger: true,
      nzCancelText: this.i18n.transform('cancel'),
      nzCancelButtonProps: isInternal ? {} : { style: { display: 'none' } },
      nzOnOk: () => new Promise<void>((resolve, reject) => {
        this.knowledgeRepoService.deleteKb(data.knowledge_base_id).then(() => {
          this.refreshList.emit({ action: 'delete' });
          resolve();
        }).catch(err => reject(err));
      })
    });
  }

  createListStopWarn(data: any): void {
    this.nzModal.confirm({
      nzTitle: this.i18n.transform('confirm_disable_knowledge_base'),
      nzContent: this.i18n.transform('disable_knowledge_base_usage_hint'),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOnOk: () => new Promise<void>((resolve, reject) => {
        this.knowledgeRepoService.stopKb(data.knowledge_base_id).then(() => {
          this.refreshList.emit({ action: 'runOrStop' });
          this.nzMessage.success(this.i18n.transform('knowledge_close_success'));
          resolve();
        }).catch(err => reject(err));
      })
    });
  }

  // 高级
  public async openAdvancedHalfModal(type: string, data: any): Promise<void> {
    let detail = await this.knowledgeRepoService.retrieveKb(data.knowledge_base_id);

    const drawerRef = this.nzDrawer.create<KnowledgeFormDrawerComponent, any>({
      nzContent: KnowledgeFormDrawerComponent,
      nzWidth: '700px',
      nzPlacement: 'right',
      nzMaskClosable: false,
      nzData: {
        type,
        formData: detail,
      }
    });

    const instance = drawerRef.getContentComponent();
    instance.submitForm.subscribe(async (ctx: any) => {
      const { form, setConfirmLoading } = ctx;
      try {
        setConfirmLoading(true);
        await this.knowledgeRepoService.modifyKb(data.knowledge_base_id, form);
        this.nzMessage.success(this.i18n.transform('update_success'));
        drawerRef.close();
        this.refreshList.emit({ action: 'advanced' })
      } finally {
        setConfirmLoading(false);
      }
    });

    instance.cancelForm.subscribe(() => drawerRef.close());
  }

  public isStatusError(row: any) {
    return !row.source?.knowledge_base_connection_id && row.type === 'external';
  }

  // 复制链接功能
  public copyUrl(event: MouseEvent, url: string) {
    event.stopPropagation(); // 阻止冒泡跳转到详情页
    if (url) {
      navigator.clipboard.writeText(url).then(() => {
        this.nzMessage.success(this.i18n.transform('copy_success'));
      }).catch(() => {
        this.nzMessage.error('Failed to copy');
      });
    }
  }

  protected readonly knowledgeBaseStatusMap = knowledgeBaseStatusMap;
  protected readonly knowledgeBaseSourceMap = knowledgeBaseSourceMap;
}
