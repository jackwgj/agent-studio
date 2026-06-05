import { ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MessageComponent } from '@shared/services/cfdata.service';
import { ApplicationType } from '@enums/agent-center.enum';
import { I18nNamespace } from '@i18n';
import * as mcpInterfaces from '@interfaces/mcp/mcp.interfaces';
import { McpInterfaces } from '@interfaces/mcp/mcp.interfaces';
import { MonacoEditorModule } from '@materia-ui/ngx-monaco-editor';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { McpAllService } from '@routes/service-market/mcp-all.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { MCPService } from '@services/agent-center/mcp.service';
import { CommonService } from '@services/common.service';
import { MCP_DEFAULT_ICON_BASE64_STR } from '@shared/config/default-icons-base64';
import { MODULES } from '@shared/modules';
import * as angularI18next from 'angular-i18next';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { utcToBeijingTime } from '../../../../../utils/commonFn';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzSpaceModule } from 'ng-zorro-antd/space';
import { DebounceDecorators } from '@shared/decorators/debouncing-throttling.directive';
enum mapKeys {
  service_id = 'service_id',
}

@Component({
  selector: 'add-mcp-third-party-modal-component',
  templateUrl: './add-mcp-third-party-modal.html',
  styleUrls: ['./add-mcp-third-party-modal.scss'],
  standalone: true,
  imports: [
    MODULES,
    MonacoEditorModule,
    NzTableModule,
    NzTabsModule,
    NzAlertModule,
    NzInputModule,
    NzButtonModule,
    NzCheckboxModule,
    NzPaginationModule,
    NzEmptyModule,
    NzIconModule,
    NzSpaceModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.COMMON],
    },
  ],
})
export class AddMcpThirdPartyModal implements OnInit {
  @Input('type') type: string = 'server';
  @Input('action') action: string = 'create';
  @Input('selectMcpData') selectMcpData: mcpInterfaces.McpInterfaces = null;
  @Input('title') title = '';
  @Input() exportType = ApplicationType.SINGLE_AGENT;

  @ViewChild('previewIcon') previewIcon: ElementRef;
  @ViewChild('iconInput') iconInput: ElementRef<HTMLInputElement> | null = null;
  @Output('mcpModalSelectedData') mcpModalSelectedData = new EventEmitter<McpInterfaces>();

  readonly modalRef = inject(NzModalRef);

  resource = 'kooGallery';
  displayedData: any[] = [];
  srcData: any = {
    data: [],
    state: {
      searched: true,
      sorted: true,
      paginated: true,
    },
  };
  currentPage: number = 1;
  totalNumber: number = 0;
  pageSize: { options: Array<number>; size: number } = {
    options: [10, 20, 50, 100],
    size: 10,
  };
  checkedList: any[] = [];

  columns: any[] = [
    {
      title: '',
    },
    {
      title: this.i18n.transform('service_name'),
    },
    {
      title: this.i18n.transform('description'),
    },
    {
      title: this.i18n.transform('create_date'),
    },
    {
      title: this.i18n.transform('update_date'),
    },
  ];

  loading = false;
  btn_loading = false;
  noDataTitle: string = this.i18n.transform('no_data');
  noDataDesc = [];

  tabs: any = [
    {
      title: this.i18n.transform('jiuwen_cloud_store'),
      active: true,
      resource: 'kooGallery',
    },
    {
      title: 'ROMA Connect',
      active: false,
      resource: 'ROMA',
    },
  ];

  public searchMcpModalName: string = '';
  public selectedRomaRows = [];
  public selectedCloudRows = [];
  public isCreating = true;

  public searchItems: any[] = [
    {
      label: this.i18n.transform('service_name'),
      field: 'name',
      options: [],
    },
  ];
  public searchName = '';

  kooNoDataTitle: string = this.i18n.transform('third_no_data', { title: this.i18n.transform('jiuwen_cloud_store') });
  kooNoDataDesc: any[] = [
    {
      id: 'item-desc1',
      type: 'html',
      html: this.i18n.transform('install_third_tip1'),
    },
    {
      id: 'item-desc2',
      type: 'link',
      link: {
        href: this.goToMCPSquare('kooGallery', true),
        target: '_self',
        text: this.i18n.transform('go_now'),
      },
    },
  ];
  romaNoDataTitle: string = this.i18n.transform('third_no_data', { title: 'Roma' });
  romaNoDataDesc: any[] = [
    {
      id: 'item-desc1',
      type: 'html',
      html: this.i18n.transform('install_third_tip1'),
    },
    {
      id: 'item-desc2',
      type: 'link',
      link: {
        href: this.goToMCPSquare('ROMA', true),
        target: '_self',
        text: this.i18n.transform('go_now'),
      },
    },
  ];

  tabSelectIndex = 0;

