import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { I18nNamespace } from '@i18n';
import { KbConstantConfigKey } from '@routes/knowledge-center/knowledge-base.interface';
import { FILE_UPLOAD_MAXIMUM_NUMBER } from '@routes/knowledge-center/knowledge.const';
import { TagSelectComponent } from '@routes/knowledge-center/components/tag-select/tag-select.component';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { KbAbilitiesService } from '@services/knowledge-center/kb-abilities.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { cloneDeep } from 'lodash';
import { BytesPipe } from 'src/pipes/bytes.pipe';
import { CommonUtils } from '../../../utils/common.util';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzMessageService } from 'ng-zorro-antd/message';
import { CommonModule } from '@angular/common';

export interface DescriptionOperator {
  type: 'text' | 'link' | 'button';
  content: string;
  link?: string;
  click?: () => void;
}

@Component({
  selector: 'knowledge-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.less'],
  standalone: true,
  imports: [MODULES, BytesPipe, TagSelectComponent, CommonModule, NzTableModule, NzButtonModule, NzIconModule, NzAlertModule, NzToolTipModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.KNOWLEDGE, I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class FileUploadComponent implements OnInit {
  @Input() modalTitle = 'upload_file';

  @Input() kbId = '';

  @Input() accept: string =
    '.doc, .docx, .pdf, .pptx, .ppt, .xlsx, .xls, .csv, .wps, .png, .jpg, .jpeg, .bmp, .gif, .tiff, .tif, .webp, .pcx, .ico, .psd, .dps, .et, .txt, .ofd, .md, .html, .mhtml';

  @Input() description: DescriptionOperator[] = [];

  @Input() isSupTag: boolean = false;

  @Output('cancelForm') cancelForm = new EventEmitter();
  @Output('goToTagManage') goToTagManage = new EventEmitter();
  @Output('submitForm') submitForm = new EventEmitter();

  @Input() outputs: { cancelForm?: () => void; goToTagManage?: () => void; submitForm?: (data: any) => void };

  @ViewChild('fileInput') fileInput: ElementRef<HTMLInputElement> | null = null;

  public fileList: any[] = [];

  public get isEmpty() {
    return 0 === this.fileList.length;
  }

  public isUploadLoading = false;

  public max_size = 0;

  public remainSize = 0;

  uploadLimit = 20;

  tagSelected = [];

  currentParams;

  isNeedCapacityLimit = false;

  kbManagementUrl = '';
  isFromDevelopSpace = false;

  constructor(
    private kbRepoServ: KnowledgeRepoService,
    private i18n: I18NextEagerPipe,
    private kbAbilitiesService: KbAbilitiesService,
    private router: Router,
    private nzMessage: NzMessageService
  ) {
    if (window.location && window.location.href && window.location.href.includes('develop-space')) {
      this.isFromDevelopSpace = true;
    }
  }

  public get available_size() {
    const uploadSize = this.fileList.reduce((res, data) => {
      const { size } = data.file;
      res += size;
      return res;
    }, 0);
    return this.remainSize - uploadSize;
  }

  get maxUploadSize() {
    return this.uploadLimit * 1024 * 1024;
  }

  get confirmTip() {
    if (this.isOverSize) {
      return this.i18n.transform('file_upload_confirm_over_size');
    }
    if (this.isEmpty) {
      return this.i18n.transform('file_upload_confirm_no_file');
    }
    return '';
  }

  public get isOverSize() {
    if (!this.isNeedCapacityLimit) {
      return false;
    }
    return this.available_size < 0;
  }

  ngOnInit(): void {
    this.setKbUrl();
    this.getSize();
    this.getUploadConfig();
  }

  goToTag() {
    this.goToTagManage.emit();
  }

  setKbUrl() {
    const prefixUrl = window.location.href?.split('/home')[0];
    const tagUrl = this.router
      .createUrlTree(['/home/knowledge-center/knowledge', this.kbId], {
        queryParams: {
          active: 'kb_tag_management',
          previousUrl: '/home/agent-center/library-home?tabId=knowledge',
        },
      })
      .toString();
    this.kbManagementUrl = `${prefixUrl}${tagUrl}`;
  }

  getUploadConfig() {
    this.kbAbilitiesService.getKbConfigWithCache(this.kbId).subscribe(res => {
      const maxSizeConfig = res?.configs?.find(item => item.key === KbConstantConfigKey.FILE_SIZE);
      const isNeedCapacityLimit = res?.configs?.find(item => item.key === KbConstantConfigKey.OBS_CAPACITY);

      if (maxSizeConfig) {
        this.uploadLimit = maxSizeConfig.value;
      }
      this.isNeedCapacityLimit = isNeedCapacityLimit?.value !== -1;
    });
  }

  public async getSize() {
    try {
      if (this.kbId) {
        const { remain_size, max_size } = await this.kbRepoServ.retrieveKb(this.kbId);
        this.max_size = max_size || 0;
        this.remainSize = remain_size || 0;
      }
    } finally {
    }
  }

  public handleFileSelect() {
    if (this.fileInput) {
      this.fileInput.nativeElement.click();
    }
  }

  public handleFileUpload(e: Event) {
    const target = e.target as HTMLInputElement;
    const files = target.files as FileList;
    if ((files?.length ?? 0) + (this.fileList?.length ?? 0) > FILE_UPLOAD_MAXIMUM_NUMBER) {
      this.fileInput.nativeElement.value = '';
      this.nzMessage.error(this.i18n.transform('file_upload_maximum_number', { quantities: FILE_UPLOAD_MAXIMUM_NUMBER }));
      return;
    }
    const newFileList = Array.from(files).reduce((res, file) => {
      const { name, size } = file;
      if (size > this.maxUploadSize) {
        this.nzMessage.error(this.i18n.transform('file_oversize', { name }));
        return res;
      }
      if (!CommonUtils.isValidExt(this.accept, file)) {
        this.nzMessage.error(this.i18n.transform('file_wrong_type', { name }));
        return res;
      }
      res.push({
        file,
        tagSelected: cloneDeep(this.tagSelected),
      });
      return res;
    }, []);
    this.fileList = [...this.fileList, ...newFileList];
    this.fileInput.nativeElement.value = '';
  }

  public deleteFile(i: number) {
    this.fileList.splice(i, 1);
    this.fileList = [...this.fileList]; // Update reference for change detection
  }

  public handleUploadCancel() {
    this.cancelForm.emit();
  }

  public setConfirmLoading(v: boolean) {
    this.isUploadLoading = v;
  }

  public handleUploadConfirm() {
    this.submitForm.emit({
      fileList: this.fileList,
      setConfirmLoading: this.setConfirmLoading.bind(this),
    });
  }
}
