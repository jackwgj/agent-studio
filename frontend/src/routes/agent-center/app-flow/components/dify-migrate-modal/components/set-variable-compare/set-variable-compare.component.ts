import {Component, Input} from '@angular/core';
import {CommonModule} from "@angular/common";

import {MODULES} from "@shared/modules";
import type {ISetVariableNode} from "@routes/agent-center/app-flow/node.type";
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from "angular-i18next";
import {I18nNamespace} from "@i18n";

@Component({
  selector: 'set-variable-compare',
  templateUrl: './set-variable-compare.component.html',
  styleUrls: ['../common-compare.less', './set-variable-compare.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class SetVariableCompareComponent {
  @Input() node: ISetVariableNode;

  constructor(
    private i18n: I18NextEagerPipe,
  ) { }

}
