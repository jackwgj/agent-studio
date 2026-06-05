import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  forwardRef,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import {
  ControlValueAccessor,
  FormsModule,
  NG_VALUE_ACCESSOR,
} from '@angular/forms';
import { TextFieldModule } from '@angular/cdk/text-field';
import { CommonModule } from '@angular/common';
import { MODULES } from "@shared/modules";

@Component({
  selector: 'flexible-text-field',
  template: `
    <textarea
      #ftfTextarea
      *ngIf="isFocus"
      [spellcheck]="false"
      class="c-flexible-text-field"
      cdkTextareaAutosize
      [cdkAutosizeMinRows]="1"
      [cdkAutosizeMaxRows]="4"
      [(ngModel)]="value"
      (ngModelChange)="onInput()"
      (focus)="focus()"
      (blur)="blur()"
      [disabled]="disabled"
      [placeholder]="placeholder"
      [maxlength]="maxlength"
      nz-input
    >
    </textarea>
    <input
      *ngIf="!isFocus"
      [disabled]="disabled"
      nz-input
      [(ngModel)]="value"
      [title]="value"
      [placeholder]="placeholder"
      (focus)="focus()"
      (blur)="blur()"
      [maxlength]="maxlength"
      style="width: 100%"
    />
  `,
  styles: [
    `
      .c-flexible-text-field {
        width: calc(100% - 26px);
        resize: none;
        height: 18px;
        line-height: 18px;
        border: 1px solid #191919;
        color: #191919;
        padding: 4px 12px;
        border-radius: 6px;
        font-size: 12px;
        box-sizing: content-box;

        &:focus-visible {
          outline: none;
        }
      }
    `,
  ],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FlexibleTextFieldComponent),
      multi: true,
    },
  ],
  standalone: true,
  imports: [TextFieldModule, CommonModule, FormsModule, MODULES],
})
export class FlexibleTextFieldComponent
  implements ControlValueAccessor, OnInit
{
  @Input('placeholder') placeholder = '';

  @Input('maxlength') maxlength: number | string | null;

  @ViewChild('ftfTextarea') ftfTextarea: ElementRef<HTMLTextAreaElement>;
  @Output() valueChange = new EventEmitter<any>();
  value = '';

  disabled = false;

  isFocus = false;

  constructor(private cdr: ChangeDetectorRef) {}

  private onChange: (value: string) => void = () => {};

  private onTouched: () => void = () => {};

  ngOnInit() {}

  writeValue(value: string): void {
    if (value) {
      this.value = value;
      this.cdr.detectChanges();
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onInput() {
    this.emit();
  }

  blur(): void {
    this.isFocus = false;
    this.cdr.detectChanges();
    this.valueChange.emit();
  }

  focus(): void {
    this.isFocus = true;

    window.setTimeout(() => {
      this.ftfTextarea.nativeElement.focus();
    });
    this.cdr.detectChanges();
  }

  private emit(): void {
    this.onChange(this.value);
    this.onTouched();
  }
}
