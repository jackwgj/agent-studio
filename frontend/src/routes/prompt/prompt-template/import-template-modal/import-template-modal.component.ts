import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MODULES } from '@shared/modules';
import { PromptService } from '@services/prompt.service';
import { CommonUtils } from '../../../../utils/common.util';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { MessageComponent } from '@shared/services/cfdata.service';
import * as angularI18next from 'angular-i18next';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import i18next from 'i18next';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzUploadChangeParam, NzUploadModule, NzUploadFile } from 'ng-zorro-antd/upload';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'meta-import-template-modal',
  standalone: true,
  imports: [NzButtonModule, NzUploadModule, NzModalModule, MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.COMMON, I18nNamespace.PROMPT_PLATFORM],
    },
  ],
  templateUrl: './import-template-modal.component.html',
  styleUrl: './import-template-modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImportTemplateModalComponent {
  public selectedFile: NzUploadFile;
  public maxSize = 10 * 1024 * 1024;
  public uploadType = ['.xls', '.xlsx'];
  constructor(
    private service: PromptService,
    private readonly i18n: angularI18next.I18NextEagerPipe,
    private message: NzMessageService
  ) {}

  dismiss(): void {}

  close(): void {}

  onAddFileSuccess(info: NzUploadChangeParam) {
    if (info.file.status === 'done') {
      if (this.selectedFile) {
        this.selectedFile?.uploader?.removeItems([this.selectedFile]);
      }

      this.selectedFile = info.file;
    } else if (info.file.status === 'removed') {
      this.onRemoveItems();
    } else if (info.file.response?.error) {
      this.onAddItemFailed({ validResults: ['type'] });
    }
  }

  public onAddItemFailed(info: { validResults: string[] }) {
    const error_type = info.validResults[0];
    if (error_type === 'type') {
      this.message.error(this.i18n.transform('only_support_upload_format', { types: this.uploadType.join(',') }));
    }
    if (error_type === 'maxSize') {
      this.message.error(this.i18n.transform('file_size_cannot_exceed', { size: this.maxSize / (1024 * 1024) }));
    }
  }

  onRemoveItems() {
    if (this.selectedFile) {
      this.selectedFile = undefined;
    }
  }

  confirm() {
    const formData = new FormData();
    formData.append('file', this.selectedFile._file);
    this.service.importPromptTmplV2(formData).subscribe(res => {
      if (res.length === 0) {
        this.close();
      } else {
        MessageComponent.showError(res.join(','));
        this.dismiss();
      }
    });
  }

  downloadTemplate() {
    this.service.downloadImportTmplCase().subscribe(res => {
      CommonUtils.downloadFile(
        new Blob([res.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        }),
        res.headers.get('Content-Disposition')
      );
    });
  }

  protected readonly changeUrl = cdnAssetUrl;
  protected readonly i18next = i18next;
}
