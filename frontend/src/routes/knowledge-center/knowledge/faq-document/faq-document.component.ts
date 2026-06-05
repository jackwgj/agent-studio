import { Component, Input, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { I18nNamespace } from '@i18n';
import { Network } from '@model';
import { FileUploadComponent } from '@routes/knowledge-center/file-upload/file-upload.component';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { BehaviorSubject, Subject, catchError, expand, from, map, of, switchMap, takeUntil, takeWhile, timer } from 'rxjs';
import { BytesPipe } from 'src/pipes/bytes.pipe';
import { FormateTimePipe } from 'src/pipes/formate-time.pipe';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import { CommonUtils } from '../../../../utils/common.util';
import { BatchDeleteComponent } from '../batch-delete.component';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NzTableModule, NzTableQueryParams } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzModalService, NzModalModule } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzDrawerService, NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzDividerModule } from 'ng-zorro-antd/divider';

@Component({
  selector: 'knowledge-faq-document',
  templateUrl: './faq-document.component.html',
  styleUrls: ['./faq-document.component.less'],
  standalone: true,
  imports: [
    MODULES,
    BytesPipe,
    BatchDeleteComponent,
    FormsModule,
    RouterModule,
    NzTableModule,
    NzButtonModule,
    NzInputModule,
    NzSelectModule,
    NzBadgeModule,
    NzTypographyModule,
    NzIconModule,
    NzEmptyModule,
    NzModalModule,
    NzDrawerModule,
    NzDividerModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.KNOWLEDGE, I18nNamespace.AGENT_CENTER],
    },
    FormateTimePipe,
    NzModalService,
    NzMessageService,
    NzDrawerService
  ],
})
export class FAQDocumentComponent implements OnInit, OnDestroy {
  @Input() kbId!: string;
  @Input() isMarket = false;

  @ViewChild('batchDeleteModal') batchDeleteModal: BatchDeleteComponent;

  tableData: any[] = [];
  loading = false;
  total = 0;
  pageIndex = 1;
  pageSize = 10;
  searchName = '';
  searchStatus: string | null = null;
  setOfCheckedId = new Set<string>();
  allChecked = false;
  indeterminate = false;
  emptyImage = `${Network.ASSETS_PATH}images/noData.svg`;
  private destroy$ = new Subject<void>();
  private loadTrigger$ = new BehaviorSubject<void>(undefined);

  constructor(
    private kbRepoServ: KnowledgeRepoService,
    private nzModal: NzModalService,
    private nzMessage: NzMessageService,
    private nzDrawerService: NzDrawerService,
    private i18n: I18NextEagerPipe,
    public kbAbilitiesService: KbAbilitiesService,
    private formateTimePipe: FormateTimePipe
  ) {}

