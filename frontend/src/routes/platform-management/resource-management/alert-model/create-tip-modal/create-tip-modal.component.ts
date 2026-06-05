import {
  Component,
  Input,
  OnInit,
  ViewEncapsulation,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

@Component({
  selector: 'resource-create-tip-modal',
  templateUrl: './create-tip-modal.component.html',
  styleUrls: ['./create-tip-modal.component.less'],
  styles: ['.develop_space_create_model { width: 550px !important; }'],
  encapsulation: ViewEncapsulation.None,
  standalone: true,
  imports: [
    MODULES,
    CommonModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PLATFORM_MANAGEMENT, I18nNamespace.COMMON],
    },
  ],
})
export class CreateTipModalComponent implements OnInit {
  @Input() tableList = [];
  constructor() {}
  ngOnInit() {}
  onActiveChange(isActive: boolean, item: any): void {}
  getIcon(url: string) {
    if (url.startsWith('data:image/')) {
      return url;
    } else {
      return cdnAssetUrl(url);
    }
  }
  goToResource(type:string){
    this.close()
  }

  dismiss() {}
  close() {}
}
