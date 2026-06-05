import { CommonModule } from '@angular/common';
import {
  Component,
  ContentChild,
  EventEmitter,
  Input,
  Output,
  TemplateRef,
} from '@angular/core';
import {MODULES} from "@shared/modules";

@Component({
  selector: 'common-create-header',
  templateUrl: './create-header.component.html',
  styleUrls: ['./create-header.component.less'],
  standalone: true,
  imports: [CommonModule, MODULES],
})
export class CreateHeaderComponent {
  // 左边操作区
  @ContentChild('op') opTemplate: TemplateRef<any>;

  @Input() title!: string;

  @Output() backClick = new EventEmitter<void>();

  public onClickBack(): void {
    this.backClick.emit();
  }
}
