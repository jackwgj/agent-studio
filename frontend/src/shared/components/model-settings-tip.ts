import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { ClickOutsideDirective } from '@shared/directives/click-outside.directive';
import { MODULES } from '@shared/modules';
import { getMaxReplySetting } from '@routes/agent-center/utils';
import { modeSelectTipText } from '@routes/agent-center/app-agent/common-logic-agent';
import { FormsModule } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzSliderModule } from 'ng-zorro-antd/slider';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzSelectModule } from 'ng-zorro-antd/select';

export interface IModelParam {
  top_p: number;
  temperature: number;
  history_size: number;
  max_tokens: number;
  frequency_penalty: number;
  enable_thinking?: boolean;
}

@Component({
  selector: 'model-settings',
  template: `
    <div
      class="flex max-h-[400px] w-[500px] flex-col gap-[24px]"
    >
      <div class="form-field">
        <div class="form-item">
          <div class="form-item-label">
            {{'mode_selection'|i18nextEager}}
            <span
              nz-tooltip
              [nzTooltipTitle]="modeSelectTipContent"
              nzTooltipPlacement="bottomLeft"
              class="common-tip-config"
            >
              <nz-icon nzType="question-circle" nzTheme="outline" />
            </span>
            <ng-template #modeSelectTipContent>
              <div *ngFor="let item of modeSelectTipText; index as i">
                <div style="font-weight: 600">{{ item.mode }}</div>
                <div>{{ item.desc }}</div>
              </div>
            </ng-template>
          </div>
          <nz-radio-group
            class="!mt-2"
            [nzButtonStyle]="'solid'"
            [(ngModel)]="modeValue"
            (ngModelChange)="changeMode($event)"
          >
            <label nz-radio-button [nzValue]="'exact'">{{modes[0].text}}</label>
            <label nz-radio-button [nzValue]="'balance'">{{modes[1].text}}</label>
            <label nz-radio-button [nzValue]="'creative'">{{modes[2].text}}</label>
            <label nz-radio-button [nzValue]="'custom'">{{modes[3].text}}</label>
          </nz-radio-group>
        </div>

        <div class="form-item">
          <div class="form-item-label">
            {{ 'model_temperature' | i18nextEager }}
            <span
              nz-tooltip
              [nzTooltipTitle]="'model_temperature_desc' | i18nextEager"
              nzTooltipPlacement="top"
              class="cursor-pointer"
            >
              <nz-icon nzType="question-circle" nzTheme="outline" />
            </span>
          </div>
          <div class="flex items-center gap-4">
            <nz-slider
              class="!my-0 flex-1"
              [(ngModel)]="kbParams.temperature"
              [nzMin]="0"
              [nzMax]="1"
              [nzStep]="0.1"
              (nzOnAfterChange)="changeTemperature()"
              (ngModelChange)="checkAndSwitchToCustom('temperature', $event)"
            >
            </nz-slider>
            <nz-input-number
              [nzMin]="0"
              [nzMax]="1"
              [nzStep]="0.1"
              [(ngModel)]="kbParams.temperature"
              (ngModelChange)="changeTemperatureSpinner($event)"
              style="width: 120px"
            >
            </nz-input-number>
          </div>
        </div>

        <div class="form-item">
          <div class="form-item-label">
            {{ 'model_top_p' | i18nextEager }}
            <span
              nz-tooltip
              [nzTooltipTitle]="'model_top_p_desc' | i18nextEager"
              nzTooltipPlacement="top"
              class="cursor-pointer"
            >
              <nz-icon nzType="question-circle" nzTheme="outline" />
            </span>
          </div>
          <div class="flex items-center gap-4">
            <nz-slider
              class="!my-0 flex-1"
              [(ngModel)]="kbParams.top_p"
              [nzMin]="0.1"
              [nzMax]="1"
              [nzStep]="0.1"
              (ngModelChange)="checkAndSwitchToCustom('top', $event)"
            >
            </nz-slider>
            <nz-input-number
              class="w-[100px] !rounded-md"
              [nzPlaceHolder]="'0-1'"
              [nzMax]="1"
              [nzMin]="0"
              [nzStep]="0.1"
              [(ngModel)]="kbParams.top_p"
              (ngModelChange)="changeTopSpinner($event)"
            >
            </nz-input-number>
          </div>
        </div>

        <div class="form-item">
          <div style="background: rgba(240, 240, 240, 1);height: 2px;width: 100%"></div>
        </div>

        <div class="form-item" *ngIf="showThinkSwitch && selectedModel?.is_support_close_reasoning">
          <div class="form-item-label">
            {{ 'modelsettingstip_51' | i18nextEager }}
            <span
              nz-tooltip
              [nzTooltipTitle]="thinkTipContent"
              nzTooltipPlacement="bottomLeft"
              class="common-tip-config"
            >
              <nz-icon nzType="question-circle" nzTheme="outline" />
            </span>
            <ng-template #thinkTipContent>
              <div>{{ 'deep_think_tip'|i18nextEager }}</div>
            </ng-template>
          </div>
          <div class="flex">
            <nz-switch [(ngModel)]="kbParams.enable_thinking"></nz-switch>
          </div>
        </div>

        <div class="form-item" *ngIf="isLlm">
          <div class="form-item-label">
            {{'model_history_size'|i18nextEager}}
            <span
              nz-tooltip
              [nzTooltipTitle]="'model_history_size_desc' | i18nextEager"
              nzTooltipPlacement="top"
              class="cursor-pointer"
            >
              <nz-icon nzType="question-circle" nzTheme="outline" />
            </span>
          </div>
          <div class="flex items-center gap-4">
            <nz-slider
              class="!my-0 flex-1"
              [(ngModel)]="kbParams.history_size"
              [nzMin]="0"
              [nzMax]="100"
              [nzStep]="1"
            >
            </nz-slider>
            <nz-input-number
              class="w-[100px] !rounded-md"
              [nzPlaceHolder]="'0-20'"
              [nzMax]="100"
              [nzMin]="0"
              [nzStep]="1"
              [(ngModel)]="kbParams.history_size"
            >
            </nz-input-number>
          </div>
        </div>

        <div class="form-item">
          <div class="form-item-label">
            {{ 'model_max_reply_length' | i18nextEager }}
            <span
              nz-tooltip
              [nzTooltipTitle]="'model_max_reply_length_desc' | i18nextEager"
              nzTooltipPlacement="top"
              class="cursor-pointer"
            >
              <nz-icon nzType="question-circle" nzTheme="outline" />
            </span>
          </div>
          <div class="flex items-center gap-4">
            <nz-slider
              class="!my-0 flex-1"
              [(ngModel)]="kbParams.max_tokens"
              [nzMin]="maxReplySetting.min"
              [nzMax]="maxReplySetting.max"
              [nzStep]="1"
            >
            </nz-slider>
            <nz-input-number
              class="w-[100px] !rounded-md"
              [nzPlaceHolder]="maxReplySetting.min + '-' + maxReplySetting.max"
              [nzMax]="maxReplySetting.max"
              [nzMin]="maxReplySetting.min"
              [nzStep]="1"
              [(ngModel)]="kbParams.max_tokens"
            >
            </nz-input-number>
          </div>
        </div>

        <div class="form-item" *ngIf="isLlm">
          <div class="form-item-label">
            {{ 'model_frequency_penalty' | i18nextEager }}
            <span
              nz-tooltip
              [nzTooltipTitle]="'model_frequency_penalty_desc' | i18nextEager"
              nzTooltipPlacement="top"
              class="cursor-pointer"
            >
              <nz-icon nzType="question-circle" nzTheme="outline" />
            </span>
          </div>
          <div class="flex items-center gap-4">
            <nz-slider
              class="!my-0 flex-1"
              [(ngModel)]="kbParams.frequency_penalty"
              [nzMin]="-2"
              [nzMax]="2"
              [nzStep]="1"
            >
            </nz-slider>
            <nz-input-number
              class="w-[100px] !rounded-md"
              [nzPlaceHolder]="'-2~2'"
              [nzMax]="2"
              [nzMin]="-2"
              [nzStep]="1"
              [(ngModel)]="kbParams.frequency_penalty"
            >
            </nz-input-number>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        padding: 8px;
      }
      .form-field {
        display: flex;
        flex-direction: column;
        gap: 12px;
      }
      .form-item {
        display: flex;
        flex-direction: column;
      }
      .form-item-label {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: normal;
      }
      .common-tip-config {
        cursor: pointer;
      }
    `,
  ],
  standalone: true,
  imports: [
    MODULES,
    FormsModule,
    ClickOutsideDirective,
    NzFormModule,
    NzIconModule,
    NzToolTipModule,
    NzRadioModule,
    NzSliderModule,
    NzInputNumberModule,
    NzSwitchModule,
    NzSelectModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT, I18nNamespace.MODEL_ACCESS],
    },
  ],
})
export class modelSettingsComponent implements OnInit, OnDestroy, OnChanges {
  @Input() param: IModelParam = {
    top_p: 0.5,
    temperature: 0.5,
    history_size: 3,
    max_tokens: null,
    frequency_penalty: 0,
    enable_thinking: true,
  };

