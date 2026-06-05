import {Component, computed, signal, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

import {Subject, takeUntil} from 'rxjs';

import {I18NEXT_NAMESPACE, I18NextEagerPipe} from 'angular-i18next';
import {MaxModelLenType, MaxModelLenTypeMap, SortByField,} from '@routes/model-square/enums';
import {QueryOptions} from '@routes/model-square/common.interface';
import {COMMON_MODULES, MODULES} from '@shared/modules';
import {ModelServiceCardsRender} from '@shared/components/card/card-renderer/card-renderer.component';
import {I18nNamespace} from '@i18n';
import {cdnAssetUrl} from 'src/single-spa/assets-url';
import {NewCommonNoDataWithBtnComponent} from '@shared/components/new-common-no-data-with-btn/new-common-no-data-with-btn.component';
import {ModelSquareSubService} from '@routes/model-square/model-square-sub.service';
import {NzModalService} from 'ng-zorro-antd/modal';
import {ModelModalComponent} from '@routes/model-square/components/model-modal/model-modal.component';
import {AuthModalComponent} from '@routes/model-management/components/auth-modal/auth-modal.component';
import {SendRequestService} from '@routes/subscription/send-request.service';
import {CommonService} from '@services/common.service';
import {ModelManagementService} from '@services/repositories/model-management-new';
import {StorageService} from '@shared/services/cfdata.service';
import {HttpService} from '@services/http.service';
import {AUTH_RESULT, AUTH_TIP_BTN, AUTH_TIP_TEXT,} from '@routes/platform-management/authorization-management/model-authorization/model-authorization.interface';
import {AgentConfigService} from '@routes/agent-center/agent-config.service';
import {SetSidebarVisibilityService} from '@shared/services/set-sidebar-visibility.service';
import {AssertSquareTagType} from '@routes/agent-center/app-agent/common-logic-agent';
import {PE_SESSION_KEY} from '@constants/exp-tmpl-config.const';
import {ICFUser} from '@interfaces/prompt/common.interface';

@Component({
  selector: 'model-square-list',
  templateUrl: './model-square-list.component.html',
  styleUrls: ['./model-square-list.component.less'],
  standalone: true,
  imports: [
    MODULES,
    ...COMMON_MODULES,
    ModelServiceCardsRender,
    NewCommonNoDataWithBtnComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.JIUWEN_MODEL, I18nNamespace.MODEL_ACCESS],
    },
  ],
})
export class ModelSquareListComponent {
  public changeUrl = cdnAssetUrl;

  public authResult$: Subject<AUTH_RESULT> = new Subject(); // tip组件实例

  public listViewInfo: any;
  public isLoading = false;
  public templateList: Array<any> = [];
  // 页面加载状态
  // public pageState: LoadingStatusEnum = LoadingStatusEnum.LOADING;
  public originTemplateList: Array<any> = [];
  // 选中tags
  public searchTags: Array<any> = [];
  public searchPresetItemMap = {};
  public searchText = '';
  public localSearchTags = 'localSearchTags';
  // 模型类型、上下文长度、系列
  public searchFilterTags: string[] = [
    'model_type',
    'context_length',
    'label_feature',
    'model_series',
  ];
  // 筛选项
  public searchItems: Array<any>;
  // 创建Subject用于处理输入流
  private searchSubject = new Subject<string>();
  // 用于取消订阅的可观察对象
  private destroy$ = new Subject<void>();
  public showAlert: boolean = true;
  public authTipInfo: any = {
    tip: '',
    btn: '',
    link: '',
  };

  public resourcesCurrentValue: number = 0;
  prefix: string = '/v1';
  public provider_id = '';
  public authStatus: boolean = false;
  public currentPage = 1;

  public totalNumber = 0;

  public isModelSubscribed: boolean = false;

  public filterParams: any = {};

  public noDataStr: string = this.i18n.transform('empty_data');

  subscribeBtnStatus = this.commonService.getSubscribeStatus();


  isTenantAdmin = false;

  activeTabId = signal('');

  selectedTabIndex = computed(() => {
    const index = this.appTabs.findIndex(tab => tab.id === this.activeTabId());
    return index >= 0 ? index : 0;
  });

