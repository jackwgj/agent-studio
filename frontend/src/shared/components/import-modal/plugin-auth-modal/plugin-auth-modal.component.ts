import {
  ChangeDetectionStrategy,
  Component,
  Output,
  EventEmitter,
  Input,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppFlowRepoService } from '@services/agent-center/app-flow-repo.service';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { NzModalRef } from 'ng-zorro-antd/modal';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import { cdnAssetUrl } from "../../../../single-spa/assets-url";
import { ApiKeyAuthArguments, IAuthInfo, UserAuthArguments } from '@routes/agent-center/app-plugin/app-plugin.interface';
import { ContextService } from "@services/context.service";
import { AgentConfigService } from "@routes/agent-center/agent-config.service";
import { showFromError } from "@routes/agent-center/app-plugin/utils";

@Component({
  selector: 'plugin-auth-modal',
  templateUrl: './plugin-auth-modal.component.html',
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
        I18nNamespace.COMMON,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.MODEL_ACCESS],
    },
  ],
})
export class PluginAuthModalComponent {
  readonly modalRef = inject(NzModalRef);
  @Input() id: string = '';
  @Output() saveSuccess = new EventEmitter<any>();

  public projectId = '';
  public isOp = false;
  public createPluginFormLabel = {
    defaultPlaceholder: this.i18n.transform('input_placeholder'),
    pluginDescription: this.i18n.transform('description'),
    pluginMethod: this.i18n.transform('request_method'),
    pluginAuth: this.i18n.transform('request_auth'),
    pluginAuthLoc: this.i18n.transform('request_secretKey'),
    requestHeader: this.i18n.transform('request_header'),
    requestArguments: this.i18n.transform('request_params'),
    requestResponse: this.i18n.transform('res_params'),
    projectId: this.i18n.transform('project_id'),
    auth_required: this.i18n.transform('auth_required'),
    metadata: this.i18n.transform('metadata'),
    protocol: this.i18n.transform('plugin_protocol'),
    host: this.i18n.transform('plugin_host'),
    baseURL: this.i18n.transform('plugin_url'),
    pluginLogo: this.i18n.transform('plugin_logo'),
  };

  authRadioList: Array<{ label: string; id: string; show: boolean }> = [
    {
      label: this.i18n.transform('no_auth'),
      id: 'pluginNoNeedAuth',
      show: true,
    },
    {
      label: 'API Key',
      id: 'pluginApiKey',
      show: true,
    },
    {
      label: 'HisIAM',
      id: 'HIS_IAM',
      show: this.configServ.getConfigs()?.his_openai_enable,
    },
    {
      label: 'HisSGOV',
      id: 'SGOV',
      show: this.configServ.getConfigs()?.his_openai_enable,
    },
    {
      label: this.i18n.transform('hw_certificate'),
      id: 'CUSTOM_IAM',
      show: true,
    },
  ];
  authList = [...this.authRadioList];
  userAuthRadioList: Array<{ label: string; id: string }> = [
    {
      label: 'Header',
      id: 'Header',
    },
    {
      label: 'Query',
      id: 'Query',
    },
  ];
  public apiKeyAuthArgs: ApiKeyAuthArguments[] = [
    {
      name: '',
      value: '',
    },
  ];
  authConfigRadioList: Array<{ label: string; id: string }> = [
    {
      label: this.i18n.transform('no_manual_activation'),
      id: 'false',
    },
    {
      label: this.i18n.transform('manual_activation'),
      id: 'true',
    },
  ];

