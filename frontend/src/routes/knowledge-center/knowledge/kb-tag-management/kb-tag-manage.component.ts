import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { Network } from '@model';
import { TagCreationComponent } from '@routes/knowledge-center/components/tag-creation/tag-creation.component';
import { MAX_KB_TAG_LIMIT } from '@routes/knowledge-center/knowledge.const';
import { KbTagItem } from '@routes/knowledge-center/knowledge.types';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { FormateTimePipe } from 'src/pipes/formate-time.pipe';
import { BatchDeleteComponent } from '../batch-delete.component';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzModalService, NzModalModule } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'kb-tag-manage',
  templateUrl: './kb-tag-manage.component.html',
  styleUrls: ['./kb-tag-manage.component.less'],
  standalone: true,
  imports: [
    MODULES,
    TagCreationComponent,
    FormsModule,
    NzTableModule,
    NzButtonModule,
    NzInputModule,
    NzIconModule,
    NzModalModule,
    NzToolTipModule,
    NzEmptyModule,
    NzTypographyModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.KNOWLEDGE,
      ],
    },
    FormateTimePipe,
    NzModalService,
    NzMessageService
  ],
})
export class KbTagManageComponent implements OnInit {
  @Input() kbId: string;
  @Input() isMarket = false;

  @ViewChild('batchDeleteModal') batchDeleteModal: BatchDeleteComponent;
  @ViewChild('tagCreationComponent') tagCreationComponent: TagCreationComponent;

  totalCount = 0;
  loading = false;
  srcData: KbTagItem[] = [];
  filteredData: KbTagItem[] = [];
  searchValue = '';
  sortOrder: string | null = null;
  emptyImage = `${Network.ASSETS_PATH}images/noData.svg`;

  public operationItems = [
    {
      label: this.i18n.transform('common_new'),
      click: () => {
        this.tagCreationComponent.showCreationModal(() => {
          this.refreshGridData();
        });
      },
      tip: '',
      disabled: false,
    },
  ];

  constructor(
    private kbRepoServ: KnowledgeRepoService,
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
    private i18n: I18NextEagerPipe,
    public kbAbilitiesService: KbAbilitiesService,
    private formateTimePipe: FormateTimePipe,
  ) {}

  ngOnInit() {
    this.updateOperationItemState();
    this.refreshGridData();
  }

  public refreshGridData() {
    this.loading = true;
    this.kbRepoServ.getKbTags(this.kbId, { offset: 0, limit: 100 })
      .then((val) => {
        const data = val?.tag_list ?? [];
        data.forEach(item => {
          item.create_time = this.formateTimePipe.transform(item.create_time);
        });

        this.srcData = data;
        this.totalCount = val?.count ?? 0;
        this.updateOperationItemState();
        this.applyFilterAndSort();
      })
      .catch(() => {
        this.srcData = [];
        this.totalCount = 0;
        this.updateOperationItemState();
        this.applyFilterAndSort();
      })
      .finally(() => {
        this.loading = false;
      });
  }

  private updateOperationItemState() {
    const isFull = this.totalCount === MAX_KB_TAG_LIMIT;
    this.operationItems[0].disabled = this.kbAbilitiesService.isResourceDisabled || isFull;
    this.operationItems[0].tip = isFull
      ? this.i18n.transform('MAX_KB_TAG_LIMIT', { maxNumber: MAX_KB_TAG_LIMIT })
      : '';
  }

  public onSearchChange(value: string) {
    this.searchValue = value;
    this.applyFilterAndSort();
  }

  public onSortChange(order: string | null) {
    this.sortOrder = order;
    this.applyFilterAndSort();
  }

  private applyFilterAndSort() {
    let data = [...this.srcData];

    if (this.searchValue) {
      const lowerSearch = this.searchValue.toLowerCase();
      data = data.filter(item => item.name?.toLowerCase().includes(lowerSearch));
    }

    if (this.sortOrder) {
      data.sort((a, b) => {
        const timeA = a.create_time || '';
        const timeB = b.create_time || '';

        return this.sortOrder === 'ascend'
          ? timeA.localeCompare(timeB)
          : timeB.localeCompare(timeA);
      });
    }

    this.filteredData = data;
  }

  public onDeleteTag(data: KbTagItem) {
    if (this.kbAbilitiesService.isResourceDisabled) return;

    this.nzModal.confirm({
      nzTitle: this.i18n.transform('common_del_tip'),
      nzContent: this.i18n.transform('tag_del_warn_tip'),
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: async () => {
        try {
          await this.kbRepoServ.delKbTag(this.kbId, data.id, { tag_name: data.name });
          this.nzMessage.success(this.i18n.transform('delete_success'));
          this.refreshGridData();
          return true;
        } catch (error) {
          return false;
        }
      }
    });
  }
}