  constructor(
    private readonly mcpService: MCPService,
    private readonly i18n: angularI18next.I18NextEagerPipe,
    public commonService: CommonService,
    private cdr: ChangeDetectorRef,
    private configServ: AgentConfigService,
    private service: AppAgentRepoService,
    private mcpAllService: McpAllService,
    private router: Router
  ) {}

  public async tabChange(index: number) {
    this.tabs.forEach((item, i) => {
      item.active = i === index;
    });
    const tab = this.tabs[index];
    if (tab.active) {
      this.checkedList = [];
      this.resource = tab.resource;
      this.selectedRomaRows = [];
      this.isCreating = true;
      this.searchName = '';
      this.currentPage = 1;
      await this.new_getMcpList();
    }
  }

  public goToMCPSquare(type: any, is_link = true): any {
    const link = `${location.protocol}//${location.host}${location.pathname}#/home/service-market?thirdValue=${type}`;
    if (is_link) {
      return link;
    } else {
      this.router.navigate(['/home/service-market'], {
        queryParams: {
          thirdValue: type,
        },
      });
      return '';
    }
  }

  public async onSearchContentChange() {
    this.currentPage = 1;
    await this.new_getMcpList();
  }
  async getReferenceList() {
    await this.new_getMcpList();
  }

  getIcon(url: string): string {
    if (url) {
      if (url.startsWith('data:image/')) {
        return url;
      } else {
        return cdnAssetUrl(url);
      }
    }
    return '';
  }
  async ngOnInit() {
    await this.new_getMcpList();
  }

  trackByFn(index: number, item: any): number {
    return item.id;
  }

  public async refreshData() {
    this.currentPage = 1;
    this.searchName = '';
    await this.new_getMcpList();
  }

  @DebounceDecorators()
  async new_getMcpList() {
    this.loading = true;
    this.srcData.data = [];
    const new_params = {
      offset: (this.currentPage - 1) * this.pageSize.size,
      limit: this.pageSize.size,
    };
    const query: any = {
      is_subscription: 1,
      resource: this.resource,
    };
    if (this.searchName.length > 0) {
      query.name = '';
    }

    await this.mcpAllService
      .getMcpSquareThird(new_params, query)
      .then(res => {
        this.srcData.data = res.mcps;
        this.totalNumber = res.total;
        this.cdr.detectChanges();
        this.loading = false;
      })
      .catch(() => {
        this.loading = false;
      });
  }

  toDetail(item) {
    const queryParams = {
      id: item.id || item[mapKeys.service_id],
      type: 'third',
      resource: this.resource,
      ...(item?.version_id && { version_id: item.version_id }),
    };
    this.router.navigate(['/home/agent-center/mcp-service/detail'], {
      queryParams: queryParams,
    });
  }

  public onCloudRowSelectionOutput(selected) {
    this.selectedCloudRows = selected;
    this.isCreating = selected.length <= 0;
    this.cdr.detectChanges();
  }

  allChecked = false;

  checkAll(checked: boolean): void {
    if (checked) {
      this.checkedList = this.srcData.data.filter((row: any) => {
        return !row.disabled || this.checkedList.includes(row);
      });
    } else {
      this.checkedList = [];
    }
  }

  checkItem(row: any, checked: boolean): void {
    if (checked) {
      if (!this.checkedList.includes(row)) {
        this.checkedList.push(row);
      }
    } else {
      this.checkedList = this.checkedList.filter((item: any) => item !== row);
    }
  }

  public onRomaRowSelectionOutput(selected) {
    this.selectedRomaRows = selected;
    this.isCreating = selected.length <= 0;
    this.cdr.detectChanges();
  }
  public handleClickMcpModalConfirm() {
    this.btn_loading = true;
    const query = {
      resource: this.resource,
    };
    const selectedRows = this.checkedList;
    const new_servers = this.mcpAllService.installThirdCommonParams(selectedRows);
    const params = {
      servers: new_servers,
    };
    this.mcpAllService
      .thirdDeployBatch(query, params)
      .then(res => {
        this.btn_loading = false;
        for (let i = 0; i < res.results.length; i++) {
          if (res.results[i].status === 'FAILED') {
            MessageComponent.showError(`${this.i18n.transform('service_name')}:${res.results[i].name}，${res.results[i].errorMessage}`, 3000);
          }
          if (res.results[i].status === 'SUCCESS') {
            MessageComponent.showSuccess(this.i18n.transform('MCPSubmitSuccess'), 3000);
            this.close();
          }
        }
      })
      .catch(() => {
        this.btn_loading = false;
      });
  }
  public dismiss() {
    this.modalRef.destroy();
  }
  public close() {
    this.modalRef.destroy();
  }

  protected readonly utcToBeijingTime = utcToBeijingTime;
  protected readonly changeUrl = cdnAssetUrl;
  protected readonly MCP_DEFAULT_ICON_BASE64_STR = MCP_DEFAULT_ICON_BASE64_STR;
}
