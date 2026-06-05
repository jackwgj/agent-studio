import {
  Component,
  ElementRef,
  Input,
  OnDestroy,
  ViewChild,
  forwardRef,
} from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { I18nNamespace } from '@i18n';
import { KB_DEFAULT_ICON_BASE64_STR } from '@routes/agent-center/app-knowledge/create-knowledge/default-icon-base64';
import { KnowledgeRepoService } from '@services/agent-center/knowledge.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { CommonUtils } from '../../../utils/common.util';
import { cdnAssetUrl } from '../../../single-spa/assets-url';
import { NzMessageService } from 'ng-zorro-antd/message';

enum mapKeys {
  VALUE= 'value',
}

@Component({
  selector: 'icon-upload',
  template: `
    <img
      [src]="previewUrl || value || defaultIcon"
      alt="logo"
      class="h-full w-full object-cover object-center"
    />
    <div class="upload-mask" (click)="uploadIcon()">
      <input
        type="file"
        #iconInput
        class="hidden"
        id="upload_file"
        [accept]="accept"
        (change)="onUploadIcon($event)"
      />
      <div *ngIf="maskIcon" class="upload-icon !text-[12px]">
        {{ maskIcon }}
      </div>
      <img *ngIf="!maskIcon" [src]="changeUrl('assets/knowledge-center/editKb.svg')" class="upload-icon" alt=""/>
    </div>
  `,
  styles: [
    `
      :host {
        position: relative;
        display: block;
        cursor: pointer;
        width: 60px;
        height: 60px;
        border-radius: 8px;
        overflow: hidden;
        &:hover {
          .upload-mask {
            display: block;
          }
        }
      }
      .upload-mask {
        display: none;
        width: 100%;
        height: 100%;
        position: absolute;
        left: 0;
        top: 0;
        background-color: #000000;
        opacity: 0.5;
        cursor: pointer;
      }
      .upload-icon {
        font-size: 20px;
        color: #fff;
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-40%, -46%);
      }
    `,
  ],
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.AGENT,
      ],
    },
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => IconUploadComponent),
      multi: true,
    },
  ],
})
export class IconUploadComponent implements OnDestroy {
  @Input() defaultIcon = KB_DEFAULT_ICON_BASE64_STR;

  @Input() accept = '.png,.jpeg,.gif,.jpg';

  @Input() maskIcon = '';

  @Input() upload_size = 1024*1024*2

  @Input() err_msg = this.i18n.transform('icon_upload_limit')

  @ViewChild('iconInput') iconInput: ElementRef<HTMLInputElement> | null = null;

  public value = '';

  public uploading = false;

  public uploadUrl = '';

  public previewUrl: SafeUrl;

  constructor(
    private i18n: I18NextEagerPipe,
    private knowledgeRepoServ: KnowledgeRepoService,
    private sanitizer: DomSanitizer,
    private message: NzMessageService,
  ) {}

  ngOnDestroy(): void {
    this.uploadUrl && URL.revokeObjectURL(this.uploadUrl);
  }

  private onChange: (value: string) => void = () => {};

  private onTouched: () => void = () => {};

  public writeValue(value: string): void {
    this.value = value;
    this.uploadUrl && URL.revokeObjectURL(this.uploadUrl);
    this.previewUrl = '';
  }

  public registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  public registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  public emit(value: string) {
    this.value = value;
    this.onChange(value);
    this.onTouched();
  }

  public uploadIcon() {
    if (this.uploading) {
      return;
    }
    this.iconInput.nativeElement.click();
  }
  public clearFile() {
    const fileInput= document.getElementById('upload_file');
    fileInput[mapKeys.VALUE] = ''; // 将文件输入框的值清空
  }

  public async onUploadIcon(e: Event) {

    const file: File = (e.target as HTMLInputElement).files[0];
    if (!file) {
      return;
    }

    if (file?.size > this.upload_size) {
      this.message.error(this.err_msg);
      this.clearFile();
      return;
    }

    if (!CommonUtils.isValidExt(this.accept, file)) {
      this.message.error(this.i18n.transform('file_wrong_type', { name: file.name }));
      this.clearFile();
      return;
    }

    try {
      this.uploading = true;
      const params = new FormData();
      params.append('file', file);
      const { file_name } = await this.knowledgeRepoServ.uploadAvator(params);
      this.uploadUrl = URL.createObjectURL(file);
      this.previewUrl = this.sanitizer.bypassSecurityTrustUrl(this.uploadUrl);
      this.emit(file_name);
      this.iconInput.nativeElement.value = '';
    } finally {
      this.uploading = false;
    }
  }

  protected readonly changeUrl = cdnAssetUrl;
}
