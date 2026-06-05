import { Component, Input } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { MessageComponent } from '@shared/services/cfdata.service';
import { HelpDocKey } from '@constants/support-topic.const';
import { I18nNamespace } from '@i18n';
import { CommonService } from '@services/common.service';
import { ModelManagementService } from '@services/repositories/model-management-new';
import { MODULES } from '@shared/modules';
import { HelpLinksService } from '@shared/services/help-links.service';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { CommonUtils } from 'src/utils/common.util';
import { SafeHtmlPipe } from '../../../../pipes/safehtml.pipe';
import { NzModalModule, NzModalRef } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { inject } from '@angular/core';

interface ModelCheckboxItem {
  id: string;
  text: string;
  disabled: boolean;
  residentModelId: string;
  checked?: boolean;
}

@Component({
  selector: 'meta-model-modal',
  templateUrl: './model-modal.component.html',
  styleUrls: ['./model-modal.component.less'],
  standalone: true,
  imports: [MODULES, SafeHtmlPipe, NzModalModule, NzButtonModule, NzSpinModule, NzCheckboxModule, NzToolTipModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.JIUWEN_MODEL],
    },
  ],
})
export class ModelModalComponent {

  /** 默认勾选的服务，如果不传值默认时批量操作，全部勾选；传空数组全不勾选 */
  @Input() defaultSelect: string[];

  private readonly nzModalRef = inject(NzModalRef);

  public authResult: string;

  public isDefaultSelectAll: boolean;

  public loading = false;

  public modelAutoOpenCheckbox = {
    value: false,
    disabled: false,
  };

  public agreeCheckbox = {
    value: false,
    tipStr: this.i18n.transform('open_modal.please_check_agreement_tip', {
      ns: I18nNamespace.JIUWEN_MODEL,
    }),
    tipShow: true,
  };

  public selectAll = {
    tipStr: this.i18n.transform('open_modal.please_check_model_tip', {
      ns: I18nNamespace.JIUWEN_MODEL,
    }),
    tipShow: true,
  };

  public isAllModelEnabled = false;

  public lang = CommonUtils.getLanguage();

  public modelList: ModelCheckboxItem[] = [];

  public modelCheckedList: ModelCheckboxItem[] = [];

  public isCheckedAll = false;

  private providerId = '';
  modelArtsPriceUrl: string = '';
  constructor(
    private sanitizer: DomSanitizer,
    private i18n: I18NextEagerPipe,
    private modelManagementServ: ModelManagementService,
    private commonServ: CommonService,
    private helpLinksService: HelpLinksService,
  ) {}

  ngOnInit() {
    this.isDefaultSelectAll = !this.defaultSelect;
    this.loading = true;
    Promise.all([this.handleModelList(), this.queryModelSubscribeSetting()])
      .then(([_, isAutoOpenModel]: any) => {
        this.modelAutoOpenCheckbox.value = isAutoOpenModel;
      })
      .finally(() => {
        this.loading = false;
      });
     this.modelArtsPriceUrl = this.helpLinksService.getHelpCenterLink(HelpDocKey.MODELARTS_PRICES);
  }

  public submit() {
    if (!this.agreeCheckbox.value && !this.isAllModelEnabled) {
      this.onAgreeCheckBoxChange(this.agreeCheckbox.value);
      return;
    }

    const hasCheckModel = this.modelCheckedList.filter(
      (item) => !item.disabled,
    );
    if (!hasCheckModel.length && !this.isAllModelEnabled) {
      this.setSelectAllCheckTip(!!hasCheckModel.length);
      return;
    }

    this.loading = true;
    const isSubscribeAll =
      this.modelCheckedList.length === this.modelList.length;
    this.modelManagementServ
      .subscribeModels({
        model_service_ids: isSubscribeAll
          ? []
          : this.modelCheckedList
              .filter((item) => !item.disabled)
              .map((item) => item.residentModelId),
        is_subscribe_all: isSubscribeAll,
        is_auto_subscribe_new_model: this.modelAutoOpenCheckbox.value,
      })
      .then((data) => {
        MessageComponent.showSuccess(
          this.i18n.transform('open_modal.subscribe_successful', {
            ns: I18nNamespace.JIUWEN_MODEL,
          }),
        );
        this.authResult = data.authorization_create_result;
        this.close();
      })
      .finally(() => {
        this.loading = false;
      });
  }

