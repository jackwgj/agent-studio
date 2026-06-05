import { Component, ElementRef, Input, OnInit, inject } from '@angular/core';
import { Validators } from '@angular/forms';
import * as angularI18next from 'angular-i18next';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { CommonUtils } from '../../../utils/common.util';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { AppPluginRepoService } from '@services/agent-center/app-plugin-repo.service';
import { distinctUntilChanged } from 'rxjs';
import { cdnAssetUrl } from '../../../single-spa/assets-url';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzSpaceModule } from 'ng-zorro-antd/space';
import { NzMessageService } from 'ng-zorro-antd/message';

enum AuthMode {
  NORMAL = 0,
  FORCE = 1,
}

@Component({
  selector: 'add-plugin-auth-components',
  templateUrl: './add-plugin-auth.component.html',
  styleUrls: ['./add-plugin-auth.component.less'],
  standalone: true,
  imports: [MODULES, NzFormModule, NzInputModule, NzButtonModule, NzAlertModule, NzSpaceModule],
  providers: [
    NzMessageService,
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.AGENT_CENTER, I18nNamespace.TOOL],
    },
  ],
})
export class AddPluginAuthComponent implements OnInit {
  readonly modalRef = inject(NzModalRef);
  @Input() pluginInfo: any;
  public _pluginCredentialsMode: AuthMode = AuthMode.NORMAL;
  public errTip: string = '';
  public errDetail: string = '';
  public showErrDetail: boolean = false;
  public lang: string = '';
  loading: boolean = false;
  authFormControl: FormGroup;

  constructor(
    private readonly i18n: angularI18next.I18NextEagerPipe,
    fb: FormBuilder,
    private elementRef: ElementRef,
    private pluginMarketService: AppPluginRepoService,
    private nzMessageService: NzMessageService
  ) {
    this.lang = CommonUtils.getLanguage();
    this.authFormControl = fb.group({
      auth_key: new FormControl('', [Validators.required]),
    });
    // 当auth_key值发生变化时，设置插件凭证模式为正常模式(需执行校验）
    this.authFormControl
      .get('auth_key')
      .valueChanges.pipe(distinctUntilChanged())
      .subscribe(value => {
        this.pluginCredentialsMode = AuthMode.NORMAL;
      });
  }

  public get pluginCredentialsMode() {
    return this._pluginCredentialsMode;
  }

  public set pluginCredentialsMode(val: AuthMode) {
    this._pluginCredentialsMode = val;
    if (val === AuthMode.FORCE) {
      this.errTip = this.i18n.transform('auth_validate_failed_tip');
    } else if (val === AuthMode.NORMAL) {
      this.errTip = '';
    } else {
      this.errTip = '';
    }
  }

  checkGroup(): boolean {
    Object.keys(this.authFormControl.controls).forEach(key => {
      this.authFormControl.get(key).markAsDirty();
      this.authFormControl.get(key).updateValueAndValidity();
    });
    if (this.authFormControl.invalid) {
      const firstError = Object.keys(this.authFormControl.controls)[0];
      this.elementRef.nativeElement.querySelector(`[formControlName=${firstError}]`).focus();
      return false;
    }
    return true;
  }

  ngOnInit() {
    if (this.pluginInfo.credential_status === 'AUTHENTICATED') {
      this.authFormControl.controls.auth_key.setValue('********');
    }
  }

  public close() {
    this.modalRef.destroy();
  }

  public dismiss() {
    this.modalRef.destroy();
  }

  public handleClickOnSubmit(): void {
    if (!this.checkGroup()) {
      return;
    }
    if (this.pluginInfo.credential_status === 'AUTHENTICATED') {
      this.deletePluginCredentials();
    } else {
      this.configPluginCredentials();
    }
  }

  configPluginCredentials() {
    let auth_info = this.pluginInfo.auth_info;
    auth_info.auth_keys[0].auth_key = this.authFormControl.controls.auth_key.value;
    let params = {
      auth_keys: auth_info.auth_keys,
    };
    const need_validate = this.pluginCredentialsMode === AuthMode.NORMAL;

    this.loading = true;
    this.pluginMarketService
      .setPluginCredentials(this.pluginInfo.plugin_id, params, need_validate)
      .then((res: any) => {
        this.nzMessageService.success(this.i18n.transform('auth_config_success'));
        this.close();
      })
      .catch(error => {
        if (error?.data?.error_code === 'Openjiuwen.02401121') {
          // 鉴权校验失败，让用户决定是否要继续保存鉴权
          this.pluginCredentialsMode = AuthMode.FORCE;
          this.errDetail = error?.data?.details[0]?.error_msg ?? '';
        } else {
          // 其他错误
          this.nzMessageService.error(this.i18n.transform('auth_config_failed'));
        }
      })
      .finally(() => {
        this.loading = false;
      });
  }

  deletePluginCredentials() {
    this.loading = true;
    this.pluginMarketService
      .deletePluginCredentials(this.pluginInfo.plugin_id)
      .then((res: any) => {
        this.nzMessageService.success(this.i18n.transform('remove_auth_success'));
        this.close();
      })
      .catch(error => {
        this.nzMessageService.error(this.i18n.transform('remove_auth_failure'));
      })
      .finally(() => {
        this.loading = false;
      });
  }

  protected readonly AuthMode = AuthMode;
  protected readonly changeUrl = cdnAssetUrl;
}
