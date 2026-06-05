import { Component, Input } from '@angular/core';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

@Component({
  selector: 'meta-upload-file-icon',
  templateUrl: './upload-file-icon.component.html',
  styleUrls: ['./upload-file-icon.component.scss'],
  standalone: true,
})
export class UploadFileIconComponent {
  @Input() fileName: string;
  @Input() width: number = 16;
  @Input() height: number = 16;

  getFileSVG() {
    const name = this.fileName;
    const lastDotIndex = name.lastIndexOf('.');
    const fileType =
      lastDotIndex !== -1 ? name.substring(lastDotIndex + 1) : '';
    const fileTypes = {
      excel: [
        'xlsx',
        'xls',
        'xlsm',
        'xlsb',
        'xltx',
        'xltm',
        'csv',
        'tsv',
        'ods',
      ],
      doc: ['docx', 'doc', 'dotx', 'dotm', 'rtf', 'odt'],
      ppt: ['pptx', 'ppt', 'potx', 'potm', 'ppsx', 'pps', 'pptm', 'odp'],
      pic: ['png', 'jpeg', 'gif', 'webp', 'jpg', 'svg'],
      txt: ['txt'],
    };
    const type = Object.keys(fileTypes).find((key) =>
      fileTypes[key].includes(fileType),
    );
    if (type) {
      return cdnAssetUrl(`assets/agent/upload_${type}.svg`);
    } else {
      return cdnAssetUrl(`assets/agent/upload_others.svg`);
    }
  }
}
