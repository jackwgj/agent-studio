import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

import { MODULES } from '@shared/modules';
import { MigrateCompareRowComponent } from '../migrate-compare-row/migrate-compare-row.component';
import type { NodeInfo } from '@routes/agent-center/app-flow/node.type';
import {I18NextEagerPipe} from "angular-i18next";
import {LLMSelectComponent} from "@routes/agent-center/app-flow/components/llm-select/llm-select.component";

@Component({
  selector: 'not-compatible-node',
  templateUrl: './not-compatible-node.component.html',
  styleUrls: ['../common-compare.less', './not-compatible-node.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    MigrateCompareRowComponent,
    LLMSelectComponent
  ],
})
export class NotCompatibleNodeComponent {
  @Input() node: NodeInfo;

  public defaultEmptyTip = this.i18n.transform('default_empty_tip');

  constructor(
    private i18n: I18NextEagerPipe,
  ) { }
}
