import { CommonModule } from '@angular/common';
import { Component, computed, Input, input } from '@angular/core';

import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';

import { I18nPipe } from '@agentcore/shared/pipes/i18n.pipe';

import { CustomHeaderParam } from './model';

@Component({
  selector: 'custom-header',
  standalone: true,
  templateUrl: './custom-header.component.html',
  styleUrl: './custom-header.component.scss',
  imports: [CommonModule, NzTagModule, NzButtonModule, NzDropDownModule, NzIconModule, NzBreadCrumbModule, I18nPipe],
})
export class CustomHeaderComponent {
  @Input() responsive = false;
  params = input.required<CustomHeaderParam>();

  data = computed(() => this.params());

  onSelect(item: any): void {
    item?.click?.();
  }

  onCrumbClick(item: any): void {
    item?.clickFn?.();
  }

  trackByIndex(index: number): number {
    return index;
  }
}
