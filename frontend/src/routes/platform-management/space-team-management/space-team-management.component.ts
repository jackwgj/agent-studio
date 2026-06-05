import {ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MODULES} from '@shared/modules';
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from 'angular-i18next';
import {I18nNamespace} from '@i18n';
import {agentCommonLogic} from '@routes/agent-center/app-agent/common-logic-agent';
import {NzModalModule, NzModalService} from 'ng-zorro-antd/modal';

import {CreateSpaceComponent} from './create-space/create-space.component';
import {DeleteSpaceComponent} from '@routes/platform-management/space-team-management/delete-space/delete-space.component';
import {AddUserComponent} from '@routes/platform-management/space-team-management/add-user/add-user.component';
import {TransferSpaceComponent} from '@routes/platform-management/space-team-management/transfer-space/transfer-space.component';
import {SpaceTeamManagementService} from '@services/space-team-management.service';
import {ContextService} from '@services/context.service';
import {cdnAssetUrl} from 'src/single-spa/assets-url';
import {DEFAULT_ICON} from 'src/assets/images/common/common_image_base64';
import {SharedModule} from '@shared/shared.module';
import {CommonService} from '@services/common.service';
import {HttpService} from "@services/http.service";
import {PipesModule} from "../../../pipes/pipes.module";
import {CommonUtils} from "../../../utils/common.util";
import {NzMessageService} from "ng-zorro-antd/message";
import {getSessionStorage} from "../../../utils/utils";
import { StorageService } from '@shared/services/cfdata.service';

@Component({
  selector: 'space-team-management',
  templateUrl: './space-team-management.component.html',
  styleUrls: ['./space-team-management.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    SharedModule,
    PipesModule,
    NzModalModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PLATFORM_MANAGEMENT, I18nNamespace.COMMON],
    },
    agentCommonLogic,
    NzModalService,
  ],
})
export class SpaceTeamManagementComponent implements OnInit, OnDestroy {
  subscribeBtnStatus = this.commonService.getSubscribeStatus();
  tabs: any = [
    {
      title: this.i18n.transform('member_crew'),
      active: true,
    },
  ];
  loading = false;
  descandbuyConfig = {
    helptip: {
      label: this.i18n.transform('space_team_management'),
    },
    buttons: [],
  };
  integrationTabsOption: Array<any> = [
    {
      roleName: this.i18n.transform('all_characters'),
      roleId: '',
    },
  ];
  activeName = '';
  userId = '';
  info_detail = {
    id: '',
    icon: DEFAULT_ICON,
    name: '',
    description: '',
    role: '',
    role_name_cn: '',
    createdOn: '',
  };

  displayed = []; // 表示表格实际呈现的数据（开发者默认设置为[]即可）
  checkedList: Array<any> = []; // 默认选中项
  currentPage: number = 1;
  totalNumber: number = 0;
  show: boolean = false;
  pageSize = {
    options: [10, 20, 50, 100],
    size: 10,
  };

  public space_team_data;

  public columns = [
    {
      title: '',
    },
    {
      title: this.i18n.transform('nick_name'),
    },
    {
      title: this.i18n.transform('character'),
    },
    {
      title: this.i18n.transform('join_time'),
    },
    {
      title: this.i18n.transform('operation'),
    },
  ];
  public searchItems = [
    {
      label: this.i18n.transform('space_name'),
      field: 'space_name',
      valueKey: 'value',
      options: [
        {
          label: this.i18n.transform('large_language_model'),
          value: 'nlp',
        },
        {
          label: this.i18n.transform('vector_type'),
          value: 'embedding',
        },
        {
          label: this.i18n.transform('ranking_model'),
          value: 'rerank',
        },
        {
          label: this.i18n.transform('character_recognition'),
          value: 'ocr',
        },
      ],
    },
  ];

  public searchName = '';
  roles_option = [];

  public isSite: boolean = false;
  lang='zh-cn';

  get selectNum() {
    if (this.checkedList?.length === 0) {
      return this.totalNumber;
    }
    return `${this.checkedList.length} / ${this.totalNumber}`;
  }

