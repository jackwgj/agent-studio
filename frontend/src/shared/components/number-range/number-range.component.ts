import { Component, EventEmitter, forwardRef, Input, OnInit, Output } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MODULES } from "@shared/modules";

@Component({
  selector: 'number-range',
  template: `
    <div class="number-range">
      <input
        nz-input
        [value]="minValue"
        (input)="onMinChange($event)"
        (blur)="blurMin()"
        [placeholder]="leftPlaceholder"
      />
      <span>-</span>
      <input
        nz-input
        [value]="maxValue"
        (input)="onMaxChange($event)"
        (blur)="blurMax()"
        [placeholder]="rightPlaceholder"
      />
    </div>
  `,
  styles: [
    `
      .number-range {
        display: flex;
        gap: 8px;
        align-items: center;
      }

      input {
        width: 50%;
      }

      input.invalid {
        border-color: red;
      }
    `,
  ],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => NumberRangeComponent),
      multi: true,
    },
  ],
  standalone: true,
  imports: [MODULES],
})
export class NumberRangeComponent implements ControlValueAccessor, OnInit {
  @Input('leftPlaceholder') leftPlaceholder = '';

  @Input('rightPlaceholder') rightPlaceholder = '';
  @Output() valueChange = new EventEmitter<any>();

  minValue = '';

  maxValue = '';

  disabled = false;

  private onChange: (value: string) => void = () => {};

  private onTouched: () => void = () => {};

  ngOnInit() {}

  writeValue(value: string): void {
    if (value) {
      const [min, max] = value.split(',');
      this.minValue = min;
      this.maxValue = max;
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

  onMinChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.minValue = value;
    this.emit();
  }

  onMaxChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.maxValue = value;
    this.emit();
  }

  blurMin(): void {
    this.emit();
    this.valueChange.emit();
  }

  blurMax(): void {
    this.emit();
    this.valueChange.emit();
  }

  private emit(): void {
    this.onChange(`${this.minValue},${this.maxValue}`);
    this.onTouched();
  }
}
