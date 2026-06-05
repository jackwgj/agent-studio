import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MODULES } from '@shared/modules';
import { SharedModule } from '@shared/shared.module';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { agentCommonLogic } from '@routes/agent-center/app-agent/common-logic-agent';
import { CommonUtils } from '../../../utils/common.util';
import { cdnAssetUrl } from '../../../single-spa/assets-url';
import { formatTime } from 'src/utils/commonFn';
import { EnvironmentVariablesManagementService } from './environment-variables-management.service';
import { CommonService } from '@services/common.service';
import { NestedTableComponent } from './nested-table/nested-table.component';
import { HttpService } from '@services/http.service';
import { ConfigEnvVariableComponent } from './config-env-variable/config-env-variable.component';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzModalModule, NzModalService, NzModalRef } from 'ng-zorro-antd/modal';
import { NzDrawerModule, NzDrawerService } from 'ng-zorro-antd/drawer';
import { NzUploadModule, NzUploadFile } from 'ng-zorro-antd/upload';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'env-management-variables-list',
  templateUrl: './environment-variables-management-list.component.html',
  styleUrls: ['./environment-variables-management-list.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MODULES,
    SharedModule,
    NestedTableComponent,
    NzTableModule,
    NzPaginationModule,
    NzButtonModule,
    NzIconModule,
    NzModalModule,
    NzDrawerModule,
    NzUploadModule,
    NzAlertModule,
    NzCheckboxModule,
    NzSpinModule,
    NzToolTipModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.PLATFORM_MANAGEMENT,
        I18nNamespace.TRACE,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.AGENT,
        I18nNamespace.PROMPT_PLATFORM
      ],
    },
    NzModalService,
    NzDrawerService,
    agentCommonLogic,
  ],
})
export class EnvironmentVariablesManagementListComponent implements OnInit {
  @ViewChild('importModal') importModal!: TemplateRef<any>;

  subscribeBtnStatus = this.commonService.getSubscribeStatus();

  srcData: any = { data: [], state: undefined };
  listLoading = false;

  noDataTitle = this.i18n.transform('no_data');
  noDataDesc = [{ type: 'html', html: this.i18n.transform('no_env_variable_data_prompt') }];
  envCurrentPage = 1;
  envTotalNumber = 0;
  envPageSize = { options: [10, 20, 50, 100], size: 10 };
  duplicateImportData: any[] = [];
  environmentId = '';
  importFile: File | null = null;
  fileList: NzUploadFile[] = [];
  importModalRef: NzModalRef | null = null;
  importLoading = false;

  private lang = CommonUtils.getLanguage();

  constructor(
    private envVariablesManagement: EnvironmentVariablesManagementService,
    private nzDrawerService: NzDrawerService,
    private nzModalService: NzModalService,
    public commonService: CommonService,
    private i18n: I18NextEagerPipe,
    private http: HttpService,
    private commonLogic: agentCommonLogic,
    private message: NzMessageService,
) {}

  ngOnInit() {
    this.getListData();
  }

  selectAll(): void {
    if (this.duplicateImportData && this.duplicateImportData.length > 0) {
      this.duplicateImportData = this.duplicateImportData.map((item) => ({
        ...item,
        checked: true
      }));
    }
  }

  deselectAll(): void {
    if (this.duplicateImportData && this.duplicateImportData.length > 0) {
      this.duplicateImportData = this.duplicateImportData.map((item) => ({
        ...item,
        checked: false
      }));
    }
  }

  public listAction($event: any, row: any): void {
    if ($event.id === 'import') {
      this.environmentId = row.id;
      this.importVariables();
    }
  }

  public openConfigModal(row: any): void {
    const drawerRef = this.nzDrawerService.create({
      nzContent: ConfigEnvVariableComponent,
      nzWidth: '700px',
      nzPlacement: 'right',
      nzData: {
        context: {
          id: row.id,
          rowData: row,
          outputs: {
            getList: () => this.getListData(),
          },
          close: () => {
            const comp = drawerRef.getContentComponent() as any;
            if (comp && typeof comp.isFormValid === 'function') {
              if (comp.isFormValid()) {
                if (typeof comp.confirm === 'function') {
                  comp.confirm();
                }
                drawerRef.close();
              }
            } else {
              drawerRef.close();
            }
          },
          dismiss: () => drawerRef.close()
        },
      } as any
    });
  }

