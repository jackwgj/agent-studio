import { NgIf } from '@angular/common';
import { Component, forwardRef, inject, Input } from '@angular/core';
import { ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR } from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe, I18NextModule } from 'angular-i18next';
import {MODULES} from "@shared/modules";

@Component({
  selector: 'struct-mem-edit-cell',
  template: `
    <div class="cell-wrapper">
      <div class="struct-mem-edit-cell-container"
           [class.invalid-cell]="isInvalid">
        <div class="tag-name-container" *ngIf="tagName">
          <nz-tag nzColor="processing">{{ tagName }}</nz-tag>
        </div>
        <div class="tag-value-edit-container">
          <input
            [required]="true"
            [maxlength]="valueMaxLength"
            type="text"
            nz-input
            [(ngModel)]="_value"
            (input)="onModelChange($event)"
            class="struct-mem-edit-cell"
            [disabled]="disabled"
            nz-tooltip
            [nzTooltipTitle]="_value"
          >
        </div>
      </div>
      <nz-tag nzColor="error" *ngIf="isInvalid">{{ errorMessage }}</nz-tag>
    </div>
  `,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => StructMemEditCellComponent),
      multi: true,
    },
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    I18NextModule,
    MODULES
  ],
  styles: [
    `
      :host.ng-invalid {
        .struct-mem-edit-cell-container {
          border-color: #FFBCBA;
        }
      }

      .cell-wrapper {
        width: 100%;
      }

      .struct-mem-edit-cell-container {
        height: 28px;
        padding: 4px 12px;
        box-sizing: border-box;
        border: 1px solid #ADB0B8;
        border-radius: 6px;
        background: rgba(255, 255, 255, 1);
        display: flex;
        gap: 8px;
        align-items: center;

        &.invalid-cell {
          border-color: #F66F6A !important;
          background-color: #FFEEED;
        }

        &:hover {
          border-color: #575D6C;
        }

        .tag-name-container {
          max-width: 30%;
        }

        .tag-value-edit-container {
          flex: 1 0;

          input {
            width: 100%;
            border: unset;
            background: transparent;
            padding-left: 0;
            padding-right: 0;
          }
        }
      }

      ti-error-msg {
        color: #DE504E;
      }
    `,
  ],
})
export class StructMemEditCellComponent implements ControlValueAccessor {
  @Input() disabled = false;

  @Input() tagName = '';

  _value: string = '';

  valueMaxLength = 1000;

  private readonly i18n = inject(I18NextEagerPipe);

  get errorMessage() {
    if (!this._value) {
      return this.i18n.transform('input_can_not_empty');
    }
    if (this._value?.length > 1000) {
      return this.i18n.transform('input_length_not_allow_over', {
        length: this.valueMaxLength,
      });
    }
    return this.i18n.transform('input_invalid_value');
  }

  get isInvalid() {
    return !this._value || this._value?.length > this.valueMaxLength;
  }

  writeValue(value: any): void {
    this._value = value;
  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onModelChange(event: Event): void {
    const newValue = (event.target as HTMLInputElement).value;
    this._value = newValue;
    this.onChange(newValue);
    this.onTouched();
  }

  private onChange: (value: any) => void = () => {
  };
  private onTouched: () => void = () => {
  };
}
