import { Component, computed, ElementRef, EventEmitter, Injector, Input, OnDestroy, OnInit, Output, signal, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NzDrawerService } from 'ng-zorro-antd/drawer';
import { CommonNoDataComponent } from '@shared/components/common-no-data/common-no-data.component';
import * as angularI18next from 'angular-i18next';
import { I18NEXT_NAMESPACE, I18NextModule } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { PromptService } from '@services/prompt.service';
import { Router } from '@angular/router';
import { from, Observable, Subject, Subscription } from 'rxjs';
import { MessageComponent } from '@shared/services/cfdata.service';
import { iconList, industryTagColor, ViewTemplateComponent } from '@routes/prompt/prompt-template/view-template/view-template.component';
import { CreateTemplateComponent } from '@routes/prompt/prompt-template/create-template/create-template.component';
import { FormsModule } from '@angular/forms';
import { PromptWritingRepoService } from '@services/repositories/prompt-writing-repo.service';
import { IPaginationListResp, ITmplCard, ITmplCardListResp } from '@interfaces/prompt/prompt-template.interface';
import { CommonUtils } from 'src/utils/common.util';
import { IBaseSelectItem, IBaseSelectResp } from '@interfaces/prompt/common.interface';
import { PromptTmplService } from '@services/prompt-template.service';
import { ApplyPromptComponent } from '../apply-prompt/apply-prompt.component';
import { ConsoleFrameworkService } from '@core/services';
import { PermissionService } from '@services/permission.service';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { CommonService } from '@services/common.service';
import { HttpService } from '@services/http.service';
import { PipesModule } from '../../../../pipes/pipes.module';
import { NewCommonNoDataWithBtnComponent } from '@shared/components/new-common-no-data-with-btn/new-common-no-data-with-btn.component';
import { AssertSquareTagType } from '@routes/agent-center/app-agent/common-logic-agent';
import { SetSidebarVisibilityService } from '@shared/services/set-sidebar-visibility.service';
import { NzIconDirective } from 'ng-zorro-antd/icon';
import { NzInputDirective, NzInputGroupComponent, NzInputGroupWhitSuffixOrPrefixDirective } from 'ng-zorro-antd/input';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { PromptSaveComponent } from '@routes/prompt/prompt-save/prompt-save.component';

enum mapKeys {
  category = 'category',
}

const OTHER_HEIGHT = 252;
const MAX_HEIGHT = 1270;

@Component({
  selector: 'meta-system-template',
  imports: [
    CommonModule,
    NzMenuModule,
    NzPaginationModule,
    NzInputModule,
    NzSelectModule,
    NzSpinModule,
    FormsModule,
    NzIconModule,
    I18NextModule,
    NzModalModule,
    NzButtonModule,
    NzUploadModule,
    NzTableModule,
    NzTagModule,
    PipesModule,
    NewCommonNoDataWithBtnComponent,
    NzDropDownModule,
    NzTabsModule,
    NzIconDirective,
    NzInputDirective,
    NzInputGroupComponent,
    NzInputGroupWhitSuffixOrPrefixDirective,
    NzTableModule,
    NzTagModule,
    NzSpinModule,
    NzTabsModule,
    NzToolTipModule,
  ],
  templateUrl: './system-template.component.html',
  styleUrls: ['./system-template.component.scss', '../prompt-template-common.scss'],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.PROMPT_PLATFORM],
    },
    NzModalService,
  ],
})
export class SystemTemplateComponent implements OnInit, OnDestroy {
  @Input() isImportModal: string;

  @Input() isShowCheckgroup: boolean;

  @Input() isLibrary = false;

  @Output() setContent = new EventEmitter<string>();
  @Output() setCardTotalNumber = new EventEmitter<number>();

  @ViewChild('fileInput') fileInput: ElementRef<HTMLInputElement>;

  @ViewChild('importErrInfoModal')
  importErrInfoModal: TemplateRef<HTMLDivElement>;
  subscribeBtnStatus = this.commonService.getSubscribeStatus();

  private destroy$ = new Subject<void>();

  public tagOptions: { id: string; label: string }[];