  @Input() isLlm: boolean = false;

  @Input() selectedModel:any = {};

  @Input() showThinkSwitch: boolean = false;

  @Output() updatedParam = new EventEmitter<IModelParam>();

  public scales = [0, 0.25, 0.5, 0.75, 1];
  public modeSelectTipText = modeSelectTipText;
  statusFlod = false;

  public kbParams: IModelParam = {
    top_p: 0.5,
    temperature: 0.5,
    history_size: 3,
    max_tokens: null,
    frequency_penalty: 0,
    enable_thinking: true,
  };

  public maxReplySetting = {
    min: 1,
    max: 131072,
    default: 2048,
  };

  public modes: any = [
    {
      value: 'exact',
      text: this.i18n.transform('accurate'),
    },
    {
      value: 'balance',
      text: this.i18n.transform('balanced'),
    },
    {
      value: 'creative',
      text: this.i18n.transform('creative'),
    },
    {
      value: 'custom',
      text: this.i18n.transform('custom'),
    },
  ];

  public modesTemperatureAndTop: any = [
    {
      temperature: 0.1,
      top_p: 0.7,
    },
    {
      temperature: 1,
      top_p: 0.7,
    },
    {
      temperature: 1,
      top_p: 0.8,
    },
    {
      temperature: 0,
      top_p: 1,
    },
  ];

