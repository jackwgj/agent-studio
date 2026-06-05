import {
  ChangeDetectionStrategy, ChangeDetectorRef,
  Component,
  EventEmitter,
  Input, OnInit, Output
} from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { KbConstantConfigKey } from '@routes/knowledge-center/knowledge-base.interface';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { MODULES } from "@shared/modules";
import { I18nNamespace } from "@i18n";
import { SetHalfmodalPropsService } from "@shared/services/set-halfmodal-props.service";
import { formatTime } from 'src/utils/commonFn';
import { KnowledgeRepoService } from "@services/agent-center/knowledge.service";
import { cdnAssetUrl } from "../../../../single-spa/assets-url";
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: "meta-obs-setting-modal",
  imports: [
    MODULES,
    NzDrawerModule,
    NzAlertModule,
    NzFormModule,
    NzSelectModule,
    NzButtonModule,
    NzIconModule,
    NzPopconfirmModule,
    NzTableModule,
    NzBadgeModule,
    NzTypographyModule,
    NzEmptyModule,
    NzPaginationModule,
    NzToolTipModule
  ],
  templateUrl: "./obs-setting-modal.component.html",
  styleUrls: ["./obs-setting-modal.component.scss"],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.AGENT_CENTER,
      ]
    },
    NzMessageService
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ObsSettingModalComponent implements OnInit {
  @Input() kbId = '';
  @Output() executeRes = new EventEmitter();
  obsConfigId = '';

  isVisible = false;
  isBucketLoading = false;
  isBucketDirLoading = false;

  public modalTop = "";
  public form: FormGroup;

  srcData: { data: any[], state?: any } = {
    data: [],
    state: undefined,
  };

  noDataDesc: string = this.i18n.transform('obs_config_execute_tip');

  public loading = false;
  currentPage: number = 1;
  totalNumber: number = 0;
  pageSize = {
    options: [10, 20, 50, 100],
    size: 10,
  };

  obsBucketOptions: Array<string> = [];
  obsBucketDirOptions: Array<string> = [];
  selectBucketName = '';
  selectBucketDir = '';

  listColumns: Array<{ title: string, width?: string }> = [
    { title: this.i18n.transform('start_time') },
    { title: this.i18n.transform('end_time') },
    { title: this.i18n.transform('execution_status') },
    { title: this.i18n.transform('execution_log') },
  ];

  obsExecuteStatusMap = new Map<string, any>([
    [
      'RUNNING',
      {
        icon: 'processing',
        text: this.i18n.transform('executing'),
      },
    ],
    [
      'FINISHED',
      {
        icon: 'success',
        text: this.i18n.transform('execution_success'),
      },
    ],
    [
      'FAILED',
      {
        icon: 'error',
        text: this.i18n.transform('execution_failure'),
      },
    ],
  ]);

  showSaveAndExecute = false;
  uploadLimit = 20; // mb

  constructor(
    private fb: FormBuilder,
    private i18n: I18NextEagerPipe,
    private halfmodalPropsServe: SetHalfmodalPropsService,
    private knowledgeService: KnowledgeRepoService,
    private cdr: ChangeDetectorRef,
    private kbAbilitiesService: KbAbilitiesService,
    private nzMessage: NzMessageService
  ) {
    this.modalTop = `${this.halfmodalPropsServe.calcHalfmodalPosition().top}px`;
    this.form = this.fb.group({
      obs_bucket_name: ['', [Validators.required]],
      obs_input_directory: ['', [Validators.required]],
    });
    this.toggleFormState();
  }

  ngOnInit(): void {
    this.getUploadConfig();
  }

  getUploadConfig() {
    this.kbAbilitiesService.getKbConfigWithCache().subscribe(res => {
      const maxSizeConfig = res?.configs?.find(item => item.key === KbConstantConfigKey.FILE_SIZE);

      if (maxSizeConfig) {
        this.uploadLimit = maxSizeConfig.value;
      }
    });
  }

  trackByFn(index: number, item: any): number {
    return item.id;
  }

  public dismiss(): void {
    this.isVisible = false;
  }

  public open() {
    this.currentPage = 1;
    this.totalNumber = 0;
    this.pageSize.size = 10;
    this.showSaveAndExecute = false;
    this.noDataDesc = this.i18n.transform('obs_config_execute_tip');
    this.obsConfigId = '';
    this.toggleFormState();
    this.isVisible = true;

    this.knowledgeService.queryObsConfigs(this.kbId).then(res => {
      if (res?.id) {
        this.selectBucketDir = res.obs_input_directory;
        this.selectBucketName = res.obs_bucket_name;
        this.form.patchValue({
          obs_bucket_name: res.obs_bucket_name,
          obs_input_directory: res.obs_input_directory,
        })
        this.obsConfigId = res.id;
        this.toggleFormState();
      }
      if (this.obsConfigId) {
        this.getExecuteRecords();
        this.noDataDesc = this.i18n.transform('obs_config_execute_tip1');
      } else {
        this.showSaveAndExecute = true;
      }
      this.cdr.detectChanges();
    })
  }

  public closeAndSaveConfig(): void {
    this.saveConfig(false);
  }

  private saveConfig(isExecute?: boolean) {
    Object.values(this.form.controls).forEach(control => {
      if (control.invalid) {
        control.markAsDirty();
        control.updateValueAndValidity({ onlySelf: true });
      }
    });

    let isValid = this.form.valid;
    if (!isValid) {
      return;
    }

    let params = {
      obs_bucket_name: this.form.get("obs_bucket_name").value,
      obs_input_directory: this.form.get("obs_input_directory").value
    };

    this.knowledgeService.saveObsConfig(this.kbId, params).then((res: any) => {
      if (res?.id) {
        if (!isExecute) {
          this.nzMessage.success(this.i18n.transform('save_obs_config_success'));
          this.dismiss();
        }
        this.obsConfigId = res.id;
        this.toggleFormState();
        if (isExecute) {
          this.knowledgeService.executeObsFileUpload(this.kbId, this.obsConfigId).then(res => {
            this.currentPage = 1;
            this.pageSize.size = 10;
            this.nzMessage.success(this.i18n.transform('execute_trigger_success'));
            this.executeRes.emit();
            this.getExecuteRecords();
            this.dismiss();
          })
        }
        this.cdr.detectChanges();
      }
    });
  }

  public closeAndSaveAndExecute(): void {
    this.saveConfig(true);
  }

  public selectObsBucket(event) {
    this.selectBucketName = event;
    this.selectBucketDir = '';
    this.form.patchValue({obs_input_directory: '' });
  }

  public selectObsBucketDir(event) {
    this.selectBucketDir = event;
  }

  public onBeforeOpenObsBucketDir(isOpen: boolean) {
    if (!isOpen) return;
    this.obsBucketDirOptions = [];
    if (this.selectBucketName) {
      this.isBucketDirLoading = true;
      const params = { bucketName: this.selectBucketName, dir: this.selectBucketDir || '' }
      this.knowledgeService.queryObsBucketsDir(params).then((res: any) => {
        this.isBucketDirLoading = false;
        if (res?.length) {
          this.obsBucketDirOptions = res;
        }
        this.cdr.detectChanges();
      }).catch(() => {
        this.isBucketDirLoading = false;
        this.cdr.detectChanges();
      });
    }
  }

  public onBeforeOpenObsBucket(isOpen: boolean) {
    if (!isOpen) return;
    this.isBucketLoading = true;
    this.knowledgeService.queryObsBucketName().then((res: any) => {
      this.isBucketLoading = false;
      if (res?.length) {
        this.obsBucketOptions = res;
      }
      this.cdr.detectChanges();
    }).catch(() => {
      this.isBucketLoading = false;
      this.cdr.detectChanges();
    });
  }

  backToLastLevel() {
    const pathStrArray = this.selectBucketDir.split('/');
    let arr = pathStrArray.slice(0, -2);
    this.selectBucketDir = arr.length ? `${arr.join('/')}/` : '';
    this.form.patchValue({obs_input_directory: this.selectBucketDir });
  }

  getExecuteRecords() {
    if (!this.obsConfigId) {
      return;
    }
    this.loading = true;
    let params = {
      limit: this.pageSize.size,
      offset: ((this.currentPage || 1) - 1) * this.pageSize.size,
    }
    this.knowledgeService.queryObsExecuteRecords(this.obsConfigId, this.kbId, params).then(res => {
      if (res?.data?.length) {
        this.srcData.data = res.data;
        this.totalNumber = res.total;
      } else {
        this.srcData.data = [];
        this.totalNumber = 0;
      }
      this.loading = false;
      this.cdr.detectChanges();
    }).catch(() => {
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  executeUploadFiles() {
    if (this.obsConfigId) {
      this.knowledgeService.executeObsFileUpload(this.kbId, this.obsConfigId).then(res => {
        this.currentPage = 1;
        this.pageSize.size = 10;
        this.nzMessage.success(this.i18n.transform('execute_trigger_success'));
        this.executeRes.emit();
        this.getExecuteRecords();
      })
    } else {
      this.saveConfig(true);
    }
  }

  toggleFormState() {
    if (this.obsConfigId) {
      this.form.get('obs_bucket_name')?.disable();
      this.form.get('obs_input_directory')?.disable();
    } else {
      this.form.get('obs_bucket_name')?.enable();
      this.form.get('obs_input_directory')?.enable();
    }
  }

  protected readonly changeUrl = cdnAssetUrl;
  protected readonly formatTime = formatTime;
}