  public industryOptions: { id: string; label: string }[];

  public taskId = '';

  public cards: ITmplCard[] = [];

  public cardChecked: string[] | Set<string> = [];

  public selectedCard: ITmplCard;

  public currentCardPage = 1;

  public totalCardNumber = 0;

  public hasData = true;

  public isLodingCard = false;

  public templateId = '';

  public cardPageSize: any = {
    options: [24, 48, 64],
    size: 24,
  };

  hasMore = true;

  public items: Array<any> = [
    {
      id: 'create',
      title: this.i18n.transform('prompt_tmpl_apply2task'),
    },
    {
      id: 'edit',
      title: this.i18n.transform('base_edit'),
    },
    {
      id: 'delete',
      title: this.i18n.transform('base_remove'),
    },
    {
      id: 'copy',
      title: this.i18n.transform('base_copy'),
    },
  ];

  public isOpAccount = false;

  public iErrInfodisplayedData: any[] = [];

  public importErrInfoSrcData: any = {
    data: [],
    state: undefined,
  };

  public importErrInfoColumns: any[] = [
    {
      title: this.i18n.transform('base_num'),
      width: '80px',
    },
    {
      title: this.i18n.transform('base_content'),
    },
  ];

  public searchItems: any[] = [
    {
      label: this.i18n.transform('base_name'),
      field: 'template_name',
    },
    {
      label: 'ID',
      field: 'template_id',
      options: [],
    },
    {
      label: this.i18n.transform('base_content'),
      field: 'content',
    },
    {
      label: this.i18n.transform('prompt_tmpl_industry_label'),
      field: 'industry',
      type: 'checkbox',
      valueKey: 'id',
      options: [],
    },
    {
      label: this.i18n.transform('prompt_tmpl_tag_label'),
      field: 'tags',
      type: 'checkbox',
      valueKey: 'id',
      options: [],
    },
  ];

  public searchedItems: any[] = [];

  protected readonly iconList = iconList;
  protected readonly industryTagColor = industryTagColor;
  public language = this.consoleFrameworkService?.dataService?.getLocale() ?? 'zh-cn';
  public currentPermissions: any;
  private permissionSubscription: Subscription;
  appPromptTabs: AssertSquareTagType[] = [
    {
      name: this.i18n.transform('all'),
      name_en: 'All',
      id: 'all',
      active: true,
      description: '',
      library_type: '',
      created_on: 0,
      updated_on: 0,
    },
  ];
  selectedTabIndex: number = 0;
  lang: string = '';
  searchKey: string = '';
  selectedTagId = signal('');

  pageTitle = computed(() => {
    if (this.selectedTagId() === '') {
      return this.i18n.transform('all');
    }
    const selectTab = this.appPromptTabs.find(a => a.id === this.selectedTagId()) ?? this.appPromptTabs[0];
    const tabName = this.lang === 'zh-cn' ? selectTab.name : selectTab.name_en;
    return [tabName, this.lang === 'zh-cn' ? '' : ' '].join('');
  });

  get totalCount() {
    if (this.searchedItems.length === 0 && this.totalCardNumber === 0) {
      return '';
    }
    return ` (${this.totalCardNumber})`;
  }

  constructor(
    private readonly i18n: angularI18next.I18NextEagerPipe,
    private service: PromptService,
    private injector: Injector,
    private router: Router,
    private nzDrawerService: NzDrawerService,
    private promptTmplServ: PromptTmplService,
    private readonly consoleFrameworkService: ConsoleFrameworkService,
    private permissionService: PermissionService,
    private commonService: CommonService,
    private readonly http: HttpService,
    private promptService: PromptService,
    private sidebarServ: SetSidebarVisibilityService,
    private message: NzMessageService,
    private nzModalService: NzModalService
  ) {
    this.lang = CommonUtils.getLanguage();
  }

  public ngOnInit(): void {
    this.sidebarServ.setSidebarsVisibilityByState('hideSidebar');
    this.getPromptTagData();
    this.getData();
    this.permissionSubscription = this.permissionService.permissions$.subscribe(permissions => {
      this.currentPermissions = permissions;
      this.items.forEach(v => {
        v.disabled = this.hasPermission() || !this.subscribeBtnStatus;
      });
    });
    window.addEventListener('WorkspaceChange', () => {
      this.getData();
    });
  }

