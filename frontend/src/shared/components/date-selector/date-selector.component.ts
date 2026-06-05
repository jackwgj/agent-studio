import { CommonModule } from '@angular/common';
import { Component, forwardRef, OnInit } from '@angular/core';
import {
  ControlValueAccessor,
  FormsModule,
  NG_VALUE_ACCESSOR,
} from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { NzSelectModule } from 'ng-zorro-antd/select';

type FormatType = 'preset' | 'custom';

@Component({
  selector: 'date-selector',
  template: `
    <div class="cmp-date-selector">
      <div class="flex w-full">
        <div class="w-[30%]">
          <nz-select
            class="combo-select-left"
            style="width: 100%"
            [nzOptions]="formatOps"
            [(ngModel)]="formatTypeStr"
            (ngModelChange)="onFormatChange()"
          ></nz-select>
        </div>

        <div class="w-[70%]">
          <input
            class="combo-select-right"
            *ngIf="formatTypeStr === 'custom'"
            style="width: 100%"
            type="text"
            nz-input
            [placeholder]="'input_placeholder' | i18nextEager"
            [(ngModel)]="ruleStr"
            (ngModelChange)="onInput()"
            (blur)="onInputBlur()"
          />

          <nz-select
            class="combo-select-right"
            *ngIf="formatTypeStr === 'preset'"
            style="width: 100%"
            [nzOptions]="presetOps"
            [(ngModel)]="ruleStr"
            [nzPlaceHolder]="'select_placeholder' | i18nextEager"
            (ngModelChange)="onSelect()"
          ></nz-select>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .cmp-date-selector {
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

      .combo-select-left {
        border-top-right-radius: 0px !important;
        border-bottom-right-radius: 0px !important;
      }

      .combo-select-right {
        border-top-left-radius: 0px !important;
        border-bottom-left-radius: 0px !important;
      }
    `,
  ],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DateSelectorComponent),
      multi: true,
    },
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
  standalone: true,
  imports: [
    MODULES,
    NzSelectModule,
    FormsModule,
    CommonModule,
  ],
})
export class DateSelectorComponent implements ControlValueAccessor, OnInit {
  disabled = false;

  formatTypeStr: FormatType = 'preset';

  ruleStr = '';

  formatOps = [
    {
      label: this.i18n.transform('specify_format'),
      value: 'preset',
    },
    {
      label: this.i18n.transform('custom_format'),
      value: 'custom',
    },
  ];

  presetOps = [
    {
      label: '%Y-%m-%d %H:%M:%S',
      value: '%Y-%m-%d %H:%M:%S',
    },
    {
      label: '%Y-%m-%d',
      value: '%Y-%m-%d',
    },
    {
      label: '%H:%M',
      value: '%H:%M',
    },
  ];

  private presetValues = this.presetOps.map((op) => op.value);

  constructor(private i18n: I18NextEagerPipe) {}

  private onChange: (value: string) => void = () => {};

  private onTouched: () => void = () => {};

  ngOnInit() {}

  writeValue(value: string): void {
    if (value) {
      if (this.presetValues.includes(value)) {
        this.formatTypeStr = 'preset';
      }
    } else {
      this.formatTypeStr = 'custom';
    }

    this.ruleStr = value;
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
    this.emitRuleStr();
  }

  onInputBlur() {
    this.emitRuleStr();
  }

  onSelect() {
    this.emitRuleStr();
  }

  onFormatChange() {
    if (this.formatTypeStr === 'preset') {
      this.ruleStr = 'YYYY-MM-DD HH:MM';
    } else {
      this.ruleStr = '';
    }

    this.emitRuleStr();
  }

  private emitRuleStr() {
    this.onChange(this.ruleStr);
    this.onTouched();
  }
}