  searchValue: string = '';
  openText = this.i18n.transform('model_open', {
    ns: I18nNamespace.PLATFORM_MANAGEMENT,
  });
  closeText = this.i18n.transform('model_close', {
    ns: I18nNamespace.PLATFORM_MANAGEMENT,
  });
  items: Array<any> = [
    {
      label: this.i18n.transform('model_name'),
      field: 'service_name',
    },
    {
      label: this.i18n.transform('subscribe_status'),
      field: 'is_subscribed',
      options: [{ label: this.openText }, { label: this.closeText }],
    },
  ];

  appTabs: AssertSquareTagType[] = [
    {
      name: this.i18n.transform('all'),
      id: 'all',
      active: true,
      description: '',
      library_type: '',
      created_on: 0,
      updated_on: 0,
    },
  ];

  modelTypeMap = {
    LLM: this.i18n.transform('LLM'),
    'TEXT-TO-IMAGE': this.i18n.transform('TEXT-TO-IMAGE'),
    'IMAGE-TO-TEXT': this.i18n.transform('IMAGE-TO-TEXT'),
    'TEXT-TO-VIDEO': this.i18n.transform('TEXT-TO-VIDEO'),
    'Text-Embedding': this.i18n.transform('EMBEDDING'),
    'Text-Multimodal-Embedding': this.i18n.transform(`${I18nNamespace.PLATFORM_MANAGEMENT}:model_multimodal_embedding`),
    RERANK: this.i18n.transform('RERANK'),
  };

  pageTitle = computed(() => {
    const activeTabId = this.activeTabId();
    const title = this.appTabs.find(a => a.id === activeTabId)?.name ?? this.appTabs[0].name;
    return `${title} ${this.getModelCount()}`;
  });

  private modelCount = signal(0);