  constructor(
    private i18n: I18NextEagerPipe,
    private spaceTeamManagementService: SpaceTeamManagementService,
    private ctxServ: ContextService,
    private cdr: ChangeDetectorRef,
    public commonService: CommonService,
    private readonly http: HttpService,
    private nzModal: NzModalService,
    private message: NzMessageService
  ) {
    this.space_team_data = {
      // 表格源数据，开发者对表格的数据设置请在这里进行
      data: [], // 源数据
      state: {
        searched: false, // 源数据未进行搜索处理
        sorted: false, // 源数据未进行排序处理
        paginated: true, // 源数据已进行分页处理
      },
    };
    this.userId = this.ctxServ?.user?.userId;
  }
  async ngOnInit() {
    this.lang = CommonUtils.getLanguage();
    this.get_space_member_roles();

    this.isSite = this.commonService.isSite();
    //获得空间详情
    await this.get_space_detail();

    await this.get_space_members();

    const cur_space_option_str = StorageService.getSessionStorage('CUR_SPACE_OPTIONS');
    if (cur_space_option_str !== '{}') {
      const cur_space_option_obj = JSON.parse(cur_space_option_str);
      this.descandbuyConfig.buttons = this.getButtons(cur_space_option_obj);
    }

    // 强制变更检测
    this.cdr.detectChanges();
  }

  getButtons(cur_space_option_obj) {
    return cur_space_option_obj.type === 'PERSON'
      ? []
      : [
        {
          label: this.i18n.transform('del_space'),
          color: 'default',
          click: () => {
            this.deleteSpace();
          },
          disabled:
            this.info_detail?.role !== 'OWNER' ||
            !this.subscribeBtnStatus,
        },
        {
          label: this.i18n.transform('exit_space'),
          color: 'default',
          disabled:
            this.info_detail?.role === 'OWNER' ||
            !this.subscribeBtnStatus,
          click: () => {
            this.exitSpace();
          },
        },
        {
          label: this.i18n.transform('transfer_space'),
          color: 'default',
          click: () => {
            this.transferSpace();
          },
          disabled:
            this.info_detail?.role !== 'OWNER' ||
            !this.subscribeBtnStatus,
        },
      ];
  }

  ngOnDestroy(): void {}

  onActiveChange(isActive: boolean, item: any): void {
    if (item.active) {
      this.activeName = item.id;
    }
  }
  getIcon(url: string) {
    if (url.startsWith('data:image/')) {
      return url;
    } else {
      return cdnAssetUrl(url);
    }
  }

  public trackByFn(index: number, item: any): number {
    return item.id;
  }

  public onSearchContentChange(info): void {
    this.currentPage = 1;
    this.totalNumber = 0;
    this.get_space_members();
  }

  onClearFn(event: MouseEvent) {
    this.get_space_members();
  }

  public onSelect(event, item) {
    event.click();
  }

  transferSpace() {
    const modal = this.nzModal.create({
      nzTitle: this.i18n.transform('transfer_space_1'),
      nzFooter: null,
      nzContent: TransferSpaceComponent,
      nzClassName: 'transfer-space-modal',
    });
    modal.afterClose.subscribe(() => {
      this.get_space_detail();
    });
  }

  exitSpace() {
    const that = this;
    this.nzModal.confirm({
      nzTitle: this.i18n.transform('exit_tip', {
        space_info: this.info_detail.name,
      }),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOkDanger: true,
      nzOnOk(): void {
        that.spaceTeamManagementService.deleteSpace().then((res) => {
          that.message.create('success', that.i18n.transform('exit_success'));
          that.spaceTeamManagementService.updateSpace('UPDATE');
        });
      },
    });
  }

  deleteThisUser(item) {
    const that = this;
    this.nzModal.confirm({
      nzTitle: this.i18n.transform('del_member_tip', {
        memberName: item.memberName,
      }),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOkDanger: true,
      nzOnOk(): void {
        that.del_members([item.memberId]);
      },
    });
  }

  deleteAllUsers() {
    const that = this;
    if (this.checkedList.length === 0) {
      this.nzModal.confirm({
        nzTitle: this.i18n.transform('prompt'),
        nzContent: this.i18n.transform('del_no_member_tip'),
        nzOkText: this.i18n.transform('ok'),
        nzCancelText: this.i18n.transform('cancel'),
      });
      return;
    }
    this.nzModal.confirm({
      nzTitle: this.i18n.transform('delete'),
      nzContent: this.i18n.transform('del_all_member_tip'),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOkDanger: true,
      nzOnOk(): void {
        that.del_members(that.checkedList);
      },
    });
  }

  deleteSpace() {
    const modal = this.nzModal.create({
      nzTitle: this.i18n.transform('del_someone_space', {
        space_name: this.info_detail.name,
      }),
      nzFooter: null,
      nzContent: DeleteSpaceComponent,
      nzClassName: 'del_space_model',
    });
    const instance = modal.getContentComponent() as any;
    instance.id = this.info_detail.id;
    instance.title = this.i18n.transform('del_someone_space', {
      space_name: this.info_detail.name,
    });
    instance.fnName = 'deleteSapceFn';
  }

