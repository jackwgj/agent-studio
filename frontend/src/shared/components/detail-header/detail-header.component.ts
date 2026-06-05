import {
  Component,
  Input,
  Output,
  EventEmitter,
  ContentChild,
  TemplateRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {MODULES} from "@shared/modules";

@Component({
  selector: 'detail-header',
  templateUrl: './detail-header.component.html',
  styleUrls: ['./detail-header.component.less'],
  standalone: true,
  imports: [CommonModule,MODULES],
})
export class DetailHeaderComponent {
  // 左边操作区
  @ContentChild('opLeft') opLeftTemplate: TemplateRef<any>;

  // 右边操作区
  @ContentChild('opRight') opRightTemplate: TemplateRef<any>;

  // 右边操作区
  @ContentChild('statusBar') statusBarTemplate: TemplateRef<any>;

  @Input() icon!: string;

  @Input() title!: string;

  @Input() loading = false;

  @Input() showDivider = false;

  @Input() showLeftDivider = false;

  @Output() backClick = new EventEmitter<void>();

  public onClickBack(): void {
    this.backClick.emit();
  }
}