  private getPromptTagData() {
    const params = {
      library_type: 'prompt',
    };
    this.promptService
      .getAssertTagAsync(params)
      .then(response => {
        const firstTab = this.appPromptTabs[0];
        this.appPromptTabs.length = 0;
        this.appPromptTabs.push(firstTab);
        for (let index = 0; index < response.length; index++) {
          this.appPromptTabs.push(response[index]);
        }
      })
      .catch();
  }

  public ngOnDestroy(): void {
    this.sidebarServ.setSidebarsVisibilityByState('destroy');
    this.destroy$.next();
    this.destroy$.complete();

    window.removeEventListener('WorkspaceChange', () => {
      this.getData();
    });
    if (this.permissionSubscription) {
      this.permissionSubscription.unsubscribe();
    }
  }

  public getData(): void {
    this.getTagList();
    this.getCardData();
    this.setItems();
  }

  public exportCardData(): void {
    this.service.exportPromptTmpl(this.cardChecked as string[]).subscribe({
      next: res => {
        CommonUtils.downloadFile(
          new Blob([res.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          }),
          res.headers.get('Content-Disposition')
        );
      },
    });
  }

  public importCardData(): void {
    this.fileInput.nativeElement.click();
  }

  public getTableData(requestParams: any): Observable<any> {
    const query = {};
    if (this.selectedTagId()) {
      query[mapKeys.category] = this.selectedTagId();
    }
    return this.service.getPromptTemplateList(requestParams, query);
  }

