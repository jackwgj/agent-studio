import { Clipboard } from '@angular/cdk/clipboard';
import { Component, EventEmitter, HostBinding, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { Subscription } from 'rxjs';
import { MessageComponent } from '@shared/services/cfdata.service';
import { SIESkillApi } from '@agentcore/api/sie-skill.api';
import { CARD_LIST_TYPE } from '@agentcore/constants/skill';
import { I18nService } from '@agentcore/core/i18n.service';
import { SkillCommonService } from '@agentcore/library/skill/services/skill.common.service';
import { I18nPipe } from '@agentcore/shared/pipes/i18n.pipe';
import { I18nNamespace } from '@i18n';
import { McpInterfaces } from '@interfaces/mcp/mcp.interfaces';
import { AddMcpHalfModalComponent } from '@routes/agent-center/app-mcp-service/components/add-mcp-half-modal/add-mcp-half-modal.component';
import { McpDeployFailType } from '@routes/agent-center/types/common.types';
import { AppPluginRepoService } from '@services/agent-center/app-plugin-repo.service';
import { MCPService } from '@services/agent-center/mcp.service';
import { CommonService } from '@services/common.service';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { WindowResizeService } from '@shared/base/window-resize.service';
import { MCP_DEFAULT_ICON_BASE64_STR } from '@shared/config/default-icons-base64';
import { MODULES } from '@shared/modules';
import { DeleteRefsService } from '@shared/services/delete-refs.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { PipesModule } from '../../../../pipes/pipes.module';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { CommonUtils } from '../../../../utils/common.util';
import { copyToClipboard } from '../../../../utils/commonFn';

import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpaceModule } from 'ng-zorro-antd/space';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

enum mapKeys {
  FROM = 'from',
  WORK_SPACE_ID = 'work_space_id',
  DEVELOP_SPACE_MCP = 'developSpaceMCP',
  DEVELOP_SPACE_PLUGIN = 'developSpacePlugin',
  IS_HOVER_UPLOAD_BTN = 'isHoverUploadBtn',
}

@Component({
  selector: 'component-card',
  templateUrl: './component-card.component.html',
  styleUrls: ['./component-card.component.less'],
  standalone: true,
  imports: [MODULES, PipesModule, I18nPipe, NzModalModule, NzButtonModule, NzIconModule, NzDropDownModule, NzTagModule, NzSpaceModule, NzToolTipModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT],
    },
    DeleteRefsService,
  ],
})
export class ComponentCardComponent implements OnInit {
  @Input() type = 0;
  @Input() data: any;
  // 是否来自agentBuilder
  @Input() isFromAgentBuilder = false;
  @Input() workSpaceId = '';
  // 是否agentBuilder携带work_space_id
  // 是否来自于首页
  @Input() isFromOverview = false;
  // 是否来自于工作空间
  @Input() isFromDevelopSpace = false;
  @Output() handleAction = new EventEmitter();
  // 刷新mcp的列表数据
  @Output() refreshMcpListEmit = new EventEmitter();

  @HostBinding('style.width') width = '';
  // 展示MCP异常信息的弹框
  mcpFailTemplateVisible = false;
  subscribeBtnStatus = this.commonService.getSubscribeStatus();
  public cardActions = [];
  public changeUrl = cdnAssetUrl;
  public lang: string;
  public isOpAccount = false;
  public cardListType = CARD_LIST_TYPE;
  // 监听浏览器宽度，做自适应弹框宽度
  public windowWidth: number;

  mcpDeployFail: McpDeployFailType = {
    code: '',
    debug_info: '',
    message_en: '',
    message_cn: '',
    reason_en: '',
    reason_cn: '',
    suggestion_cn: '',
    suggestion_en: '',
  };

  protected readonly MCP_DEFAULT_ICON_BASE64_STR = MCP_DEFAULT_ICON_BASE64_STR;
  private resizeSub: Subscription;

  public get isMcp() {
    return this.type === 0;
  }

