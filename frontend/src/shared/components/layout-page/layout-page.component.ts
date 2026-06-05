import { Component, DOCUMENT, Inject, Input, OnDestroy, OnInit } from '@angular/core';

import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { SetSidebarVisibilityService } from '@shared/services/set-sidebar-visibility.service';

@Component({
  selector: 'layout-page',
  standalone: true,
  imports: [NzLayoutModule],
  templateUrl: './layout-page.component.html',
  styleUrl: './layout-page.component.scss'
})
export class LayoutPageComponent implements OnInit, OnDestroy {
  @Input() showContentHeader = true;
  @Input() showLeftbar = false;
  @Input() showPurchase = false;
  @Input() showTpLayout = true;
  @Input() responsive = true; // 响应式
  @Input() showBackgroud = true; // 是否展示背景色
  @Input() newStyle = false;
  @Input() isCreate = false;
  mainId = '';
  private cfMinWidth = '--cf-main-width-min';
  // 用于存储初始值
  private originalValue: string | null = null;

  constructor(
    private visibilityService: SetSidebarVisibilityService,
    @Inject(DOCUMENT) private document: Document
  ) {}

  ngOnInit(): void {
    if (this.newStyle) {
      this.originalValue = getComputedStyle(this.document.documentElement).getPropertyValue(this.cfMinWidth).trim();
      if (this.isCreate) {
        this.mainId = 'customCreate';
        document.documentElement.style.setProperty(this.cfMinWidth, `${1316 + 96 * 2}px`, 'important');
      } else {
        this.mainId = 'customDetail';
        document.documentElement.style.setProperty(this.cfMinWidth, `${1692 + 96 * 2}px`, 'important');
      }
    }
    this.visibilityService.setSidebarsVisibilityByState(null);
  }
  
  ngOnDestroy(): void {
    this.visibilityService.setSidebarsVisibilityByState('destroy');
    if (this.originalValue !== null && this.originalValue !== '') {
      document.documentElement.style.setProperty(this.cfMinWidth, this.originalValue, 'important');
    }
  }
}
