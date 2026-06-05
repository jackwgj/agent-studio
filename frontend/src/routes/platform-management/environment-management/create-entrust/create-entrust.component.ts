import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { SharedModule } from '@shared/shared.module';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'create-entrust',
  templateUrl: './create-entrust.component.html',
  styleUrls: ['./create-entrust.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    SharedModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.PLATFORM_MANAGEMENT, I18nNamespace.COMMON, I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class CreateEntrustComponent implements OnInit {
  @Input() title = this.i18n.transform('create_delegate')
  @Input() data: any;
  @Input() type ='create'
  displayedData: any[] = [];
  srcData: { data: any[]; state: undefined } = {
    data: [],
    state: undefined,
  };
  listColumns: any[] = [
    { title: this.i18n.transform('service_name')},
    { title: this.i18n.transform('service_desc')},
  ];
  constructor(private i18n: I18NextEagerPipe) {}
  ngOnInit() {
    if (this.data?.length > 0) {
      this.srcData.data = this.data
    } else {
      this.srcData.data = [];
    }

  }

  public trackByFn(index: number, item: any): number {
    return item?.id;
  }
  close(): void {}
  dismiss(): void {}
}