  public get name() {
    if (this.isZH()) {
      return this.data.name || this.data.plugin_chinese_name || this.data.skill_name;
    }
    return this.data.name_en || this.data.plugin_display_name || this.data.name || this.data.plugin_chinese_name || this.data.skill_name;
  }

  public get status() {
    if (!this.isMcp) {
      return this.data?.last_version_id ? 'published' : 'unpublished';
    }
    return this.getStatus();
  }

  public get statusColor() {
    return this.getColor();
  }

  public get testStatus() {
    if (!this.isMcp) {
      const status = this.data?.test_status;
      if (status === 'SUCCESS') {
        return this.i18n.transform('debug_success');
      } else if (status === 'FAILED') {
        return this.i18n.transform('debug_failed');
      }
    }
    return undefined;
  }

  public get testStatusColor(): string | undefined {
    const status = this.data?.test_status;
    if (status === 'SUCCESS') {
      return '#1476FF';
    } else if (status === 'FAILED') {
      return 'red';
    }
    return undefined;
  }

  // 设置抽屉的宽度
  private get halfModalWidth() {
    if (this.windowWidth > 1920) {
      return '800px';
    } else {
      return '600px';
    }
  }

  constructor(
    private clipboard: Clipboard,
    private router: Router,
    private i18n: I18NextEagerPipe,
    private _skillApi: SIESkillApi,
    private i18nCore: I18nService,
    private skillCommonService: SkillCommonService,
    private appPluginRepoServ: AppPluginRepoService,
    private mcpService: MCPService,
    private readonly api: MCPService,
    private commonService: CommonService,
    private ctxServ: ContextService,
    private readonly http: HttpService,
    private windowResizeService: WindowResizeService,
    private deleteRefsServe: DeleteRefsService,
    private nzModal: NzModalService
  ) {
    this.lang = CommonUtils.getLanguage();
    this.isOpAccount = this.ctxServ.isOpAccount;
  }

  ngOnInit(): void {
    this.resizeSub = this.windowResizeService.getWindowWidth().subscribe(width => {
      this.windowWidth = width;
    });
    this.cardActions = [
      {
        id: 'delete',
        label: this.isMcp ? this.i18n.transform('unload') : this.i18n.transform('delete'),
        disabled: !this.subscribeBtnStatus || (this.isMcp && this.data.fc_instance_status === 'creatingStack'),
        tip: this.isMcp && this.data.fc_instance_status === 'creatingStack' ? this.i18n.transform('componentcardcomponent_1173') : '',
      },
    ];
    if (!this.isMcp) {
      if (this.data.status === 'developed') {
        this.cardActions.unshift({
          id: 'export',
          label: this.i18nCore.transform('skill.list.button.export'),
          disabled: !this.subscribeBtnStatus,
        });
      }
      if (this.type !== CARD_LIST_TYPE.SKILL) {
        this.cardActions.unshift({
          id: 'copyID',
          label: this.i18n.transform('replication_id'),
          disabled: !this.subscribeBtnStatus,
        });
      }
    }
    if (this.isMcp) {
      this.addCardActionWithMcpFail();
      this.setMcpDeployFail(this.data.fail_reason_detail || {});
      this.setStatus();
      this.setType();
    }
  }

  ngOnDestroy(): void {
    this.resizeSub.unsubscribe();
  }

  public getMcpTagName(resource) {
    const resource_list = {
      ROMA: 'ROMA',
      custom: this.i18n.transform('custom'),
      kooGallery: this.i18n.transform('jiuwen_cloud_store'),
      inner: this.i18n.transform('platform_selection'),
    };
    return resource_list[resource] ?? this.i18n.transform('custom');
  }

