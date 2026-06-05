import { Component, Input, OnDestroy, OnInit, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { UploadImageComponent } from '@routes/knowledge-center/components/upload-image/upload-image.component';
import {
  KbConnectionTypeCardComponent,
} from '@routes/knowledge-center/kb-source-connection/kb-connection-type-card/kb-connection-type-card.component';
import { KbUtils } from '@routes/knowledge-center/kb.utils';
import { ConnectionFormType, ConnectionUiData, ConnectionUiType } from '@routes/knowledge-center/knowledge.types';
import { KnowledgeBaseConstant } from '@routes/knowledge-center/utils/knowledge-base.constant';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { filter, Subject, takeUntil } from 'rxjs';
import { NzDrawerRef } from 'ng-zorro-antd/drawer';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'kb-source-connection-modal',
  templateUrl: './kb-source-connection-modal.component.html',
  styleUrls: ['./kb-source-connection-modal.component.less'],
  standalone: true,
  imports: [
    MODULES,
    KbConnectionTypeCardComponent,
    UploadImageComponent,
    CdkTextareaAutosize,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.COMMON,
      ],
    },
  ],
})
export class KbSourceConnectionModalComponent implements OnInit, OnDestroy {
  @Input() connectionId: string;
  @Output() editSuccess = new EventEmitter<void>();

  connectionFormGroup = new FormGroup({
    type: new FormControl(null, [Validators.required]),
    name: new FormControl('', [
      Validators.required, Validators.pattern(/^[A-Za-z0-9\u4e00-\u9fa5][A-Za-z0-9\u4e00-\u9fa5_-]{0,49}$/),
    ]),
    description: new FormControl('', [Validators.maxLength(255)]),
    icon: new FormControl('', []),
  });

  connectionParamGroup: FormGroup;

  typeData = [];

  connectionParamUiData: ConnectionUiData[] = [];

  FORM_TYPE_MAP = new Map<ConnectionUiType, {
    type: ConnectionFormType,
  }>([
    [
      ConnectionUiType.STRING, {
      type: ConnectionFormType.INPUT,
    },
    ],
    [
      ConnectionUiType.SECRET, {
      type: ConnectionFormType.SECRET,
    },
    ],
  ]);

  connectionDetail;

  initialImage = KnowledgeBaseConstant.KNOWLEDGE_BASE_ACCESS_ICON;

  secretKeys = [];

  isVisible = true;

  isConnectChange = false;

  isLoading = false;

  isChecking = false;

  drawerRef: NzDrawerRef;

  protected readonly ConnectionFormType = ConnectionFormType;
  protected readonly Boolean = Boolean;
  private destroy$ = new Subject<void>();

  constructor(
    private knowledgeRepoService: KnowledgeRepoService,
    private readonly i18n: I18NextEagerPipe,
    private message: NzMessageService,
  ) {
  }

  ngOnInit() {
    this.getConnectors();
    this.connectionFormGroup.get('type').valueChanges
      .pipe(
        takeUntil(this.destroy$),
        filter(() => !this.isChecking),
      )
      .subscribe(() => {
        if (!this.isChecking) {
          this.changeType();
        }
      });
  }

  getConnectors() {
    this.knowledgeRepoService.getThirdPartyKnowledgeBaseConnectors().then(res => {
      const { connectors = [] } = res;
      this.typeData = connectors;

      if (this.connectionId) {
        this.getConnectionDetail();
      } else {
        this.connectionFormGroup.patchValue({
          type: this.typeData[0]?.id,
        });
      }
    });
  }

  getConnectionDetail() {
    this.knowledgeRepoService.getThirdPartyKnowledgeBaseDetail(this.connectionId).then(res => {
      const detail = res?.knowledge_base_connection_detail;
      if (detail) {
        this.connectionDetail = detail;
        this.renderData();
      }
    });
  }

  changeType() {
    this.generateConnectorForm();

    if (this.connectionDetail) {
      this.renderParamData();
      this.isConnectChange = false;
    }
  }

  generateConnectorForm() {
    const connectorId = this.connectionFormGroup.getRawValue().type;
    const connectorInfo = this.typeData.find(item => item.id === connectorId);

    this.connectionParamGroup = new FormGroup({});
    this.connectionParamUiData = [];
    if (connectorInfo) {
      const { param_definition = [] } = connectorInfo;
      param_definition.forEach(param => {
        let validators = [];

        if (param.need === 'Y') {
          validators.push(Validators.required);
        }

        if (param.pattern) {
          validators.push(Validators.pattern(param.pattern));
        }

        const uiData = {
          name: param.name,
          code: param.code,
          placeholder: param.remark ?? '',
          ...this.FORM_TYPE_MAP.get(param.type) ?? {},
        };

        this.connectionParamUiData.push(uiData);

        const control = new FormControl('', validators);
        this.connectionParamGroup.addControl(param.code, control);
      });
    }
  }