  public iamTip = {
    iamUrl: this.i18n.transform('iam_tip.iam_url', { ns: I18nNamespace.MODEL_ACCESS }),
    iamDomain: this.i18n.transform('iam_tip.iam_domain', {
      ns: I18nNamespace.MODEL_ACCESS,
    }),
    iamProject: this.i18n.transform('iam_tip.iam_project', {
      ns: I18nNamespace.MODEL_ACCESS,
    }),
    iamAK: this.i18n.transform('iam_tip.iam_ak', {
      ns: I18nNamespace.MODEL_ACCESS,
    }),
    iamSK: this.i18n.transform('iam_tip.iam_sk', { ns: I18nNamespace.MODEL_ACCESS }),
    iamUser: this.i18n.transform('iam_tip.iam_user', {
      ns: I18nNamespace.MODEL_ACCESS,
    }),
    iamPassword: this.i18n.transform('iam_tip.iam_password', {
      ns: I18nNamespace.MODEL_ACCESS,
    }),
  };
  verifyMethods = [
    {
      label: this.i18n.transform('verify_methods.iam_username_and_password', { ns: I18nNamespace.MODEL_ACCESS }),
      id: 'iam',
    },
    {
      label: 'Access Key ID/Secret Access Key',
      id: 'aksk',
    },
  ];

  public basicFormControl = this.fb.group({
    pluginAuthMethod: new FormControl(this.authList[0].id),
    pluginApiAuth: new FormControl(this.userAuthRadioList[0].label),
    auth_required: new FormControl(this.authConfigRadioList[0].id),
    metadata: new FormControl(''),
    iamUrl: new FormControl(''),
    sgovUrl: new FormControl(''),
    credential: new FormControl(''),
    iamProject: new FormControl(''),
    appId: new FormControl(''),
    iamAccount: new FormControl(''),
    iamSecret: new FormControl(''),
    iamDomain: new FormControl(''),
    iamEnterprise: new FormControl(''),
    iamUser: new FormControl(''),
    iamPassword: new FormControl(''),
    iamAK: new FormControl(''),
    iamSK: new FormControl(''),
    verify: new FormControl('iam'),
  });
  constructor(
    private i18n: I18NextEagerPipe,
    private fb: FormBuilder,
    private ctxServ: ContextService,
    private configServ: AgentConfigService,
    private appFlowRepoServe: AppFlowRepoService,
  ) {

  }

  ngOnInit() {
    this.isOp = this.ctxServ.isOpAccount;
  }

  dismiss(): void {
    this.modalRef.destroy();
  }

  changeVerifyMethod(verify) {
    if (verify === 'iam') {
      this.basicFormControl.controls.iamUser.setValidators([]);
      this.basicFormControl.controls.iamPassword.setValidators([]);
      this.basicFormControl.controls.iamAK.setValidators([]);
      this.basicFormControl.controls.iamSK.setValidators([]);
      this.basicFormControl.controls.iamAK.setValue('');
      this.basicFormControl.controls.iamSK.setValue('');
    } else {
      this.basicFormControl.controls.iamUser.setValue('');
      this.basicFormControl.controls.iamPassword.setValue('');
      this.basicFormControl.controls.iamUser.setValidators([]);
      this.basicFormControl.controls.iamPassword.setValidators([]);
      this.basicFormControl.controls.iamAK.setValidators([]);
      this.basicFormControl.controls.iamSK.setValidators([]);
    }
  }
  public deleteArgs(key: string, index: number) {
    switch (key) {
      case 'apiAuth': {
        this.apiKeyAuthArgs.splice(index, 1);
        break;
      }
      case 'userAuthArgs': {
        this.userAuthArgs.splice(index, 1);
        break;
      }
      default: {
        break;
      }
    }
  }

  addInputParam() {
    this.apiKeyAuthArgs.push({
      name: '',
      value: '',
    });
  }

  public userAuthArgs: UserAuthArguments[] = [
    {
      name: '',
      sourceName: '',
    },
  ];

  resetControl() {
    this.basicFormControl.controls.sgovUrl.setValidators([]);
    this.basicFormControl.controls.appId.setValidators([]);
    this.basicFormControl.controls.credential.setValidators([]);
    this.basicFormControl.controls.iamUrl.setValidators([]);
    this.basicFormControl.controls.iamAccount.setValidators([]);
    this.basicFormControl.controls.iamProject.setValidators([]);
    this.basicFormControl.controls.iamSecret.setValidators([]);
    this.basicFormControl.controls.iamEnterprise.setValidators([]);
    this.basicFormControl.controls.metadata.setValidators([]);
    this.basicFormControl.controls.iamUser.setValidators([]);
    this.basicFormControl.controls.iamDomain.setValidators([]);
    this.basicFormControl.controls.iamPassword.setValidators([]);
    this.basicFormControl.controls.iamAK.setValidators([]);
    this.basicFormControl.controls.iamSK.setValidators([]);
  }