  public mode: any = this.modes[0];
  public modeValue: string = 'exact';

  constructor(
    private i18n: I18NextEagerPipe,
  ) { }
  ngOnInit(): void {
    this.initParams();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.param) {
      this.initParams();
    }
  }

  private initParams(): void {
    this.kbParams = {
      top_p: this.param?.top_p ?? 0.5,
      temperature: this.param?.temperature ?? 0.5,
      history_size: this.param?.history_size ?? 3,
      max_tokens: this.param?.max_tokens ?? null,
      frequency_penalty: this.param?.frequency_penalty ?? 0,
      enable_thinking: this.param?.enable_thinking ?? true,
    };
    this.isCustomMode();
    this.setMaxReplySetting(this.selectedModel);
  }

  public toggleStatus(event) {
    this.statusFlod = !this.statusFlod;
    event.stopPropagation();
  };

  public filterNaN(event: KeyboardEvent): void {
    if (['e', 'E', '+'].includes(event.key)) {
      event.preventDefault();
    }
  }

  public onParamBlur(key: string) {
    const value = Number(this.kbParams[key]);
    this.kbParams[key] = value.toFixed(1);
  }

  ngOnDestroy(): void {
   this.close()
  }

  public close() {
    this.updatedParam.emit({
      top_p: this.kbParams.top_p,
      temperature: this.kbParams.temperature,
      history_size: this.kbParams.history_size,
      max_tokens: this.kbParams.max_tokens,
      frequency_penalty: this.isLlm ? this.kbParams.frequency_penalty : null,
      enable_thinking: (this.showThinkSwitch && this.selectedModel?.is_support_close_reasoning)
        ? this.kbParams.enable_thinking
        : null,
    });

    console.log({
      top_p: this.kbParams.top_p,
      temperature: this.kbParams.temperature,
      history_size: this.kbParams.history_size,
      max_tokens: this.kbParams.max_tokens,
      frequency_penalty: this.isLlm ? this.kbParams.frequency_penalty : null,
      enable_thinking: (this.showThinkSwitch && this.selectedModel?.is_support_close_reasoning)
        ? this.kbParams.enable_thinking
        : null,
    })
  }

  private setMaxReplySetting(selectedModel): void {
    this.maxReplySetting = getMaxReplySetting(selectedModel);
    if (!this.kbParams.max_tokens) {
      this.kbParams.max_tokens = this.maxReplySetting.default ?? 2048;
    } else {
      this.kbParams.max_tokens = Math.min(
        this.kbParams.max_tokens,
        this.maxReplySetting.max,
      );
    }
  }

  public changeMode(event: any) {
    const modeMapping: any = {
      exact: this.modesTemperatureAndTop[0],
      balance: this.modesTemperatureAndTop[1],
      creative: this.modesTemperatureAndTop[2],
      custom: this.modesTemperatureAndTop[3],
    };

    if (modeMapping[event]) {
      this.kbParams.temperature = modeMapping[event].temperature;
      this.kbParams.top_p = modeMapping[event].top_p;
    }
  }

  public changeTemperature() {
    this.isCustomMode();
  }

  public changeTemperatureSpinner(newValue: number) {
    this.kbParams.temperature = newValue;
    this.isCustomMode();
  }

  private isCustomMode() {
    const modeMap = [
      {
        temperature: this.modesTemperatureAndTop[0].temperature,
        top_p: this.modesTemperatureAndTop[0].top_p,
        mode: this.modes[0].value,
      },
      {
        temperature: this.modesTemperatureAndTop[1].temperature,
        top_p: this.modesTemperatureAndTop[1].top_p,
        mode: this.modes[1].value,
      },
      {
        temperature: this.modesTemperatureAndTop[2].temperature,
        top_p: this.modesTemperatureAndTop[2].top_p,
        mode: this.modes[2].value,
      },
    ];

    const matchedMode = modeMap.find(
      (mode) => this.kbParams.temperature === mode.temperature && this.kbParams.top_p === mode.top_p,
    );
    if (matchedMode) {
      this.mode = this.modes[this.findModeByValue(matchedMode.mode)];
      this.modeValue = matchedMode.mode;
    } else {
      this.mode = this.modes[3];
      this.modeValue = 'custom';
    }
  }

  private findModeByValue(value) {
    return this.modes.findIndex((mode) => mode.value === value);
  }

  private switchToCustomMode(): void {
    this.mode = this.modes[3];
    this.modeValue = 'custom';
  }

  public checkAndSwitchToCustom(changedParam: string, newValue: any): void {
    switch (changedParam) {
      case 'temperature':
        this.kbParams.temperature = newValue;
        break;
      case 'top':
        this.kbParams.top_p = newValue;
        break;
      default:
        break;
    }

    if (this.modeValue !== 'custom') {
      this.switchToCustomMode();
    }
  }

  public changeTopSpinner(newValue: number) {
    this.kbParams.top_p = newValue;
    this.isCustomMode();
  }
}
