import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { MODULES } from '@shared/modules';

@Component({
  selector: 'migrate-compare-row',
  templateUrl: './migrate-compare-row.component.html',
  styleUrls: ['../common-compare.less', './migrate-compare-row.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    NzIconModule,
  ],
})
export class MigrateCompareRowComponent {

}
