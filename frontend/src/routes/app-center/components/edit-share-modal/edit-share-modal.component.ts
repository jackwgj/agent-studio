import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import * as angularI18next from 'angular-i18next';
import { cloneDeep } from 'lodash';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzDrawerRef } from 'ng-zorro-antd/drawer';
import { SHARE_PAGE, SHARE_TOOL_TYPE } from './edit-share-modal.config';
import { SHARE_TYPE } from '@routes/app-center/app-center-management/app-center-management.config';
import { NgForm } from '@angular/forms';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';
import { ShareService } from './share.service';
import { MessageComponent } from '@shared/services/cfdata.service';
import { ApplicationType } from '@enums/agent-center.enum';
import { AppPluginRepoService } from '@services/agent-center/app-plugin-repo.service';
import { HttpService } from '@services/http.service';
import { CardService } from '@routes/agent-center/my-card/card.service';
@Component({
  selector: 'edit-share-modal',
  templateUrl: './edit-share-modal.component.html',
  styleUrls: ['./edit-share-modal.component.less'],
  standalone: true,
  imports: [
    MODULES,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class EditShareModalComponent implements OnInit {
  @Input() sharePage: SHARE_PAGE = SHARE_PAGE.apply;
  @Input() isEdit = false;
  @Input() shareInfo: any;
  @ViewChild('myForm') myForm: NgForm;
  @Output() changeShearVersion = new EventEmitter();

  title = '';
  isConfirmLoading = false;
  typeOptions = [
    {
      label: this.i18n.transform('workflow_market'),
      value: SHARE_TOOL_TYPE.workflow,
    },
    {
      label: this.i18n.transform('multiple_agent'),
      value: SHARE_TOOL_TYPE.multipleAgent,
    },
  ];
  typeSelect = '';
  applyOptions = [];
  applySelect = '';
  applyCount = 0;
  pluginOptions = [];
  pluginSelect = '';
  versionOptions = [];
  versionSelect = [];
  shareTypeOptions = [
    {
      id: SHARE_TYPE.CAN_COPY,
      name: this.i18n.transform('edit_share_modal_11'),
      label: this.i18n.transform('edit_share_modal_12'),
      disable: true,
      active: false,
    },
    {
      id: SHARE_TYPE.CAN_COPY,
      name: this.i18n.transform('edit_share_modal_13'),
      label: this.i18n.transform('edit_share_modal_14'),
      disable: false,
      active: true,
    }
  ];
  allWorkspaceRadio = [
    {
      id: 'all',
      label: this.i18n.transform('edit_share_modal_15'),
      value: 'all',
    },
    {
      id: 'half',
      label: this.i18n.transform('edit_share_modal_16'),
      value: 'half',
    },
  ];
  allWorkspaceSelect = '';
  shareTypeSelect = SHARE_TYPE.CAN_COPY;
  searchKey = '';
  allWorkspaceList = [];
  searchWorkspaceList = [];
  showWorkspaceList = []
  shareWorkspaceSelect = [];
  shareWorkspaceSelectIds = [];
  isAllSelected = false;
  currentPage = 1;
  totalNumber = 0;
  public pageSize: any = {
    options: [10],
    size: 10,
  };
  get isApplyPage(): boolean {
    return this.sharePage === SHARE_PAGE.apply;
  }

  get isWorkspaceSelectAll(): boolean {
    return this.allWorkspaceSelect === this.allWorkspaceRadio[0].value;
  }
  constructor(
    private readonly i18n: angularI18next.I18NextEagerPipe,
    private flowServ: AppFlowRepoService,
    private appAgentRepoServ: AppAgentRepoService,
    private shareService: ShareService,
    private appPluginRepoServ: AppPluginRepoService,
    private readonly http: HttpService,
    private cardService: CardService,
    private nzModalService: NzModalService,
    private drawerRef: NzDrawerRef,
    private cdr: ChangeDetectorRef,
  ) {

  }

  ngOnInit() {
    if (this.isEdit) {
      this.title = this.i18n.transform('edit_share_modal_17');
      this.typeSelect = this.shareInfo.resource_type;
      this.applySelect = this.shareInfo.resource_id;
      this.pluginSelect = this.shareInfo.resource_id;
      this.onSelectApply('');
    } else {
      const type = this.i18n.transform(this.sharePage === SHARE_PAGE.apply ? 'app' : 'plugin');
      this.title = this.i18n.transform('edit_share_modal_18', { type });
      this.allWorkspaceSelect = this.allWorkspaceRadio[1].value;
    }
    if (this.sharePage === SHARE_PAGE.plugin) {
      this.getPluginList();
    }
    this.queryWorkspaceList();
  }


  onSelectType($event: any): void {
    // 选了类型查列表
    if (this.typeSelect === SHARE_TOOL_TYPE.workflow) {
      this.getWorkflowList();
    } else if (this.typeSelect === SHARE_TOOL_TYPE.multipleAgent) {
      this.getMultipleAgent();
    }

  }

  onSelectApply($event: any): void {
    //  选了应用查版本
    if (this.typeSelect === SHARE_TOOL_TYPE.workflow) {
      this.getWorkflowVersion();
    } else if (this.typeSelect === SHARE_TOOL_TYPE.multipleAgent) {
      this.getMultipleAgentVersion();
    } else if (this.typeSelect === SHARE_TOOL_TYPE.plugin) {
      this.getPluginVersion();
    }
  }

  onSelectPlugin($event: any): void {
    //  选了应用查版本
    this.getPluginVersion();
  }

  changeShareType(item: any): void {
    if (item.disable) {
      return;
    }
    this.shareTypeSelect = item.id;
  }

  search(value: any): void {
    this.currentPage = 1;
    if (!value) {
      this.searchWorkspaceList = cloneDeep(this.allWorkspaceList);
      this.totalNumber
    } else {
      this.searchWorkspaceList = this.allWorkspaceList.filter(v => {
        return v.name.includes(value);
      });

    }
    this.totalNumber = this.searchWorkspaceList.length;

    if (this.shareWorkspaceSelect.length) {
      this.shareWorkspaceSelect = this.shareWorkspaceSelect.map(v => {
        const select = this.searchWorkspaceList.find(se => se.id === v.id);
        return select || v;
      });
    }
    this.getShowWorkspaceList();
  }

  shareWorkspaceChange(checked: any): void {
  }

  onAllCheckedChange(checked: boolean): void {
    if (checked) {
      this.shareWorkspaceSelect = [...this.searchWorkspaceList];
    } else {
      this.shareWorkspaceSelect = [];
    }
    this.updateShowWorkspaceChecked();
  }

  onItemCheckedChange(item: any, checked: boolean): void {
    if (checked) {
      if (!this.shareWorkspaceSelect.find(v => v.id === item.id)) {
        this.shareWorkspaceSelect.push(item);
      }
    } else {
      this.shareWorkspaceSelect = this.shareWorkspaceSelect.filter(v => v.id !== item.id);
    }
    this.isAllSelected = this.shareWorkspaceSelect.length === this.searchWorkspaceList.length;
  }

  private updateShowWorkspaceChecked(): void {
    this.showWorkspaceList.forEach(item => {
      item.checked = !!this.shareWorkspaceSelect.find(v => v.id === item.id);
    });
    this.isAllSelected = this.shareWorkspaceSelect.length === this.searchWorkspaceList.length && this.searchWorkspaceList.length > 0;
  }

  onPageUpdate(): void {
    this.getShowWorkspaceList();
  }

  queryWorkspaceList() {
    this.shareService.getWorkspace({
      scope: 'domain'
    })
      .then((res) => {
        const list = res.workspaceList || [];
        const allList = list.filter(v => {
          return v.id !== this.http.getWorkspaceId();
        });

        this.allWorkspaceList = allList || [];
        this.searchWorkspaceList = cloneDeep(this.allWorkspaceList);
        this.totalNumber = allList.length || 0;
        this.getShowWorkspaceList();
        this.initWorkSpace();
        this.cdr.detectChanges();
      });
  }

  getShowWorkspaceList() {
    this.showWorkspaceList = this.getSubArray(this.searchWorkspaceList, ((this.currentPage || 1) - 1) * this.pageSize.size, this.pageSize.size);
    this.updateShowWorkspaceChecked();
  }


  getSubArray(arr = [], offset: number, limit: number) {
    const start = Math.max(offset, 0);
    const end = Math.min(offset + limit, arr.length);
    return arr.slice(start, end);
  }

  dismiss(): void {
    this.drawerRef.close();
  }

  close(): void {
    this.drawerRef.close();
  }

  onComfirm(): void {
    let params: any = {}
    if (this.sharePage === SHARE_PAGE.plugin) {
      const plugin = this.pluginOptions.find(v => this.pluginSelect === v.id);
      params = {
        resource_id: this.pluginSelect,
        resource_type: 'plugin',
        resource_name: plugin.plugin_chinese_name,
        version_list: [...this.versionSelect],
        auth_workspace_id_list: this.isWorkspaceSelectAll ? ['all'] : this.shareWorkspaceSelect.map(v => {
          return v.id;
        }),
      }
    } else {
      const apply = this.applyOptions.find(v => this.applySelect === v.id);
      params = {
        resource_id: this.applySelect,
        resource_type: this.typeSelect,
        resource_name: apply?.name || this.shareInfo.resource_name,
        version_list: [...this.versionSelect],
        auth_workspace_id_list: this.isWorkspaceSelectAll ? ['all'] : this.shareWorkspaceSelect.map(v => {
          return v.id;
        }),
      }
    }
    if (this.isEdit) {
      this.getReferList(this.shareInfo, params)
    } else {
      this.shareService.postShare(params).then((res) => {
        MessageComponent.showSuccess(this.i18n.transform('edit_share_modal_19'), 3000);
        this.close();
      })
    }
  }

  private getReferList(data: any, params: any) {
    const param = {
      offset: 0,
      limit: 100,
      reference_type: 'share',
      resource_type: data.resource_type === SHARE_PAGE.plugin ? 'tool' : data.resource_type,
    };
    this.cardService
      .selectQuoteList(data.resource_id, param)
      .then((res) => {
        let count = res.count;
        if (count) {
          this.nzModalService.confirm({
            nzTitle: this.i18n.transform('edit_share_modal_20'),
            nzContent: this.i18n.transform('edit_share_modal_21'),
            nzOkText: this.i18n.transform('ok'),
            nzOnOk: () => {
              this.editDel(params);
            },
          });
        } else {
          this.editDel(params);
        }
      })
      .finally(() => {
      });
  }

  editDel(params: any) {
    this.shareService.postShare(params).then((res) => {
      MessageComponent.showSuccess(this.i18n.transform('edit_share_modal_22'), 3000);
      this.changeShearVersion.emit(params.version_list[0]);
      this.close();
    })
  }

  async getWorkflowList(searchWord = '', offset = 0) {
    const params = {
      offset,
      limit: 2000,
      name: searchWord,
      status: 'published'
    };
    const { workflow_list, count } = await this.flowServ.getFlows(params);

    workflow_list?.forEach((v: any) => {
      v.id = v.workflow_id;
      v.disabled = v.is_share === 1;
    });
    this.applyOptions = workflow_list || [];
    this.applyCount = count;
  }

  getWorkflowVersion() {
    this.appAgentRepoServ.getFlowVersion(this.applySelect).then(
      (res: any) => {
        const { version_list } = res || {};
        this.versionOptions = version_list;
        this.initVersion();
      },
      () => {
        this.versionOptions = [];
      },
    );
  }

  async getMultipleAgent(searchWord = '', offset = 0) {
    let param: any = {
      type: ApplicationType.MULTI_AGENT,
      status: 'published',
    };
    if (searchWord) {
      param.name = searchWord;
    }
    const { agent_list, count } = await this.appAgentRepoServ.getAgentList(
      param,
      offset,
      1000,
    );
    agent_list?.forEach((v: any) => {
      v.id = v.agent_id;
      v.disabled = v.is_share === 1;
    });
    this.applyOptions = agent_list;
  }

  getMultipleAgentVersion() {
    this.appAgentRepoServ.getAgentVersionList(this.applySelect).then(
      (res: any) => {
        const { version_list } = res || {};
        this.versionOptions = version_list;
        this.initVersion();
      },
      () => {
        this.versionOptions = [];
      },
    );
  }

  async getPluginList(searchWord = '', offset = 0) {
    let params: any = {
    };
    if (searchWord) {
      params.cn_name = searchWord;
    }
    const { plugin_list, count } = await this.appPluginRepoServ.getPluginList({
      offset,
      limit: 1000,
      published: 1,
      ...params,
    });

    plugin_list?.forEach((v: any) => {
      v.id = v.plugin_id;
      v.disabled = v.is_share === 1;
    })
    this.pluginOptions = plugin_list || [];
    this.applyCount = count;
  }

  getPluginVersion() {
    this.appAgentRepoServ
      .getPluginVersionList(this.pluginSelect)
      .then((res) => {
        this.versionOptions = res.version_list || [];
        this.initVersion();
      })
      .finally(() => {});
  }

  private initVersion() {
    if (this.shareInfo && this.isEdit) {
      this.versionSelect = this.shareInfo.version_info_list.map(item => {
        return item.version_id;
      })
    }
  }

  private initWorkSpace() {
    if (this.shareInfo && this.isEdit) {
      if (!this.shareInfo.workspace_list.length) {
        this.allWorkspaceSelect = this.allWorkspaceRadio[0].value;
      } else {
        this.allWorkspaceSelect = this.allWorkspaceRadio[1].value;
        this.shareWorkspaceSelect = this.shareInfo.workspace_list.map(item => {
          let idx = this.searchWorkspaceList.find(elm => {
            return item.workspace_id === elm.id;
          });
          return idx;
        }).filter(v => v?.id);
        this.isAllSelected = this.shareWorkspaceSelect.length === this.searchWorkspaceList.length;
      }

    }
  }
}
