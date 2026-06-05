import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { CommonNoDataWithBtnComponent } from '@shared/components/common-no-data-with-btn/common-no-data-with-btn.component';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { KnowledgeFormDrawerComponent } from '../knowledge-form-drawer/knowledge-form-drawer.component';
import { environment } from 'src/environment/environment';
import { KnowledgeCardComponent } from '../knowledge-card/knowledge-card.component';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';

import { NzDrawerService, NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';

export interface SearchboxItem {
  label: string;
  field: string;
  options: any[];
}

@Component({
  selector: 'meta-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MODULES,
    CommonNoDataWithBtnComponent,
    KnowledgeCardComponent,
    NzDrawerModule,
    NzButtonModule,
    NzInputModule,
    NzSelectModule,
    NzIconModule,
    NzSpinModule,
    NzPaginationModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.COMMON,
      ],
    },
    NzDrawerService
  ],
})
export class HomeComponent implements OnInit {
  public loading = false;

  public searchItems: SearchboxItem[] = [];

  public searchField: string = 'name';
  public searchValue: string = '';

  public currentPage = 1;

  public total = 0;

  public pageSize = {
    options: [12, 24, 48, 64],
    size: 12,
  };

  public knowledges = [];

  constructor(
    private readonly i18n: I18NextEagerPipe,
    private nzDrawerService: NzDrawerService,
    private kbRepo: KnowledgeRepoService,
  ) {}

  ngOnInit(): void {
    // POC版本，知识库列表查询接口不支持id过滤
    this.searchItems =
      environment.envType === 'poc'
        ? [{ label: this.i18n.transform('knowledge_name'), field: 'name', options: [] }]
        : [
          { label: this.i18n.transform('knowledge_name'), field: 'name', options: [] },
          { label: this.i18n.transform('knowledge_id'), field: 'id', options: [] },
        ];

    this.searchField = this.searchItems[0]?.field || 'name';
    this.getData();
  }

  public async getData() {
    try {
      this.loading = true;
      const searchParams: any = {};
      if (this.searchValue) {
        searchParams[this.searchField] = this.searchValue;
      }

      const { size } = this.pageSize;
      const { knowledge_repo_list, count } =
        await this.kbRepo.getKnowledges({
          ...searchParams,
          limit: size,
          offset: (this.currentPage - 1) * size,
        });
      this.knowledges = knowledge_repo_list;
      this.total = count;
    } finally {
      this.loading = false;
    }
  }

  public onSearch() {
    this.currentPage = 1;
    this.getData();
  }

  public openHalfModel() {
    const drawerRef = this.nzDrawerService.create<KnowledgeFormDrawerComponent, any, any>({
      nzTitle: this.i18n.transform('create_kb'),
      nzContent: KnowledgeFormDrawerComponent,
      nzWidth: '700px',
      nzPlacement: 'right',
      nzMaskClosable: true
    });

    const instance = drawerRef.getContentComponent();

    instance.submitForm.subscribe(
      async ({ form, setConfirmLoading }) => {
        try {
          setConfirmLoading(true);
          await this.kbRepo.createKb(form);
          drawerRef.close();
          this.onSearch();
        } finally {
          setConfirmLoading(false);
        }
      },
    );

    instance.cancelForm.subscribe(() =>
      drawerRef.close()
    );
  }

  public handleAction(action: string) {
    if ('delete' === action) {
      // 如果删除知识库时当前页不是首页并且有且仅有一条数据，页码减一
      const currentPageCount = this.knowledges.length;
      if (1 === currentPageCount && 1 !== this.currentPage) {
        this.currentPage -= 1;
      }
    }
    this.getData();
  }
}