  ngOnInit() {
    this.setupDataPolling();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupDataPolling() {
    this.loadTrigger$.pipe(
      takeUntil(this.destroy$),
      switchMap(() => this.fetchAndPollFiles())
    ).subscribe(res => {
      this.tableData = res.data;
      this.total = res.total;
      this.loading = false;
      this.refreshCheckedStatus();
    });
  }

  private fetchAndPollFiles() {
    this.loading = true;
    return this.getFiles().pipe(
      expand((val: any) => {
        const hasProcessing = val.file_info_list.some((file: any) => ['PENDING', 'RUNNING'].includes(file.file_status.toUpperCase()));
        if (hasProcessing) {
          return timer(15_000).pipe(switchMap(() => this.getFiles()));
        }
        return of(null);
      }),
      takeWhile(val => val !== null),
      map((val: any) => {
        val?.file_info_list?.forEach((item: any) => {
          item.create_time = this.formateTimePipe.transform(item.create_time);
        });
        return {
          total: val?.count ?? 0,
          data: val?.file_info_list ?? [],
        };
      }),
      catchError(() => {
        return of({
          total: 0,
          data: [],
        });
      })
    );
  }

  public getFiles() {
    const query = {
      offset: (this.pageIndex - 1) * this.pageSize,
      limit: this.pageSize,
    };

    return from(
      this.kbRepoServ.listFaqFiles(this.kbId, query, {
        file_name: this.searchName || undefined,
        file_status: this.searchStatus || undefined,
      })
    );
  }

  public onQueryParamsChange(params: NzTableQueryParams) {
    this.pageIndex = params.pageIndex;
    this.pageSize = params.pageSize;
    this.loadTrigger$.next();
  }

  public onSearch() {
    this.pageIndex = 1;
    this.loadTrigger$.next();
  }

  public openUploadFileHalfmodal() {
    const drawerRef = this.nzDrawerService.create({
      nzContent: FileUploadComponent,
      nzWidth: '700px',
      nzMaskClosable: true,
      nzContentParams: {
        kbId: this.kbId,
        description: [
          { type: 'text', content: 'faq_upload_tip' }
        ],
        accept: '.xlsx, .xls, .docx, .doc',
        outputs: {
          cancelForm: () => drawerRef.close(),
        },
      }
    });

    drawerRef.afterOpen.subscribe(() => {
      const instance = drawerRef.getContentComponent();
      if (instance) {
        instance.submitForm.subscribe(async ({ fileList, setConfirmLoading }: any) => {
          try {
            setConfirmLoading(true);
            const promises = fileList.map(async (file: any) => {
              try {
                const formData = new FormData();
                formData.append('file', file.file);
                return await this.kbRepoServ.uploadFaqFile(this.kbId, formData);
              } catch (error: any) {
                if (error?.data?.error_code) {
                  this.nzMessage.error(
                    this.i18n.transform('file_upload_error', {
                      errorMsg: error.data.error_msg,
                      fileName: file.name,
                    }),
                    { nzDuration: 3000 }
                  );
                }
                return null;
              }
            });

            const res = await Promise.all(promises);
            if (!res.includes(null)) {
              this.nzMessage.success(this.i18n.transform('upload_success'));
            }
            if (res.some(r => r !== null)) {
              this.loadTrigger$.next();
            }
            drawerRef.close();
          } finally {
            setConfirmLoading(false);
          }
        });

        if (instance.cancelForm) {
          instance.cancelForm.subscribe(() => drawerRef.close());
        }
      }
    });
  }

  public operationDisableFn(): boolean {
    return this.setOfCheckedId.size === 0 || this.kbAbilitiesService.isResourceDisabled;
  }

  public onBatchDelete() {
    const selectedRows = this.tableData.filter(d => this.setOfCheckedId.has(d.file_id));
    this.batchDeleteModal.openModal(
      [
        { headerName: this.i18n.transform('faq_file'), field: 'file_name' },
        { headerName: this.i18n.transform('status'), field: '_statusText' }
      ],
      selectedRows.map(row => ({ ...row, _statusText: this.getStatusText(row.file_status) }))
    );
  }

  public async handleBatchDelete(ctx: any) {
    try {
      const selectedRows = ctx.tableData || [];
      const promises = selectedRows.map(async ({ file_id }: any) => {
        try {
          await this.kbRepoServ.deleteFaqFile(this.kbId, file_id);
          return true;
        } catch (error) {
          return false;
        }
      });
      const res = await Promise.all(promises);
      const successCount = res.filter(item => item).length;

      this.nzMessage.success(
        this.i18n.transform('batch_delete_success', {
          totalCount: res.length,
          deletedCount: successCount,
        })
      );
      this.setOfCheckedId.clear();
      this.loadTrigger$.next();
      this.batchDeleteModal.closeModal();
    } finally {
      ctx.confirmLoading = false;
    }
  }

  public onDelete(data: any) {
    if (this.kbAbilitiesService.isResourceDisabled) return;

    this.nzModal.confirm({
      nzTitle: this.i18n.transform('faq_file_delete_title'),
      nzContent: this.i18n.transform('faq_file_delete_content', { fileName: data.file_name }),
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOkText: this.i18n.transform('delete'),
      nzOnOk: async () => {
        try {
          await this.kbRepoServ.deleteFaqFile(this.kbId, data.file_id);
          this.nzMessage.success(this.i18n.transform('delete_success'));
          this.loadTrigger$.next();
          return true;
        } catch (error) {
          return false;
        }
      }
    });
  }

  public onDownload(data: any) {
    if (data.file_status !== 'SUCCESS' || this.kbAbilitiesService.isResourceDisabled) return;

    this.kbRepoServ.downloadFaqFile(this.kbId, data.file_id).then(res => {
      CommonUtils.downloadFile(res as Blob, data.file_name, true);
    });
  }

  onAllChecked(checked: boolean): void {
    this.tableData.forEach(({ file_id }) => this.updateCheckedSet(file_id, checked));
    this.refreshCheckedStatus();
  }

  onItemChecked(id: string, checked: boolean): void {
    this.updateCheckedSet(id, checked);
    this.refreshCheckedStatus();
  }

  private updateCheckedSet(id: string, checked: boolean): void {
    if (checked) {
      this.setOfCheckedId.add(id);
    } else {
      this.setOfCheckedId.delete(id);
    }
  }

  private refreshCheckedStatus(): void {
    this.allChecked = this.tableData.length > 0 && this.tableData.every(({ file_id }) => this.setOfCheckedId.has(file_id));
    this.indeterminate = this.tableData.some(({ file_id }) => this.setOfCheckedId.has(file_id)) && !this.allChecked;
  }

  public getFileType(fileName: string): string {
    return fileName?.split('.').slice(-1)[0] || '--';
  }

  public getStatusText(status: string): string {
    const map: Record<string, string> = {
      'PENDING': this.i18n.transform('pending'),
      'RUNNING': this.i18n.transform('running'),
      'SUCCESS': this.i18n.transform('success'),
      'ERROR': this.i18n.transform('error'),
      'IMPORT_EXCEPTION': this.i18n.transform('import_exception'),
      'FILE_ENCODING_ERROR': this.i18n.transform('file_encoding_error')
    };
    return map[status?.toUpperCase()] || status;
  }

  public getStatusIcon(status: string): any {
    const map: Record<string, string> = {
      'PENDING': 'default',
      'RUNNING': 'processing',
      'SUCCESS': 'success',
      'ERROR': 'error',
      'IMPORT_EXCEPTION': 'error',
      'FILE_ENCODING_ERROR': 'error'
    };
    return map[status?.toUpperCase()] || 'default';
  }
}