  addUser(user_info) {
    const modal = this.nzModal.create({
      nzTitle: this.i18n.transform('add_user'),
      nzFooter: null,
      nzContent: AddUserComponent,
      nzClassName: 'add-user-modal',
    });
    const instance = modal.getContentComponent() as any;
    instance.space_id = user_info.id;
    modal.afterClose.subscribe(() => {
      this.get_space_members();
    });
  }

  edit_fn() {
    const thisNzModal: any = this.nzModal.create({
      nzTitle: this.i18n.transform('edit_space'),
      nzFooter: null,
      nzContent: CreateSpaceComponent,
      nzClassName: 'edit-space-modal',
    });
    const instance = thisNzModal.getContentComponent();
    instance.title = this.i18n.transform('edit_space');
    instance.detail = this.info_detail;
    instance.source = 'edit';
    instance.afterClose.subscribe(() => {
      this.get_space_detail();
    });
  }

  async get_space_detail() {
    await this.spaceTeamManagementService
      .getWorkspaceDetial(this.ctxServ.currentWorkspaceId)
      .then((res) => {
        this.info_detail = res;
        this.info_detail.role_name_cn = this.role_name(res.role);
      });
  }

  get_space_members() {
    this.loading = true;
    const params = {
      page_num: this.currentPage,
      page_size: this.pageSize.size,
      role: this.activeName,
      member_name: this.searchName,
    };
    this.spaceTeamManagementService
      .getSpaceMembers(params)
      .then((res: any) => {
        this.space_team_data.data = res?.workspaceList ?? [];
        this.displayed = this.space_team_data.data;
        this.totalNumber = this.searchName
          ? res?.workspaceList.length
          : res.count;
      })
      .finally(() => {
        this.loading = false;
      });
  }

  get_space_member_roles() {
    const role_list = getSessionStorage('ROLES') ? getSessionStorage('ROLES') : [];
    this.roles_option = role_list?.map((item) => {
      return {
        roleId: item.roleId,
        roleName:this.lang==='zh-cn'?item.roleNameCn:CommonUtils.titleCase3(item.roleNameEn),
        roleNameCn: item.roleNameCn,
        roleNameEn: item.roleNameEn,
        disabled: item.roleId === 'OWNER',
      };
    });
    const inter_roles_option = role_list.map((item) => {
      return {
        roleId: item.roleId,
        roleName:this.lang==='zh-cn'?item.roleNameCn:CommonUtils.titleCase3(item.roleNameEn),
        roleNameCn: item.roleNameCn,
        roleNameEn: item.roleNameEn,
      };
    });

    this.integrationTabsOption = [
      ...this.integrationTabsOption,
      ...inter_roles_option,
    ];
  }

  role_name(id: string): string {
    const tiLocaleKey= localStorage.getItem('tiLocaleKey')
    const role = this.roles_option.filter((item) => item.roleId === id);
    let role_name = '';

    if (role.length) {
      if (this.lang==='zh-cn') {
        role_name = role[0].roleNameCn;
      } else {
        role_name = role[0].roleNameEn;
      }
    } else {
      role_name = '';
    }
    return CommonUtils.titleCase3(role_name);
  }

  del_members(ids: any[]) {
    const params = {
      user_ids: ids,
    };
    this.spaceTeamManagementService
      .deleteSpaceMembers(params)
      .then((res) => {
        this.message.create('success', this.i18n.transform('delete_success'));
        this.checkedList = [];
        this.get_space_members();
      })
      .finally(() => {});
  }

  integrationSelect(info) {
    this.get_space_members();
  }

  onCurrentPageChange(): void {
    this.get_space_members();
  }

  onPageNumChange(event): void {
    this.currentPage = event.currentPage;
    this.pageSize.size = event.size;
    this.get_space_members();
  }

  onRoleSelect(event, item) {
    const params = {
      members: [
        {
          member_id: item.memberId,
          role: event,
        },
      ],
    };
    this.spaceTeamManagementService
      .editSpaceMembers(params)
      .then((res) => {
        this.message.create('success', this.i18n.transform('edit_success'));
      })
      .finally(() => {});
  }

  async handleClickRefresh() {
    await this.get_space_members();
  }

  protected readonly JSON = JSON;
  protected readonly changeUrl = cdnAssetUrl;
  protected readonly cdnUrl = cdnAssetUrl;
}
