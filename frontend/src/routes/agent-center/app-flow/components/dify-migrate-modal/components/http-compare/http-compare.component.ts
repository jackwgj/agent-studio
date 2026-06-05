import {Component, Input} from '@angular/core';
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from "angular-i18next";
import {NzAlertModule} from "ng-zorro-antd/alert";

import {CommonModule} from "@angular/common";
import {MODULES} from "@shared/modules";
import {I18nNamespace} from "@i18n";
import type {IHttpRepo} from "@routes/agent-center/app-flow/node.type";

@Component({
  selector: 'http-compare',
  templateUrl: './http-compare.component.html',
  styleUrls: ['../common-compare.less', './http-compare.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    NzAlertModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class HttpCompareComponent {
  @Input() node: IHttpRepo;

  constructor(
    private i18n: I18NextEagerPipe,
  ) { }


}