  public close() {
    this.nzModalRef.close(this.authResult);
  }

  public dismiss() {
    this.nzModalRef.close(null);
  }

  public handleModelList() {
    this.providerId = '100';
    const param = {
      provider_id: this.providerId,
      page_num: 1,
      page_size: 500,
    };
    return new Promise((resolve) => {
      this.modelManagementServ
        .getModelList(param)
        .then((modelData) => {
          const openModelList = [];
          const notOpenModelList = [];
          const targetModelList = [];
          (modelData.data || []).forEach((item) => {
            const option: ModelCheckboxItem = {
              id: item.id,
              text: item.service_name,
              disabled: item.is_subscribed,
              residentModelId: item.resident_model_id,
            };
            if (this.defaultSelect?.includes(option.residentModelId)) {
              targetModelList.push(option);
            } else if (item.is_subscribed) {
              openModelList.push(option);
            } else {
              notOpenModelList.push(option);
            }
          });

          this.modelList = [
            ...targetModelList,
            ...notOpenModelList,
            ...openModelList,
          ];

          if (this.isDefaultSelectAll) {
            this.modelCheckedList = [...this.modelList];
            this.isCheckedAll = true;
          } else {
            const defaultSelectList = this.modelList.filter((item) =>
              this.defaultSelect?.includes(item.residentModelId),
            );
            this.modelCheckedList = defaultSelectList.concat(openModelList);
          }

          if (openModelList.length === modelData.data?.length) {
            this.isAllModelEnabled = true;
          }
          resolve(modelData);
        })
        .catch(() => {
          resolve({});
        });
    });
  }

  public queryModelSubscribeSetting() {
    return new Promise((resolve) => {
      if (!this.isDefaultSelectAll) {
        return resolve(false);
      }
      return this.modelManagementServ
        .getModelSubscribeSetting()
        .then((data) => {
          resolve(data?.is_auto_subscribe_new_model || false);
        })
        .catch(() => {
          resolve(false);
        });
    });
  }

  public onAllCheckChange(checked: boolean) {
    if (checked) {
      this.modelCheckedList = [...this.modelList];
    } else {
      this.modelCheckedList = [];
    }
    this.modelAutoOpenCheckbox.disabled =
      this.modelCheckedList.length !== this.modelList.length;
    if (this.modelAutoOpenCheckbox.disabled) {
      this.modelAutoOpenCheckbox.value = false;
    }
    this.setSelectAllCheckTip(!!this.modelCheckedList.length);
  }

  public onModelCheckChange(item: ModelCheckboxItem, checked: boolean) {
    if (checked) {
      if (!this.modelCheckedList.find(m => m.id === item.id)) {
        this.modelCheckedList.push(item);
      }
    } else {
      this.modelCheckedList = this.modelCheckedList.filter(m => m.id !== item.id);
    }
    this.isCheckedAll = this.modelCheckedList.length === this.modelList.length;
    this.modelAutoOpenCheckbox.disabled =
      this.modelCheckedList.length !== this.modelList.length;
    if (this.modelAutoOpenCheckbox.disabled) {
      this.modelAutoOpenCheckbox.value = false;
    }
    this.setSelectAllCheckTip(!!this.modelCheckedList.length);
  }

  onAgreeCheckBoxChange(value) {
    if (value && this.agreeCheckbox.tipShow) {
      this.agreeCheckbox.tipShow = false;
    } else {
      this.agreeCheckbox.tipShow = true;
    }
  }

  setSelectAllCheckTip(value) {
    if (value && this.selectAll.tipShow) {
      this.selectAll.tipShow = false;
    } else {
      this.selectAll.tipShow = true;
    }
  }

  isModelChecked(item: ModelCheckboxItem): boolean {
    return !!this.modelCheckedList.find(m => m.id === item.id);
  }
}
