import {
  ChangeDetectionStrategy,
  Component, EventEmitter,
  Input, Output, inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageComponent } from '@shared/services/cfdata.service';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

@Component({
  selector: 'mcp-auth-modal',
  templateUrl: './mcp-auth-modal.component.html',
  styleUrls: ['./mcp-auth-modal.component.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MODULES,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.AGENT,
        I18nNamespace.COMMON,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.MODEL_ACCESS,
      ],
    },
  ],
})
export class McpAuthModalComponent {
  readonly modalRef = inject(NzModalRef);
  @Input() id: string = '';
  @Output() saveSuccess = new EventEmitter<any>();
  public authSecretPosition = [
    {
      id: '1',
      label: 'Header',
      value: 'HEADERS',
    },
    {
      id: '2',
      value: 'QUERY',
      label: 'Query',
    },
  ];

  public mcpAuthConfigData = {
    selectedAuthType: '',
    selectedAuthPos: 'HEADERS',
    authQueryParams: [],
    authHeaderParams: [],
    authParams: {
      url: '',
      id: '',
      secret: '',
      scope: '',
    },
  };

  public authConfigType = [
    {
      id: '1',
      label: this.i18n.transform('mcp_no_auth'),
      value: '',
    },
    {
      id: '2',
      value: 'SERVICE',
      label: this.i18n.transform('mcp_api_auth'),
    },
    {
      id: '3',
      label: this.i18n.transform('mcp_oauth_auth'),
      value: 'OAUTH',
    },
  ];
  public isDisabledConfirmBtn: boolean = false;

  constructor(
    private appFlowRepoServe: AppFlowRepoService,
    private i18n: I18NextEagerPipe,
  ) {}

  ngOnInit() {

  }

  public dismiss() {
    this.modalRef.destroy();
  }


  public confirm() {
    const params = this.setAuthConfig();
    this.appFlowRepoServe.saveMcpAutheConfig(params, this.id).then(res => {
      this.saveSuccess.emit();
      this.modalRef.destroy();
    }).catch(() => {
      MessageComponent.showError(this.i18n.transform('auth_fail'), 3000)
    });
  }

  /** 设置鉴权信息 **/
  private setAuthConfig() {
    let params:any = {};
    if (!this.mcpAuthConfigData.selectedAuthType) {
      params = {};
    }
    if (this.mcpAuthConfigData.selectedAuthType === 'SERVICE') {
      params = {
        scope: 'SERVICE',
        domain: 'HEADERS',
        auth_keys: [],
      };
      if (this.mcpAuthConfigData.selectedAuthPos === 'HEADERS') {
        for (let index = 0; index < this.mcpAuthConfigData.authHeaderParams.length; index++) {
          params.auth_keys.push({
            auth_key: this.mcpAuthConfigData.authHeaderParams[index].value,
            target_name: this.mcpAuthConfigData.authHeaderParams[index].name,
          });
        }
      } else {
        params.domain = 'QUERY';
        for (
          let index = 0;
          index < this.mcpAuthConfigData.authQueryParams.length;
          index++
        ) {
          params.auth_keys.push({
            auth_key: this.mcpAuthConfigData.authQueryParams[index].value,
            target_name: this.mcpAuthConfigData.authQueryParams[index].name,
          });
        }
      }
    }
    if (this.mcpAuthConfigData.selectedAuthType === 'OAUTH') {
      params = {
        scope: 'OAUTH',
        custom_oauth: {
          client_secret: this.mcpAuthConfigData.authParams.secret,
          scope: this.mcpAuthConfigData.authParams.scope,
          endpoint_url: this.mcpAuthConfigData.authParams.url,
          client_id: this.mcpAuthConfigData.authParams.id,
        },
      };
    }
    return params;
  }

  /** 添加api-key/header或者query认证参数 **/
  public handleClickAddAPIParams() {
    if (this.mcpAuthConfigData.selectedAuthPos === 'HEADERS') {
      this.mcpAuthConfigData.authHeaderParams.push({ name: '', value: '' });
    } else {
      this.mcpAuthConfigData.authQueryParams.push({ name: '', value: '' });
    }
  }

  /** 删除api-key/header或者query认证参数 **/
  public handleClickDeleteAPIParams(index: number) {
    if (this.mcpAuthConfigData.selectedAuthPos === 'HEADERS') {
      if (this.mcpAuthConfigData.authHeaderParams.length <= 0) {
        return;
      }
      this.mcpAuthConfigData.authHeaderParams.splice(index, 1);
      return;
    }
    if (this.mcpAuthConfigData.authQueryParams.length <= 0) {
      return;
    }
    this.mcpAuthConfigData.authQueryParams.splice(index, 1);
  }

  /** 是否展示api-key的参数名称 **/
  public isShowAPIParams() {
    if (this.mcpAuthConfigData.selectedAuthType !== 'SERVICE') {
      return false;
    }
    if (this.mcpAuthConfigData.selectedAuthPos === 'HEADERS') {
      return this.mcpAuthConfigData.authHeaderParams.length > 0;
    }
    if (this.mcpAuthConfigData.selectedAuthPos === 'QUERY') {
      return this.mcpAuthConfigData.authQueryParams.length > 0;
    }
    return false;
  }

  /** 是否展示Oauth的配置信息 **/
  public isShowOauthConfig() {
    return this.mcpAuthConfigData.selectedAuthType === 'OAUTH';
  }

  /** 是否展示密钥位置 **/
  public isShowSecretPos() {
    return this.mcpAuthConfigData.selectedAuthType === 'SERVICE';
  }

  /** 是否展示参数列表 **/
  public isShowParamList() {
    return this.mcpAuthConfigData.selectedAuthType === 'SERVICE';
  }

  // 切换APIKEY中的Header和Query
  public handleChangeAPIKeyRadio() {
    if (this.mcpAuthConfigData.selectedAuthType !== 'SERVICE') {
      this.isDisabledConfirmBtn = false;
      return;
    }
    if (this.mcpAuthConfigData.selectedAuthPos === 'HEADERS') {
      this.isDisabledConfirmBtn = false;
      if (this.mcpAuthConfigData.authHeaderParams.length <= 0) {
        this.isDisabledConfirmBtn = false;
        return;
      }
      for (let index = 0; index < this.mcpAuthConfigData.authHeaderParams.length; index++) {
        if (this.mcpAuthConfigData.authHeaderParams[index].value === '' || this.mcpAuthConfigData.authHeaderParams[index].name === '') {
          this.isDisabledConfirmBtn = true;
          break;
        }
      }
      return;
    }
    if (this.mcpAuthConfigData.selectedAuthPos === 'QUERY') {
      this.isDisabledConfirmBtn = false;
      if (this.mcpAuthConfigData.authQueryParams.length <= 0) {
        this.isDisabledConfirmBtn = false;
        return;
      }
      for (let index = 0; index < this.mcpAuthConfigData.authQueryParams.length; index++) {
        if (this.mcpAuthConfigData.authQueryParams[index].value === '' || this.mcpAuthConfigData.authQueryParams[index].name === '') {
          this.isDisabledConfirmBtn = true;
          break;
        }
      }
      return;
    }
    return;
  }

  // 切换鉴权方式要取消按钮的置灰操作
  handleChangeAuthWay() {
    if (this.mcpAuthConfigData.selectedAuthType === 'SERVICE') {
      this.handleChangeAPIKeyRadio();
      return;
    }
    this.isDisabledConfirmBtn = false;
  }

  protected readonly changeUrl = cdnAssetUrl;
}
