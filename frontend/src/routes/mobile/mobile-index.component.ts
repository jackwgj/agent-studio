import {
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { cdnAssetUrl } from 'src/single-spa/assets-url';

@Component({
  selector: 'mobile-index',
  templateUrl: './mobile-index.component.html',
  styleUrls: ['./mobile-index.component.less'],
  standalone: true,
  imports: [CommonModule, MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MOBILE, I18nNamespace.COMMON],
    },
  ],
})
export class MobileIndexComponent implements OnInit, OnDestroy {
  public user_guide; //用户指南
  public best_practice; //最佳实践
  public mobile_list;

  constructor(
    private i18n: I18NextEagerPipe,
  ) {
  }

  ngAfterViewInit() {
  }

  async ngOnInit() {
    this.mobile_list = [
      {
        icon: this.getIcon('assets/mobile-device/icon1-2X.svg'),
        title: this.i18n.transform('product_entry'),
        content:this.i18n.transform('product_entry_con'),
        url:`${location.protocol}//${location.host}${location.pathname}#/home/overview`
      },
      {
        icon: this.getIcon('assets/mobile-device/icon4-2X.svg'),
        title: this.i18n.transform('product_forum'),
        content: this.i18n.transform('product_forum_con'),
        url: '',
      },
      {
        icon: this.getIcon('assets/mobile-device/icon2-2X.svg'),
        title: this.i18n.transform('user_guide'),
        content: this.i18n.transform('user_guide_con'),
        url: this.user_guide,
      },
      {
        icon: this.getIcon('assets/mobile-device/icon3-2X.svg'),
        title: this.i18n.transform('best_practice'),
        content: this.i18n.transform('best_practice_con'),
        url: this.best_practice,
      },
    ];
  }

  ngOnDestroy(): void {}

  openFn(url: string) {
    const newWindow = window.open(url, '_blank');
    if (
      !newWindow ||
      newWindow.closed ||
      typeof newWindow.closed === 'undefined'
    ) {
      window.location.href = url; // 备选方案
    }
  }

  getIcon(url: string) {
    if (url.startsWith('data:image/')) {
      return url;
    } else {
      return cdnAssetUrl(url);
    }
  }

  dismiss() {}

  close() {}

  submit() {}

  protected readonly open = open;
}