  public onFileSelected(e: Event): void {
    const file = (e.target as HTMLInputElement).files[0];

    if (!CommonUtils.checkXLSXFileFormat(file)) {
      MessageComponent.showWarn(this.i18n.transform('prompt_tmpl_upload_ext_err'));

      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    this.service.importPromptTmpl(formData).subscribe({
      next: (res: string[]) => {
        this.getCardData();

        if (res.length) {
          this.importErrInfoSrcData.data = res.map((content, index) => ({
            index,
            content,
          }));

          this.nzModalService.error({
            nzTitle: this.i18n.transform('prompt_tmpl_import_err_title'),
            nzContent: this.importErrInfoModal,
            nzOkText: this.i18n.transform('base_confirm'),
            nzWidth: 500,
          });
        } else {
          MessageComponent.showSuccess(this.i18n.transform('base_import_success'));
        }

        this.fileInput.nativeElement.value = null;
      },
      error: () => {
        this.fileInput.nativeElement.value = null;
      },
    });
  }

  public getTagList = () => {
    const params = {
      limit: 1000,
      offset: 0,
    };
    this.service.getTagList(params).subscribe({
      next: (res: IPaginationListResp) => {
        this.tagOptions = res.data.map(tag => {
          return {
            id: tag.id,
            label: this.language === 'en-us' ? tag.nameEn || tag.name : tag?.name,
          };
        });
        this.searchItems[4].options = this.tagOptions;
      },
    });
    this.service.getIndustryList().subscribe({
      next: (data: IBaseSelectResp[]) => {
        this.industryOptions = data.map(ind => {
          return {
            id: ind.id,
            label: this.language === 'en-us' ? ind.name_en || ind.name : ind.name,
          };
        });
        this.searchItems[3].options = this.industryOptions;
      },
    });
  };

  public downloadImportCase(): void {
    this.service.downloadImportTmplCase().subscribe({
      next: res => {
        CommonUtils.downloadFile(
          new Blob([res.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          }),
          res.headers.get('Content-Disposition')
        );
      },
    });
  }

  handleClickRefreshBtn() {
    this.cards.length = 0;
    this.totalCardNumber = this.cards.length;
    this.currentCardPage = 1;
    this.getCardData();
  }

  public handleSearch(searchedItems) {
    this.searchedItems = searchedItems;
    this.cards.length = 0;
    this.totalCardNumber = this.cards.length;
    this.currentCardPage = 1;
    this.getCardData();
  }

  public getCardData() {
    this.isLodingCard = true;
    if (window.innerHeight - OTHER_HEIGHT > MAX_HEIGHT) {
      this.cardPageSize.size = this.cardPageSize.options[1];
    } else {
      this.cardPageSize.size = this.cardPageSize.options[0];
    }
    const params = {
      limit: this.cardPageSize?.size,
      offset: ((this.currentCardPage || 1) - 1) * this.cardPageSize.size,
      template_name: this.searchKey,
      source: 'PRESET',
    };
    this.totalCardNumber = 0;
    from(this.getTableData(params)).subscribe({
      next: (res: ITmplCardListResp) => {
        this.searchItems.find(item => item.field === 'template_id').options = res.data.map(d => ({ label: d.template_id }));
        this.totalCardNumber = res?.count;
        if (res?.count) {
          this.cards.push(...(res.data || []));
          this.currentCardPage++;
          this.hasMore = this.currentCardPage < Math.ceil(res.count / this.cardPageSize.size) + 1;
        } else {
          this.cards = [];
          this.hasMore = false;
        }
        this.setCardTotalNumber.emit(this.totalCardNumber);
        this.hasData = Boolean(res?.count);
        this.isLodingCard = false;
      },
      error: () => {
        this.isLodingCard = false;
        this.hasData = false;
      },
      complete: () => {
        this.isLodingCard = false;
      },
    });
  }

  public deleteTemplate(params: ITmplCard[]) {
    this.service.deletePromptTemplate(params[0].template_id).subscribe({
      next: () => {
        this.cardChecked = this.promptTmplServ.deleteCheckedTmplId(this.cardChecked as string[], params[0].template_id);

        MessageComponent.showSuccess(this.i18n.transform('base_delete_success'));
        this.getCardData();
      },
    });
  }

  public onClick(event: Event, data: any, item: ITmplCard) {
    event.stopPropagation();
    this.onSelect(data, item);
  }

  public hasPermission(): boolean {
    return this.currentPermissions && Object.prototype.hasOwnProperty.call(this.currentPermissions, 'OPERATOR');
  }

  public onSelect(event: any, item: ITmplCard) {
    if (event.id === 'view') {
      this.openCardDetail(item);
    }
    if (event.id === 'edit') {
      this.openEditTemplate(item);
    }
    if (event.id === 'create') {
      this.openCreateTask(item);
    }
    if (event.id === 'copy') {
      navigator.clipboard.writeText(item.content).then(() => {
        this.message.create('success', this.i18n.transform('copied_to_clipboard'));
      });
    }
    if (event.id === 'save') {
      this.openSaveModal(item);
    }
  }

  public openSaveModal(template: ITmplCard) {
    const title = this.i18n.transform('prompt_tmpl_create_prompt');
    let variableList: any[];
    try {
      variableList = JSON.parse(template.variables || '[]').map(item => {
        return {
          type: item.type === 'text' ? 'text' : 'image',
          value: item.name,
        };
      });
    } catch (e) {
      variableList = [];
    }
    const InitInfo = {
      promptId: template.template_id,
      name: template.template_name,
      desc: template.description,
      prompt: template.content,
      tags: template.tag_list,
      industry: template.industry,
      type: template.pt_type,
      variableList: variableList,
    };
    const drawerRef = this.nzDrawerService.create({
      nzWidth: '700px',
      nzContent: PromptSaveComponent,
      nzClosable: true,
      nzData: {
        title: title,
        promptSaveInitInfo: InitInfo,
        savePromptTemplate: promptTemplate => {
          this.service.createPromptV2(promptTemplate).then(res => {
            const href = `${window.location.href.split('#')[0]}#/home/agent-center/library-home?tabId=prompt`;
            MessageComponent.showSuccess(`${this.i18n.transform('prompt_creation_success')} <a href="${href}">${this.i18n.transform('goto_view')}</a>`);
            drawerRef?.close();
          });
        },
      },
    });
  }

  public onPageUpdate(): void {
    this.getCardData();
  }

  public stopCheckItemPropagation(e: Event): void {
    e.stopPropagation();
  }

  public openCardDetail(card: ITmplCard) {
    if (!this.subscribeBtnStatus) {
      return;
    }
    if (!this.isImportModal) {
      const modal = this.nzModalService.create({
        nzTitle: '',
        nzContent: ViewTemplateComponent,
        nzWidth: 800,
        nzClosable: true,
        nzMaskClosable: true,
        nzFooter: null,
      });
      modal.componentInstance.currentTmpl = card;
    }
  }

  public onClickCard(card: ITmplCard) {
    if (this.isImportModal) {
      const element = document.getElementById(this.templateId);

      if (element) {
        element.style.borderColor = '#c2c2c2';
      }

      this.selectedCard = card;
      this.templateId = card.template_id;
      document.getElementById(this.templateId).style.borderColor = 'red';

      if (this.selectedCard?.content) {
        this.setContent.emit(this.selectedCard.content);
      }
    }
  }

  public setMouseEnter(tmpId: string) {
    if (!this.isImportModal) {
      document.getElementById(`menu-labelkey${tmpId}`).style.display = 'block';
    }
  }

  public setMouseLeave(tmpId: string) {
    document.getElementById(`menu-labelkey${tmpId}`).style.display = 'none';
  }

  public openCreateTask(template: ITmplCard) {
    this.nzModalService.create({
      nzTitle: '',
      nzContent: ApplyPromptComponent,
      nzWidth: 544,
      nzClosable: true,
      nzMaskClosable: true,
      nzFooter: null,
    });
  }

  public openCreateTemplate() {
    this.nzModalService.create({
      nzTitle: this.i18n.transform('system_tmpl_create_prompt'),
      nzContent: CreateTemplateComponent,
      nzWidth: 1000,
      nzClosable: true,
      nzMaskClosable: true,
      nzFooter: null,
      nzOnOk: () => {
        this.getCardData();
      },
    });
  }

  public openEditTemplate(params: any) {
    const modal = this.nzModalService.create({
      nzTitle: this.i18n.transform('system_tmpl_edit_prompt'),
      nzContent: CreateTemplateComponent,
      nzWidth: 1000,
      nzClosable: true,
      nzMaskClosable: true,
      nzFooter: null,
      nzOnOk: () => {
        this.getCardData();
      },
    });
    modal.componentInstance.data = params;
  }

  public isExportDisabled(): boolean {
    return (this.cardChecked as string[]).length === 0;
  }

  public getCheckedCardBorderStyle(id: string): Record<string, string> {
    return this.promptTmplServ.getCheckedCardStyle(this.cardChecked as string[], id);
  }

  private setItems(): void {
    const items = [
      {
        id: 'copy',
        title: this.i18n.transform('copy_prompt'),
      },
      {
        id: 'save',
        title: this.i18n.transform('save_to_my_prompt'),
      },
    ];
    this.items = items;
  }

  protected readonly changeUrl = cdnAssetUrl;

  changeAppTab(data: AssertSquareTagType) {
    this.appPromptTabs.forEach(item => (item.active = false));
    this.appPromptTabs[this.selectedTabIndex].active = true;
    if (data.active) {
      this.cards = [];
      this.totalCardNumber = this.cards.length;
      this.currentCardPage = 1;
      this.setCardTotalNumber.emit(this.totalCardNumber);
      if (data.id === 'all') {
        this.selectedTagId.set('');
      } else {
        this.selectedTagId.set(data.id);
      }
      this.getCardData();
    }
  }

  loadMore(): void {
    if (this.isLodingCard || !this.hasMore) {
      return;
    }
    this.isLodingCard = true;
    this.getCardData();
  }

  onScroll(): void {
    const scrollableElement = document.querySelector('.card-container');
    if (scrollableElement) {
      const { scrollTop, scrollHeight, clientHeight } = scrollableElement;
      if (scrollHeight - (scrollTop + clientHeight) < 100) {
        this.loadMore();
      }
    }
  }

  public handleClickClearData() {
    this.searchedItems = [];
    this.searchKey = '';
    this.currentCardPage = 1;
    this.getCardData();
  }
}
