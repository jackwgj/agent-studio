import {
  Component,
  ElementRef,
  EventEmitter,
  Input, OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { CommonUtils } from '../../../../utils/common.util';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'upload-image',
  templateUrl: './upload-image.component.html',
  styleUrls: ['./upload-image.component.less'],
  standalone: true,
  imports: [MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.KNOWLEDGE,
        I18nNamespace.COMMON,
        I18nNamespace.AGENT_CENTER,
      ],
    },
  ],
})
export class UploadImageComponent implements OnInit, OnChanges {
  @Input() initialImage: string | null = null;
  @Input() accept: string = '.png,.jpeg,.gif,.jpg';
  @Input() isPermitDel = false;
  @Output() imageChanged = new EventEmitter<any | null>();
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  imagePreview: string | null = null;
  isUploading = false;
  mouseEnter = false;

  constructor(
    private i18n: I18NextEagerPipe,
    private message: NzMessageService,
  ) {}

  ngOnInit(): void {}

  ngOnChanges(changes: SimpleChanges) {
    if(changes.initialImage?.currentValue) {
      this.imagePreview = changes.initialImage?.currentValue;
    }
  }

  // 触发文件选择
  triggerFileInput(): void {
    this.fileInput.nativeElement.click();
  }

  // 处理文件选择
  async handleFileChange(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      input.value = '';
      return;
    }
    // 验证文件大小
    if (file.size > 204800) {
      input.value = '';
      this.message.error(this.i18n.transform('file_size_exceeds_limit'));
      return;
    }

    if (!CommonUtils.isValidExt(this.accept, file)) {
      input.value = '';
      this.message.error(this.i18n.transform('file_wrong_type', { name: file.name }));
      return;
    }

    this.isUploading = true;

    try {
      // 读取文件为base64
      const base64 = await this.readFileAsBase64(file);
      this.imagePreview = base64;

      // 触发更新事件，可以在这里处理上传到服务器的逻辑
      this.imageChanged.emit(file);
    } finally {
      this.isUploading = false;
      // 重置input，允许重复选择同一文件
      input.value = '';
    }
  }

  // 读取文件为base64
  readFileAsBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  // 删除图片
  handleDelete(): void {
    this.imagePreview = null;
    this.mouseEnter = false;
    this.imageChanged.emit(null);
  }

  protected readonly changeUrl = cdnAssetUrl;
}