  changeAuthType(key) {
    this.resetControl();
    if (key === 'pluginApiKey') {
      this.basicFormControl.controls.metadata.setValidators([]);
    }
    if (key === 'HIS_IAM') {
      this.basicFormControl.controls.iamUrl.setValidators([]);
      this.basicFormControl.controls.iamAccount.setValidators([]);
      this.basicFormControl.controls.iamProject.setValidators([]);
      this.basicFormControl.controls.iamSecret.setValidators([]);
      this.basicFormControl.controls.iamEnterprise.setValidators([]);
    }

    if (key === 'SGOV') {
      this.basicFormControl.controls.sgovUrl.setValidators([]);
      this.basicFormControl.controls.appId.setValidators([]);
      this.basicFormControl.controls.credential.setValidators([]);
    }

    if (key === 'CUSTOM_IAM') {
      this.basicFormControl.controls.iamUrl.setValidators([]);
      this.basicFormControl.controls.iamDomain.setValidators([]);
      this.basicFormControl.controls.iamProject.setValidators([]);
      this.basicFormControl.controls.iamUser.setValidators([]);
      this.basicFormControl.controls.iamPassword.setValidators([]);
    }
  }
  public confirm() {
    if (
      this.basicFormControl.controls.auth_required.value === 'true' &&
      !showFromError(this, this.basicFormControl)
    ) {
      return;
    }

    if (
      this.basicFormControl.controls.pluginAuthMethod.value !==
      'pluginNoNeedAuth' &&
      this.basicFormControl.controls.pluginAuthMethod.value !==
      'pluginApiKey' &&
      !showFromError(this, this.basicFormControl)
    ) {
      return;
    }
    const params = this.buildRequestBody();
    this.appFlowRepoServe.savePluginAutheConfig(params,this.id).then(res=>{
      this.saveSuccess.emit();
      this.modalRef.destroy();
    });
  }
  private buildRequestBody() {
    const authMethod = this.basicFormControl.controls.pluginAuthMethod.value;
    const ApiAuth = this.basicFormControl.controls.pluginApiAuth.value;
    const data = {
      auth_required:
        this.basicFormControl.controls.auth_required.value === 'true',
      metadata:
        authMethod === 'pluginApiKey'
          ? this.basicFormControl.controls.metadata.value
          : '',
      authInfo: null,
    };

    let auth: any = {};
    if (authMethod === 'pluginApiKey') {
      auth =
        ApiAuth === 'Header'
          ? { scope: 'SERVICE', headers: {} }
          : { scope: 'SERVICE', query: {} };
      if (ApiAuth === 'Header') {
        for (const param of this.apiKeyAuthArgs) {
          auth.headers[param.name] = param.value;
        }
      } else {
        for (const param of this.apiKeyAuthArgs) {
          auth.query[param.name] = param.value;
        }
      }
    } else if (authMethod === 'HIS_IAM') {
      auth = {
        scope: 'HIS_IAM',
        his_iam_info: {
          iam_url: this.basicFormControl.controls.iamUrl.value,
          iam_project: this.basicFormControl.controls.iamProject.value,
          iam_account: this.basicFormControl.controls.iamAccount.value,
          iam_secret: this.basicFormControl.controls.iamSecret.value,
          iam_enterprise: this.basicFormControl.controls.iamEnterprise.value,
        },
      };
    } else if (authMethod === 'SGOV') {
      auth = {
        scope: 'SGOV',
        his_sgov: {
          app_id: this.basicFormControl.controls.appId.value,
          sgov_url: this.basicFormControl.controls.sgovUrl.value,
          credential: this.basicFormControl.controls.credential.value,
        },
      };
    } else if (authMethod === this.i18n.transform('iam_auth')) {
      auth = {
        scope: 'IAM',
        iam_credentials: {
          projectId: this.projectId,
        },
      };
    } else if (authMethod === 'IAM' || authMethod === 'HIS') {
      auth = {
        scope: authMethod,
        iam_credentials: {
          projectId: this.projectId,
        },
      };
    } else if (authMethod === 'CUSTOM_IAM') {
      auth = {
        scope: 'CUSTOM_IAM',
        custom_iam_credentials: {
          iam_url: this.basicFormControl.controls.iamUrl.value,
          iam_domain: this.basicFormControl.controls.iamDomain.value,
          iam_project: this.basicFormControl.controls.iamProject.value,
          iam_user: this.basicFormControl.controls.iamUser.value,
          iam_password: this.basicFormControl.controls.iamPassword.value,
          iam_ak: this.basicFormControl.controls.iamAK.value,
          iam_sk: this.basicFormControl.controls.iamSK.value,
        },
      };
    }
    if (auth) {
      data.authInfo = this.buildAuthInfo(auth);
    }
    return data;
  }