  deleteVersionModal(data) {
    // 两种id用不同字段名，同时只存在一个
    const id = data.id || data.plugin_id;
    const { plugin_chinese_name, name } = data;
    const resource_type = this.isMcp ? 'mcp' : 'tool';
    this.deleteRefsServe.onDeleteRefs(
      {
        id,
        query: {
          resource_type,
        },
      },
      {
        title: this.isMcp ? this.i18n.transform('del_mcp_service') : this.i18n.transform('del_workflow_version'),
        alertText: this.isMcp ? this.i18n.transform('del_mcp_version_tips') : this.i18n.transform('del_plugin_version_tips'),
        secondConfirmLabel: `${this.i18n.transform('del_sure_tips')} <strong>DELETE</strong>`,
        tiMsgTitle: this.i18n.transform('messageInfo'),
        tiMsgContent: this.isMcp ? this.i18n.transform('del_mcp_version_content') : this.i18n.transform('del_plugin_version_content'),
      },
      () => {
        if (!this.isMcp) {
          this.deletePlugin(id);
        } else {
          this.deleteMcp();
        }
      }
    );
  }

  deletePlugin(plugin_id: string) {
    this.appPluginRepoServ
      .deletePlugin(plugin_id)
      .then(() => {
        MessageComponent.showSuccess(this.i18n.transform('deleted_successfully'));
        this.handleAction.emit({
          action: 'delete',
        });
      })
      .catch(() => {});
  }

  public onClickAction(action: any, data: any) {
    const { id } = action;
    switch (id) {
      case 'copyID':
        this.copyID();
        break;
      case 'edit_action':
        this.edit_action(id, data);
        break;
      case 'delete':
        this.toDel();
        break;
      case 'customizeNode':
        this.handleAction.emit({
          action: id,
          data,
        });
        break;
      case 'reason':
        this.showMcpFailBox();
        break;
      case 'edit':
        this.handleEditMcpServer(data.id);
        break;
      case 'redeploy':
        this.redeployMcp();
        break;
      case 'remodel':
        this.remodel(data);
        break;
      case 'export':
        this.exportSkill(data);
        break;
      default:
        break;
    }
  }

  // 点击弹框的重新部署
  handleClickRedeployMcpModal() {
    this.mcpFailTemplateVisible = false;
    this.redeployMcp();
  }

  // 改造
  remodel(data) {}

  // 重新部署
  public redeployMcp() {
    this.api
      .getMcpTools(this.data.id)
      .then(() => {
        MessageComponent.showSuccess(this.i18n.transform('MCPSubmitSuccessContent'), 6000);
        this.refreshMcpListEmit.emit(true);
      })
      .catch(() => {})
      .finally(() => {});
  }

  // 点击弹框的编辑mcp
  handleClickEditMcpModal() {
    this.mcpFailTemplateVisible = false;
    this.handleEditMcpServer(this.data.id);
  }

  dismiss(): void {}

  close(): void {}

  // 当点击MCP卡片的状态为"部署失败"时，支持展示错误的弹框
  public handleClickStatusTag(event: Event) {
    if (this.data?.fc_instance_status === 'stackFail') {
      this.onClick(event);
      this.showMcpFailBox();
    }
  }

  // 关闭MCP的错误弹框
  public closeMcpFailBox(): void {
    this.mcpFailTemplateVisible = false;
  }

  public edit_action(id: any, data: any) {
    const prefixUrl = window.location.href?.split('/home')[0];
    this.router.navigate(['/home/agent-center/custom-plugin/action'], {
      queryParams: {
        id: data?.tool_id,
      },
    });
  }

