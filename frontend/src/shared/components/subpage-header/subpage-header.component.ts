import { Location } from '@angular/common';
import {
  Component,
  Input,
  Output,
  EventEmitter,
  ViewChild,
  ElementRef,
  OnDestroy,
} from '@angular/core';
import { MODULES } from '@shared/modules';
@Component({
  selector: 'subpage-header',
  templateUrl: './subpage-header.component.html',
  standalone: true,
  imports: [MODULES],
})
export class SubpageHeaderComponent implements OnDestroy {
  @Input() title: string;

  @Input() showEdit: boolean;

  @Input() task_id: string;

  @Output() clickBack = new EventEmitter<void>();

  @Output() titleChange = new EventEmitter<string>();

  @Output() taskUpdateSuccess = new EventEmitter<void>();

  @ViewChild('titleInput') titleInput: ElementRef<HTMLInputElement>;

  public isEditing = false;

  private focusTimer: number | null = null;

  constructor(private location: Location) {}

  public ngOnDestroy(): void {
    if (Number.isInteger(this.focusTimer)) {
      window.clearTimeout(this.focusTimer);
    }
  }

  public onClickBack(): void {
    /** 如果没有提供点击返回的接收函数, 默认返回到上一级 */
    if (!this.clickBack.observed) {
      this.location.back();
    }

    this.clickBack.emit();
  }

  public onTitleChanged(): void {
    this.isEditing = false;
    this.showEdit = true;
    this.titleChange.emit(this.title);
  }
}