  private buildAuthInfo(auth: any): IAuthInfo {
    if (Object.keys(auth).length === 0) {
      return {};
    }

    if (auth?.scope === 'SERVICE') {
      const domain = Object.keys(auth).find((key) =>
        ['query', 'headers'].includes(key),
      );

      return {
        domain: domain.toUpperCase(),
        scope: auth.scope,
        auth_keys: Object.keys(auth[domain]).map((key) => ({
          auth_key: auth[domain][key],
          target_name: key,
        })),
      };
    }

    if (auth?.scope === 'IAM' || auth?.scope === 'HIS') {
      return {
        scope: auth.scope,
        iam_credentials: auth.iam_credentials,
      };
    }
    if (auth?.scope === 'CUSTOM_IAM') {
      return {
        scope: auth.scope,
        custom_iam_credentials: auth.custom_iam_credentials,
      };
    }

    if (auth?.scope === 'HIS_IAM' || auth?.scope === 'SGOV') {
      return auth;
    }

    return auth;
  }

  public btnDisabledFunc(): boolean {
    const formControls = this.basicFormControl.controls;
    if (formControls.auth_required.value === 'true' && formControls.metadata.errors) {
      return true;
    }
    if (formControls.pluginAuthMethod.value === 'pluginApiKey') {
      return this.apiKeyAuthArgs.some((param) => !param.name || !param.value);
    }

    if (formControls.pluginAuthMethod.value === 'CUSTOM_IAM') {
      let error = null;
      error =
        formControls.iamUrl.errors || formControls.iamDomain.errors || formControls.iamProject.errors;
      if (formControls.verify.value === 'iam') {
        error = error || formControls.iamUser.errors || formControls.iamPassword.errors;
        return (
          !!error ||
          !formControls.iamDomain.value ||
          !formControls.iamProject.value ||
          !formControls.iamUser.value ||
          !formControls.iamUrl.value ||
          !formControls.iamPassword.value
        );
      } else {
        error = error || formControls.iamAK.errors || formControls.iamSK.errors;
        return (
          !!error ||
          !formControls.iamDomain.value ||
          !formControls.iamProject.value ||
          !formControls.iamUrl.value ||
          !formControls.iamAK.value ||
          !formControls.iamSK.value
        );
      }
    }

    if (formControls.pluginAuthMethod.value === 'HIS_IAM') {
      let error =
        formControls.iamSecret.errors ||
        formControls.iamAccount.errors ||
        formControls.iamUrl.errors ||
        formControls.iamProject.errors ||
        formControls.iamEnterprise.errors;

      return (
        !!error ||
        !formControls.iamAccount.value ||
        !formControls.iamUrl.value ||
        !formControls.iamProject.value ||
        !formControls.iamSecret.value ||
        !formControls.iamEnterprise.value
      );
    }
    if (formControls.pluginAuthMethod.value === 'SGOV') {
      let error =
        formControls.appId.errors || formControls.credential.errors || formControls.sgovUrl.errors;
      return (
        !!error ||
        !formControls.credential.value ||
        !formControls.appId.value ||
        !formControls.sgovUrl.value
      );
    } else {
      this.basicFormControl.controls.auth_required.setValue('false');
    }
    return false;
  }

  protected readonly changeUrl = cdnAssetUrl;
}