  public handleDetailView() {
    if (!this.subscribeBtnStatus) {
      return;
    }
    if (this.isMcp) {
      if (this.data.fc_instance_status === 'creatingStack') {
        MessageComponent.showWarn(this.i18n.transform('componentcardcomponent_1169'));
        return;
      }
    }
    const { id, resource } = this.data;
    const mcpQueryParams = {
      id,
      type: 'service',
      resource,
    };
    const pluginQueryParams = {
      id: this.data?.plugin_id,
    };
    if (this.isFromDevelopSpace) {
      mcpQueryParams[mapKeys.FROM] = 'develop-space';
      pluginQueryParams[mapKeys.FROM] = 'develop-space';
    }
    if (this.isFromOverview) {
      mcpQueryParams[mapKeys.FROM] = 'overview';
      pluginQueryParams[mapKeys.FROM] = 'overview';
    }
    if (this.isFromAgentBuilder) {
      mcpQueryParams[mapKeys.FROM] = 'agentBuilder';
      pluginQueryParams[mapKeys.FROM] = 'agentBuilder';
    }
    if (this.workSpaceId) {
      mcpQueryParams[mapKeys.WORK_SPACE_ID] = this.workSpaceId;
      pluginQueryParams[mapKeys.WORK_SPACE_ID] = this.workSpaceId;
    }
    if (this.isMcp) {
      window[mapKeys.DEVELOP_SPACE_MCP] = this.isFromDevelopSpace;
    } else {
      window[mapKeys.DEVELOP_SPACE_PLUGIN] = this.isFromDevelopSpace;
    }
    this.isMcp
      ? this.router.navigate(['/home/agent-center/mcp-service/detail'], {
          queryParams: mcpQueryParams,
        })
      : this.router.navigate(['/home/agent-center/custom-plugin/detail'], {
          queryParams: pluginQueryParams,
        });
    if (this.type === CARD_LIST_TYPE.SKILL) {
      this.router.navigate(['/home/skill/detail'], {
        queryParams: {
          id: this.data.skill_id,
        },
      });
    }
  }

  public deleteMcp() {
    this.mcpService
      .deleteService(this.data.id)
      .then(() => {
        MessageComponent.showSuccess(this.i18n.transform('deploy_mcp_success'), 3000);
        this.handleAction.emit({
          action: 'delete',
        });
      })
      .catch(() => {
        MessageComponent.showError(this.i18n.transform('deploy_mcp_fail'), 3000);
      });
  }

  public deleteMCPServer(context: any) {
    this.mcpService
      .deleteService(this.data.id)
      .then(() => {
        MessageComponent.showSuccess(this.i18n.transform('deploy_mcp_success'), 3000);
        context.dismiss();
        this.handleAction.emit({
          action: 'delete',
        });
      })
      .catch(() => {
        MessageComponent.showError(this.i18n.transform('deploy_mcp_fail'), 3000);
      });
  }

  public deleteSkillModal(id: string, name: string) {
    this.skillCommonService.onDeleteSkill({
      skill_id: id,
      name,
    });
  }

  isZH(): boolean {
    return this.lang === 'zh-cn';
  }

  // 复制MCP的错误码
  copyData() {
    copyToClipboard(this.mcpDeployFail.code);
  }

  // 编辑mcp服务
  public handleEditMcpServer(mcpServiceId: string) {
    this.getMcpServiceDetail(mcpServiceId);
  }

  private setStatus() {
    if (this.data.deploy_type) {
      this.data.status_cn = this.data.deploy_type.toUpperCase();
      this.data.status_en = this.data.deploy_type.toUpperCase();
      return '';
    }
    return 'null';
  }

  private setType() {
    if (this.data.type === 'inner') {
      this.data.type_cn = this.i18n.transform('platform');
      this.data.type_en = 'Platform';
    } else {
      this.data.type_cn = this.i18n.transform('user');
      this.data.type_en = 'User';
    }
  }

  private getStatus() {
    if (this.data?.fc_instance_status === 'success') {
      return 'deployed';
    }
    if (this.data?.fc_instance_status === 'creatingStack') {
      return 'deploying';
    }
    if (this.data?.fc_instance_status === 'pendingTools') {
      return 'pendingTools';
    }
    return 'deployFailed';
  }

  private getColor() {
    if (this.status === 'published' || this.status === 'deployed') {
      return 'green';
    }
    if (this.status === 'deploying' || this.status === 'unpublished' || this.status === 'pendingTools') {
      return 'orange';
    }
    return 'red';
  }

