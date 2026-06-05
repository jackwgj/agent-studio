import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';

import { COMMON_MODULES, MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';

import { HttpClientModule } from '@angular/common/http';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'app-common-no-data',
  templateUrl: './common-no-data.component.html',
  styleUrls: ['./common-no-data.component.less'],
  imports: [COMMON_MODULES, MODULES, HttpClientModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PROMPT_PLATFORM],
    },
  ],
  standalone: true,
})
export class CommonNoDataComponent implements OnInit {
  ngOnInit(): void {}
}