  renderData() {
    if (this.connectionDetail) {
      this.initialImage = this.connectionDetail.icon;

      this.connectionFormGroup.patchValue({
        name: this.connectionDetail.name,
        description: this.connectionDetail.description,
        type: this.connectionDetail.connector_id,
      });

      this.renderParamData();

      // patch后执行，区分初始化
      this.connectionParamGroup.valueChanges
        .pipe(
          takeUntil(this.destroy$),
          filter(() => !this.isChecking),
        )
        .subscribe(() => {
          if (!this.isConnectChange) {
            this.isConnectChange = true;
            this.secretKeys.forEach(key => {
              this.connectionParamGroup.get(key).setValue('');
            });
          }
        });
    }
  }

  private renderParamData() {
    let pathValue: Record<string, any> = {};
    this.connectionDetail.params?.forEach(param => {
      const { code, value } = param;
      const targetType = this.connectionParamUiData?.find(ui => ui.code === code)?.type;
      if (targetType && targetType === ConnectionFormType.SECRET) {
        pathValue[code] = `******（${this.i18n.transform('fill_form_again_when_change')}）`;
        this.secretKeys.push(code);
      } else {
        pathValue[code] = value;
      }
    });
    this.connectionParamGroup.patchValue({
      ...pathValue,
    });
  }

  async onImageChanged(file: any) {
    if (!file) {
      return;
    }
    KbUtils.readFileAsBase64(file).then(res => {
      this.connectionFormGroup.patchValue({
        icon: res,
      });
    })
  }

  isTypeAllowed(): boolean {
    return ['LakeSearch', 'Ragflow', 'common-api', 'KooSearch', 'General'].includes(this.connectionFormGroup.get('type').value);
  }

  testConnection() {
    if (this.isConnectChange) {
      this.isChecking = true;

      this.connectionParamGroup.markAllAsTouched();
      const errors = this.connectionParamGroup.invalid;

      this.isChecking = false;

      if (errors) {
        return;
      }
    }

    if (this.connectionDetail?.id && !this.isConnectChange) {
      this.knowledgeRepoService
        .pointTestThirdPartyKnowledgeBase(this.connectionDetail?.id)
        .then(() => {
          this.message.success(this.i18n.transform('test_success'));
        });
    } else {
      this.knowledgeRepoService
        .testThirdPartyKnowledgeBase({
          connector_id: this.connectionFormGroup.getRawValue().type,
          params: this.getConnectionParam(),
        })
        .then(() => {
          this.message.success(this.i18n.transform('test_success'));
        });
    }
  }

  editConnector() {
    return new Promise(resolve => {
      this.getConnectorParams().then(params => {
        if (params) {
          this.isLoading = true;
          if (this.connectionId) {
            this.knowledgeRepoService.editThirdPartyKnowledgeBase(this.connectionId, params).then(() => {
              resolve(true);
              this.isLoading = false;
            }).catch(() => {
              resolve(false);
              this.isLoading = false;
            });
          } else {
            this.knowledgeRepoService.accessThirdPartyKnowledgeBase(params).then(() => {
              resolve(true);
              this.isLoading = false;
            }).catch(() => {
              resolve(false);
              this.isLoading = false;
            });
          }
        } else {
          resolve(false);
        }
      });
    });
  }

  getConnectorParams() {
    return new Promise(resolve => {

      let errors = [];
      this.isChecking = true;

      this.connectionFormGroup.markAllAsTouched();
      errors.push(this.connectionFormGroup.invalid);

      const isConnectionCheck = !this.connectionId || (this.connectionId && this.isConnectChange);

      if (isConnectionCheck) {
        this.connectionParamGroup.markAllAsTouched();
        errors.push(this.connectionParamGroup.invalid);
      }

      this.isChecking = false;

      if (errors.filter(Boolean).length) {
        resolve(null);
      } else {
        const basicInfoValue = this.connectionFormGroup.getRawValue();
        const connectionParam = this.getConnectionParam();

        resolve({
          name: basicInfoValue.name,
          description: basicInfoValue.description,
          connector_id: basicInfoValue.type,
          ...(basicInfoValue.icon && { icon: basicInfoValue.icon }),
          ...(isConnectionCheck ? { params: connectionParam } : {}),
        });
      }
    });
  }

  getConnectionParam() {
    const currentConnectionParam = this.connectionParamGroup.getRawValue();
    if (currentConnectionParam && Object.keys(currentConnectionParam).length > 0) {
      return Object.keys(currentConnectionParam).map(code => {
        return {
          code,
          value: currentConnectionParam[code],
        };
      });
    } else {
      return [];
    }
  }

  dismiss() {
    this.drawerRef.close();
  }

  close() {
    this.editConnector().then(res => {
      if (res) {
        this.drawerRef.close();
        this.editSuccess.emit();
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
