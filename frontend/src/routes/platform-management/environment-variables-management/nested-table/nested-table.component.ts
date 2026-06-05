import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { SharedModule } from '@shared/shared.module';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'nested-table',
  templateUrl: './nested-table.component.html',
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    SharedModule,
    NzTableModule,
    NzTagModule,
    NzButtonModule,
    NzToolTipModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.COMMON,
        I18nNamespace.PROMPT_PLATFORM,
        I18nNamespace.AGENT_CENTER,
        I18nNamespace.PLATFORM_MANAGEMENT,
      ],
    },
  ],
})
export class NestedTableComponent implements OnInit {
  @Input() data: Array<any> = [];
  @Output() configVariable = new EventEmitter();

  srcData: any = {
    data: []
  };

  columns: Array<{ title: string }> = [];
  noDataTitle: string = '';
  noDataDescHtml: string = '';

  constructor(private i18n: I18NextEagerPipe) {}

  ngOnInit(): void {
    // 延迟初始化，保证 i18n 已加载
    this.columns = [
      { title: this.i18n.transform('variable_name') },
      { title: this.i18n.transform('variable_value') }
    ];
    this.noDataTitle = this.i18n.transform('no_variable_list_data');
    this.noDataDescHtml = this.i18n.transform('no_variable_list_data_tip');

    this.srcData.data = this.data;
  }

  onConfigVariable() {
    this.configVariable.emit();
  }

  protected readonly changeUrl = cdnAssetUrl;
}