  public confirmExport(row: any): void {
    this.nzModalService.confirm({
      nzTitle: this.i18n.transform('export_env_variables_tip'),
      nzOkText: this.i18n.transform('export'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOnOk: () => {
        this.exportVariables(row.id);
      }
    });
  }

  beforeUpload = (file: NzUploadFile): boolean => {
    if (file.size! > 10485760) {
      this.message.error(this.i18n.transform('file_size_error_10M'));
      return false;
    }

    this.importFile = file as any;
    const formData = new FormData();
    formData.append('file', file as any);
    formData.append('environment_id', this.environmentId);

    this.importLoading = true;
    this.envVariablesManagement.verifyImport(formData).then((res) => {
      if (res.result === true) {
        const arr = (res.variables || []).map((item: any) => ({
          ...item,
          checked: item.validateResult === 'exist',
        }));
        this.duplicateImportData = arr.filter((item) => item.validateResult === 'exist');
        this.message.success(this.i18n.transform('verify_success'));
      } else {
        this.message.error(this.i18n.transform('verify_failed'));
        this.resetImportState();
      }
    }).catch(() => {
      this.resetImportState();
    }).finally(() => {
      this.importLoading = false;
    });

    return false;
  };

  removeFile = (file: NzUploadFile): boolean => {
    this.resetImportState();
    return true;
  };

  exportVariables(id: string): void {
    this.envVariablesManagement.exportEnvVariables({ environment_id: [id] }).then((res) => {
      CommonUtils.downloadFile(
        new Blob([res], { type: 'application/json' }),
        `${this.commonLogic.getFormattedDateTime()}.jsonl`,
        true
      );
    });
  }

  closeImportantConfirm() {
    this.nzModalService.confirm({
      nzTitle: this.i18n.transform('environment_edit_confirm_title'),
      nzContent: this.i18n.transform('environment_edit_confirm_content'),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOnOk: () => {
        this.doImport();
      }
    });
  }

  importVariables(): void {
    this.resetImportState();
    this.importModalRef = this.nzModalService.create({
      nzTitle: this.i18n.transform('importing_environment_variables'),
      nzContent: this.importModal,
      nzFooter: null,
      nzWidth: 600,
      nzOnCancel: () => {
        this.resetImportState();
      }
    });
  }

  private doImport() {
    if (!this.importFile) {
      this.message.error(this.i18n.transform('select_file_tip'));
      return;
    }

    const coverVars = this.duplicateImportData
      .filter(item => item.checked)
      .map(item => item.variableName);
    const formData = new FormData();
    formData.append('file', this.importFile);
    formData.append('environment_id', this.environmentId);
    formData.append('cover_variables', new Blob([JSON.stringify(coverVars)], { type: 'application/json' }));

    this.envVariablesManagement.importEnvVariables(formData).then(() => {
      this.getListData();
      this.resetImportState();
      if (this.importModalRef) {
        this.importModalRef.destroy();
      }
    });
  }

  private resetImportState(): void {
    this.importFile = null;
    this.duplicateImportData = [];
    this.fileList = [];
  }

  pageIndexChange(page: number): void {
    this.envCurrentPage = page;
    this.getListData();
  }

  pageSizeChange(size: number): void {
    this.envPageSize.size = size;
    this.envCurrentPage = 1;
    this.getListData();
  }

  public async getListData(): Promise<void> {
    this.listLoading = true;
    try {
      const res = await this.envVariablesManagement.getEnvVariablesList({
        limit: this.envPageSize.size,
        offset: (this.envCurrentPage - 1) * this.envPageSize.size,
      });
      this.srcData.data = (res.variables || []).map((item: any) => ({ ...item, expand: false }));
      this.envTotalNumber = res.total;
    } finally {
      this.listLoading = false;
    }
  }

  public trackByFn(index: number, item: any): any {
    return item.id || index;
  }

  isZH(): boolean {
    return this.lang === 'zh-cn';
  }

  protected readonly changeUrl = cdnAssetUrl;
  protected readonly formatTime = formatTime;
}