  constructor(
    public activatedRoute: ActivatedRoute,
    public router: Router,
    public i18n: I18NextEagerPipe,
    public commonService: CommonService,
    public modelSquareSubService: ModelSquareSubService,
    private nzModal: NzModalService,
    private modelManagementService: ModelManagementService,
    private sendRequest: SendRequestService,
    private readonly http: HttpService,
    public configServ: AgentConfigService,
    private sidebarServ: SetSidebarVisibilityService,
  ) {
    this.modelSquareSubService
      .clearUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value: boolean) => {
        if (value) {
          const { platform_provider_id } = this.configServ.getConfigs();
          this.provider_id = platform_provider_id || '100';
          this.filterParams = {};
          if (this.activeTabId()) {
            this.filterParams.model_types = this.activeTabId();
          }
          this.getData(this.filterParams);
        }
      });
    this.judgeUserRoles();
  }

  private judgeUserRoles() {
    let userInfo = StorageService.getSessionStorage(
      PE_SESSION_KEY,
    ) as ICFUser | null;
    let userRoles = userInfo.userRoles;
    if (Array.isArray(userRoles) && userRoles.includes('te_admin')) {
      this.isTenantAdmin = true;
    }
  }

  ngOnInit() {
    this.sidebarServ.setSidebarsVisibilityByState('hideSidebar');
    const { platform_provider_id } = this.configServ.getConfigs();
    this.provider_id = platform_provider_id || '100';
    this.getAuthStatus();
    this.setSearchItems();
    this.getData({ invoke: 'init' });
    this.authResult$.subscribe((data: AUTH_RESULT) => {
      if (data && data !== AUTH_RESULT.FAILED) {
        this.authTipInfo.tip = this.i18n.transform(AUTH_TIP_TEXT[data]);
        this.authTipInfo.btn = this.i18n.transform(AUTH_TIP_BTN[data]);
        if (data === 'exceed_limit') {
          this.authTipInfo.btn = this.i18n.transform('square_btn.know');
        }
        this.handleTip(true);
      }
    });
  }

  ngOnDestroy(): void {
    this.sidebarServ.setSidebarsVisibilityByState('destroy');
    this.destroy$.next();
    this.destroy$.complete();
  }

  public filterChange(event: object): void {
    this.searchTags = [];
    this.searchItems.forEach((searchItem) => {
      const value = event[searchItem.field];
      if (value) {
        this.searchTags.push({
          field: searchItem.field,
          label: searchItem.label,
          type: searchItem.type,
          value, // 多值用|分开
        });
      }
    });
    this.storeSearchTags();
    this.onComboSearch(this.searchTags);
    this.listViewInfo = {
      ...this.listViewInfo,
      srcData: this.templateList,
    };
  }

  // 实际执行搜索的方法（含防抖）
  private performSearch(): void {
    if (this.listViewInfo) {
      this.onComboSearch(this.searchTags);
      this.listViewInfo = {
        ...this.listViewInfo,
        srcData: this.templateList,
      };
    }
  }

  public getCountString(): string {
    const leftBracket = this.i18n.transform('common_term_left_bracket');
    const rightBracket = this.i18n.transform('common_term_right_bracket');
    return `${this.i18n.transform('model-Selection')}${leftBracket}${
      this.templateList.length
    }${rightBracket}`;
  }

  // 暂存筛选字段
  private storeSearchTags(): void {
    window.sessionStorage.setItem(
      this.localSearchTags,
      JSON.stringify(this.searchTags),
    );
  }

  // limit 1000, 查询全部的模型
  public queryOptions: QueryOptions = {
    offset: 0,
    limit: 1000,
    order: "asc",
    sort_by: SortByField.DISPLAY_INDEX,
    preset_model: 'true',
  };

  // 判断是否包含输入的模型名称
  public hasContainName(pre: string, next: string): boolean {
    let flag = false;
    if (!next) {
      return flag;
    }
    flag = pre.toLowerCase().includes(next.toLowerCase());

    return flag;
  }

  /**
   * 判断两个数组是否有公共元素
   * @param pre
   * @param next
   */
  public hasCommonItem(pre: string[], next: string[]): boolean {
    if (!pre?.length || !next?.length) {
      return false;
    }

    // 去掉字符串前后的空格
    const nextTrimmed = next.map((item) => item.trim());

    return pre.some((item: string) => nextTrimmed.includes(item.trim()));
  }

  public onComboSearch(data: Array<any>): void {
    // 先根据用户的输入过滤
    if (this.searchText && this.searchText.trim().length > 0) {
      this.templateList = this.templateList.filter((template) =>
        this.hasContainName(template.model_name, this.searchText),
      );
    }

    this.filterParams = {};
    data.forEach((item) => {
      const { field, value } = item;
      switch (field) {
        case 'model_series':
          this.filterParams.model_series = [];
          value.split('|').map((val) => {
            this.filterParams.model_series.push(val.trim());
          });
          break;
        case 'model_type':
          this.filterParams.model_types = [];
          value.split('|').map((val) => {
            this.filterParams.model_types.push(val.trim());
          });
          break;
        case 'context_length':
          this.filterParams.context_length = [];
          value.split('|').map((val) => {
            this.filterParams.context_length.push(val.trim());
          });
          break;
        case 'label_feature':
          value.split('|').map((val) => {
            this.filterParams[val] = true;
          });
          break;
        default:
          break;
      }
    });
    this.getData(this.filterParams);
  }

  filterOperateTypes(value: string): void {}

  handleRefresh() {
    this.onAdvancdeSearchChange(null);
  }

  // 收集到所有的筛选项
  public setSearchItemMap(elem: any): void {
    this.searchFilterTags.forEach((key) => {
      // 去掉导入数据前后的空格
      // 当前数据类型只有两种: 字符和字符组成的数组
      let val = elem[key];
      if (typeof val === 'string') {
        val = val.trim();
      } else {
        // 考虑返回内容为null的情况
        if (val) {
          val = val.map((item) => item.trim());
        }
      }

      if (
        !Object.prototype.hasOwnProperty.call(this.searchPresetItemMap, key)
      ) {
        this.searchPresetItemMap[key] = [];
      }
      if (typeof val === 'string') {
        this.searchPresetItemMap[key].push(val);
      } else if (Array.isArray(val)) {
        this.searchPresetItemMap[key].push(...val);
      }
    });
  }

  // 设置searchbox的筛选项
  public setSearchItems(): void {
    this.searchItems = [
      {
        label: this.i18n.transform('model_table_columns_type'),
        field: 'model_type',
        type: 'checkbox',
        options: [
          {
            label: this.i18n.transform('LLM'),
            id: 'LLM',
          },
          {
            label: this.i18n.transform('TEXT-TO-IMAGE'),
            id: 'TEXT-TO-IMAGE',
          },
          {
            label: this.i18n.transform('IMAGE-TO-TEXT'),
            id: 'IMAGE-TO-TEXT',
          },
          {
            label: this.i18n.transform('TEXT-TO-VIDEO'),
            id: 'TEXT-TO-VIDEO',
          },
          {
            label: this.i18n.transform('EMBEDDING'),
            id: 'Text-Embedding',
          },
          {
            label: this.i18n.transform('RERANK'),
            id: 'RERANK',
          },
        ],
      },
      {
        label: this.i18n.transform('model_len_label'),
        field: 'context_length',
        type: 'checkbox',
        options: [
          {
            label: MaxModelLenType.K64PLUS,
            id: MaxModelLenTypeMap.K64PLUS,
          },
          {
            label: MaxModelLenType.K32,
            id: MaxModelLenTypeMap.K32,
          },
          {
            label: MaxModelLenType.K16,
            id: MaxModelLenTypeMap.K16,
          },
          {
            label: MaxModelLenType.K8MINUS,
            id: MaxModelLenTypeMap.K8MINUS,
          },
        ],
      },
      {
        label: this.i18n.transform('model_square_ability'),
        field: 'label_feature',
        type: 'checkbox',
        options: [
          {
            label: 'Function Call',
            id: 'is_support_function_call',
          },
          {
            label: this.i18n.transform('deep_research'),
            id: 'is_support_deep_thinking',
          },
        ],
      },
      {
        label: this.i18n.transform('model_series_title'),
        field: 'model_series',
        type: 'checkbox',
        options: [
          {
            label: 'DeepSeek',
            id: 'DeepSeek',
          },
          {
            label: this.i18n.transform('qwen'),
            id: this.i18n.transform('qwen'),
          },
          {
            label: this.i18n.transform('qwen3'),
            id: this.i18n.transform('qwen3'),
          },
          {
            label: this.i18n.transform('qwen_image'),
            id: this.i18n.transform('qwen_image'),
          },
          {
            label: 'Kimi',
            id: 'Kimi',
          },
          {
            label: 'LongCat',
            id: 'LongCat',
          },
          {
            label: 'Deepseek Coder',
            id: 'Deepseek Coder',
          },
          {
            label: 'Ling',
            id: 'Ling',
          },
          {
            label: this.i18n.transform('wan2.2'),
            id: this.i18n.transform('wan2.2'),
          },
          {
            label: 'Stable Diffusion',
            id: 'Stable Diffusion',
          },
          {
            label: 'BGE',
            id: 'BGE',
          },
        ],
      },
    ];
  }

  public handleOperateTypeSearchItems(list: Array<{ label; id }>): void {
    const temp = {};
    const { length } = list;
    for (let i = length - 1; i >= 0; i--) {
      const { label, id } = list[i];
      if (Object.prototype.hasOwnProperty.call(temp, label)) {
        temp[label] += `|${id}`;
        list.splice(i, 1);
      } else {
        temp[label] = id;
      }
    }
    list.forEach((item) => {
      const { label } = item;
      item.id = temp[label];
    });
  }

  /**
   * 获取模型上下文长度搜索标识
   */
  private getMaxModelLenText(maxModelLen: string): string[] {
    let maxModelLenArray: string[] = [];
    const len = Number(maxModelLen);
    if (len === 128 * 1024) {
      maxModelLenArray = [MaxModelLenType.K128];
    } else if (len === 64 * 1024) {
      maxModelLenArray = [MaxModelLenType.K64];
    } else if (len === 32 * 1024) {
      maxModelLenArray = [MaxModelLenType.K32];
    } else if (len === 16 * 1024) {
      maxModelLenArray = [MaxModelLenType.K16];
    } else if (len === 0) {
      maxModelLenArray = [];
    } else if (len <= 8 * 1024) {
      maxModelLenArray = [MaxModelLenType.K8MINUS];
    }
    return maxModelLenArray;
  }

  onOpenChange(openState: boolean): void {
    this.showAlert = openState;
  }

  public handleClickClearSearch() {
    this.modelSquareSubService.clearFilter(true);
    this.searchText = undefined;
    this.searchValue = '';
  }

  handleTip(value: boolean) {
  }

  openModel() {
    if (!this.subscribeBtnStatus) {
      return;
    }
    const modalRef = this.nzModal.create({
      nzTitle: '',
      nzContent: ModelModalComponent,
      nzWidth: 500,
      nzFooter: null,
      nzClosable: false,
      nzMaskClosable: false,
    });
    modalRef.afterClose.subscribe((authResult: string | undefined) => {
      if (authResult) {
        this.authResult$.next(authResult as AUTH_RESULT);
      }
      this.handleRefresh();
    });
  }

  public showAuthModal(item) {
    if (this.subscribeBtnStatus) {
      item.id = this.provider_id;
      item.provider_name = 'ModelArts Studio(MaaS)';
      const modalRef = this.nzModal.create({
        nzTitle: '',
        nzContent: AuthModalComponent,
        nzWidth: 400,
        nzFooter: null,
        nzClosable: false,
      });
      const instance = modalRef.getContentComponent();
      instance.id = '';
      instance.provider_info = item;
    }
  }

  onClearFn() {
    this.filterParams = {};
    this.getData(this.filterParams);
  }

  getData = (params) => {
    const param = {
      provider_id: this.provider_id,
      page_num: this.currentPage,
      page_size: 500,
      ...params,
    };
    this.isLoading = true;

    // 搜索栏订阅状态值非布尔，则数据不存在
    if (
      params.is_subscribed !== null &&
      params.is_subscribed !== undefined &&
      typeof params.is_subscribed === 'boolean'
    ) {
      this.isLoading = false;
      this.templateList = [];
      return;
    }
    this.modelCount.set(0);
    this.modelManagementService
      .getModelList(param)
      .then((res) => {
        this.templateList = res.data ?? [];

        const data = new Date().getTime();
        this.templateList.forEach((item) => {
          item.tag =
            data - item?.last_updated_date <= 3 * 24 * 60 * 60 * 1000
              ? this.i18n.transform('latest_launch')
              : '';
          item.modelTags = [];
          if (item?.model_tags && item.model_tags.trim()) {
            item.modelTags = item?.model_tags.split(',');
          }
        });
        this.totalNumber = res.total;
        this.modelCount.set(res.total);

        // ngOninit只调用一次,从list接口判断是否存在未订阅模型，获取模型系列
        if (params.invoke === 'init') {
          this.isModelSubscribed = this.templateList.some(
            (itme) => itme.is_subscribed,
          );

          const all_model_type = res?.all_model_type || [];
          const seriesMap: any[] = all_model_type.map((itm) => ({
            name: this.modelTypeMap[itm],
            id: itm,
          }));
          this.appTabs = [...this.appTabs, ...seriesMap];
        }
      }).catch(() => {
        this.templateList = [];
      })
      .finally(() => {
        this.isLoading = false;
      });
  };

  toPlatform() {
    if (!this.subscribeBtnStatus || !this.isTenantAdmin) {
      return;
    }
    this.router.navigate([
      '/home/platform-management/authorization-management/model',
    ]);
  }

  handleAuthTip() {
    if (this.authTipInfo.btn === this.i18n.transform(AUTH_TIP_BTN.success)) {
      this.handleTip(false);
    }
    if (
      this.authTipInfo.btn === this.i18n.transform(AUTH_TIP_BTN.exceed_limit)
    ) {
      this.toPlatform();
    }
  }

  getSubscribeStatus(authResult) {
    this.authResult$.next(authResult);
    this.handleRefresh();
  }

  changeAppTab(data: AssertSquareTagType) {
    if (data.active) {
      if (data.id === 'all') {
        this.filterParams = {};
        this.activeTabId.set('');
      } else {
        this.filterParams.model_types = [data.id];
        this.activeTabId.set(data.id);
      }
      this.getData(this.filterParams);
    }
  }

  onTabIndexChange(index: number) {
    const tab = this.appTabs[index];
    if (tab.id === 'all') {
      this.filterParams = {};
      this.activeTabId.set('');
    } else {
      this.filterParams.model_types = [tab.id];
      this.activeTabId.set(tab.id);
    }
    this.getData(this.filterParams);
  }

  getAuthStatus() {
    this.modelManagementService
      .getModelPublisherInfo(this.provider_id, 'platform')
      .then((res) => {
        this.authStatus =
          res.auth_configs[0].auth_type !== 'IAM' &&
          res.auth_config_status === 'available';
      });
  }

  onAdvancdeSearchChange(data) {
    const subscribeStatus = {
      [this.openText]: true,
      [this.closeText]: false,
    };
    const params = { service_name: undefined, is_subscribed: undefined };
    params.service_name = this.searchValue;
    if (params.service_name) {
      this.filterParams.service_name = params.service_name;
    } else {
      delete this.filterParams.service_name;
    }

    if (params.is_subscribed) {
      this.filterParams.is_subscribed = [
        this.openText,
        this.closeText,
      ].includes(params.is_subscribed)
        ? subscribeStatus[params.is_subscribed]
        : params.is_subscribed;
    } else {
      delete this.filterParams.is_subscribed;
    }

    this.getData(this.filterParams);
  }

  private getModelCount() {
    if (this.searchValue === '' && this.modelCount() === 0) {
      return '';
    }
    return `(${this.modelCount()})`;
  }
}
