import { Component, EventEmitter, forwardRef, Input, Output } from '@angular/core';
import { ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

@Component({
  selector: 'kb-connection-type-card',
  templateUrl: './kb-connection-type-card.component.html',
  styleUrls: ['./kb-connection-type-card.component.less'],
  standalone: true,
  imports: [
    MODULES,
    FormsModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.KNOWLEDGE, I18nNamespace.COMMON],
    },
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => KbConnectionTypeCardComponent),
      multi: true
    }
  ],
})
export class KbConnectionTypeCardComponent implements ControlValueAccessor {
  @Input() typeData = [];
  @Input() readonly = false;

  private _value: string;

  @Output() valueChange = new EventEmitter<any>();

  @Input()
  get value(): any {
    return this._value;
  }

  set value(val: any) {
    if (this._value !== val && !this.readonly) {
      this._value = val;
      this.onChange(val);
      this.valueChange.emit(val);
    }
  }

  constructor() {}

  onChange: any = () => {};
  onTouched: any = () => {};

  selectValue(value: any) {
    this.value = value;
  }

  writeValue(value: any): void {
    this._value = value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }
}
