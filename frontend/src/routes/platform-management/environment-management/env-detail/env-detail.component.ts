import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { SharedModule } from '@shared/shared.module';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { formatTime } from 'src/utils/commonFn';
import { environmentStatusMap } from '@routes/platform-management/environment-management/env-management.map';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NZ_DRAWER_DATA } from 'ng-zorro-antd/drawer';

@Component({
  selector: 'env-detail',
  templateUrl: './env-detail.component.html',
  styleUrls: ['./env-detail.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    SharedModule,
    NzDescriptionsModule,
    NzTagModule,
    NzTypographyModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.PLATFORM_MANAGEMENT,
        I18nNamespace.COMMON,
        I18nNamespace.TRACE,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.AGENT
      ],
    },
  ],
})
export class EnvDetailComponent implements OnInit {
  @Input() data: any;
  @Input() context: any;

  readonly drawerData = inject(NZ_DRAWER_DATA, { optional: true });

  constructor() {}

  ngOnInit() {
    if (this.drawerData?.context?.data) {
      this.data = this.drawerData.context.data;
    } else if (this.context?.data) {
      this.data = this.context.data;
    }
  }

  protected readonly formatTime = formatTime;
  protected readonly environmentStatusMap = environmentStatusMap;

  getNzTagColor(type: string | undefined): string {
    switch (type) {
      case 'success':
      case 'green':
        return 'success';
      case 'error':
      case 'danger':
      case 'red':
        return 'error';
      case 'warning':
      case 'yellow':
        return 'warning';
      case 'primary':
      case 'blue':
      case 'info':
        return 'processing';
      default:
        return 'default';
    }
  }
}