  // 获取mcp的详情信息
  private getMcpServiceDetail(mcpServiceId: string) {
    this.api.getServiceDetail(mcpServiceId).then((res: any) => {
      if (!res) {
        return;
      }
      res.icon = res.icon || MCP_DEFAULT_ICON_BASE64_STR;
      this.openMcpServiceModal(res);
    });
  }

  // 打开mcp service的抽屉
  private openMcpServiceModal(mcpServiceData: McpInterfaces) {
    const thisNzModal: any = this.nzModal.create({
      nzContent: AddMcpHalfModalComponent,
      nzWidth: '1000px',
    });
    const instance = thisNzModal.getContentComponent();
    instance.type = 'service';
    instance.selectMcpData = mcpServiceData;
    instance.action = 'edit';
    instance.close.subscribe(() => {
      this.refreshMcpListEmit.emit(true);
    });
  }

  private onClick(event: Event) {
    event.stopPropagation();
  }

  private copyID() {
    this.clipboard.copy(this.data.plugin_id);
    MessageComponent.showSuccess(this.i18n.transform('id_copied_successfully'));
  }

  private toDel(): void {
    // 两种id用不同字段名，同时只存在一个
    const id = this.data.id || this.data.plugin_id || this.data.skill_id;
    const name = this.data.name || this.data.skill_name;
    if (this.isMcp) {
      if (this.data.fc_instance_status === 'creatingStack') {
        MessageComponent.showWarn(this.i18n.transform('componentcardcomponent_1173'));
        return;
      }
    }
    if (this.type === CARD_LIST_TYPE.SKILL) {
      this.deleteSkillModal(id, name);
      return;
    }
    this.deleteVersionModal(this.data);
  }

  private showMcpFailBox() {
    this.mcpFailTemplateVisible = true;
  }

  private exportSkill(data) {
    const skillId = data.skill_id;
    this._skillApi.exportSkillVersion(skillId).subscribe(response => {
      if (!response) {
        return;
      }
      this.skillCommonService.downloadFile(response.obs_url);
    });
  }

  private addCardActionWithMcpFail() {
    if (this.data.fc_instance_status === 'stackFail') {
      const failCardAction = [
        {
          id: 'reason',
          label: this.i18n.transform('view_reason'),
          disabled: !this.subscribeBtnStatus || (this.isMcp && this.data.fc_instance_status === 'creatingStack'),
          tip: '',
        },
        {
          id: 'edit',
          label: this.i18n.transform('edit'),
          disabled: !this.subscribeBtnStatus || (this.isMcp && this.data.fc_instance_status === 'creatingStack'),
          tip: '',
        },
        {
          id: 'redeploy',
          label: this.i18n.transform('redeploy'),
          disabled: !this.subscribeBtnStatus || (this.isMcp && this.data.fc_instance_status === 'creatingStack'),
          tip: '',
        },
      ];
      this.cardActions = [...failCardAction, ...this.cardActions];
    }
  }

  private setMcpDeployFail(failInfo: McpDeployFailType) {
    this.mcpDeployFail.code = failInfo.code || 'Openjiuwen.Unknown';
    this.mcpDeployFail.debug_info = failInfo.debug_info || 'N/A';
    this.mcpDeployFail.message_cn = failInfo.message_cn || '服务状态异常。';
    this.mcpDeployFail.message_en = failInfo.message_en || 'Service Status Abnormal.';
    this.mcpDeployFail.reason_en = failInfo.reason_en || 'The system detected an abnormal deployment status, but no specific error code was captured.';
    this.mcpDeployFail.reason_cn = failInfo.reason_cn || '系统检测到服务部署状态异常，但未捕获到明确的错误码。';
    this.mcpDeployFail.suggestion_cn = failInfo.suggestion_cn || '请检查服务配置是否正确并重新部署。如多次失败，请联系管理员排查。';
    this.mcpDeployFail.suggestion_en =
      failInfo.suggestion_en || 'Please verify the configuration and redeploy. If the failure persists, contact the administrator.';
  }
}
