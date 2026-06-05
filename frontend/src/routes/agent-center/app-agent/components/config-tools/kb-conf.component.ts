import { ChangeDetectorRef, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { I18nNamespace } from "@i18n";
import { KBRetrieveMode, SearchModeType } from '@routes/knowledge-center/knowledge-base.interface';
import { KbAbilitiesService } from "@services/knowledge-center/kb-abilities.service";
import { CommonSkeletonComponent } from '@shared/components/common-skeleton/common-skeleton';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from "angular-i18next";
import { ClickOutsideDirective } from "@shared/directives/click-outside.directive";
import { MODULES } from "@shared/modules";
import { AgentConfigService } from "@routes/agent-center/agent-config.service";
import { ValueDuplicateValidatorDirective } from "@shared/directives/variable-name-validator.directive";
import { AccBlockComponent } from "@routes/agent-center/app-flow/components/acc-block/acc-block.component";

export interface IKbParam {
  top_k: number;
  recall_threshold: number;
  search_mode: string;
  faq_threshold: number;
  show_source?: boolean;
  retrieve_image?: boolean;
  need_extras_faq_search: boolean;
  extra_params?: Array<{ key: string, value: string }>;
}

@Component({
  selector: 'kb-conf',
  template: `
    <div (clickOutside)="close()" style='max-height:500px;color:black;width:500px' class="flex w-[400px] flex-col gap-[16px]">
      <div>
        <div *ngIf="searchModeOptions?.length > 0">
          <span nz-tooltip [nzTooltipTitle]="retrieveModeTip" nzTooltipPlacement="top" class="cursor-help">
            {{ 'kb_conf.helptip_label' | i18nextEager }}
            <nz-icon nzType="question-circle" nzTheme="outline"></nz-icon>
          </span>
        </div>
        <common-skeleton type="list" *ngIf="abilityLoading; else searchModeDisplay"></common-skeleton>
        <ng-template #searchModeDisplay>
          <div class="flex flex-col gap-[8px]">
            @for (item of searchModeOptions; track item.value) {
              <div
                class="selectable-card"
                [class.selected]="kbParams.search_mode === item.value"
                (click)="kbParams.search_mode = item.value"
              >
                <div class="flex items-center gap-x-[8px]">
                  <img
                    [src]="item.iconName"
                    alt="search_mode-icon"
                    class="h-[24px] w-[24px]"
                  />
                  <span class="font-bold">{{ item.title }}</span>
                  <span class="flex-1 text-[#808080] overflow-hidden line-clamp-2" [title]="item.summary">{{
                      item.summary
                    }}</span>
                </div>
              </div>
            }
          </div>
        </ng-template>
      </div>
      <div *ngIf="lakesearch_faq_enable" class="flex w-full flex-col gap-[8px]">
        <div class="flex items-center gap-[8px]">
          <div>
            {{ 'relevance_enable' | i18nextEager }}
          </div>
          <nz-icon
            class="cursor-pointer"
            nz-tooltip
            [nzTooltipTitle]="'relevance_enable_tip' | i18nextEager"
            nzTooltipPlacement="top"
            nzType="question-circle"
            nzTheme="outline"
          />
        </div>
        <div class="flex items-center justify-between gap-4 pb-2">
          <nz-switch [nzDisabled]="isFaqDisabled" nz-tooltip [nzTooltipTitle]="faqDisabledTip" id="switch-basic" [(ngModel)]="kbParams.need_extras_faq_search"></nz-switch>
        </div>
      </div>
      <div *ngIf="kbParams.need_extras_faq_search && lakesearch_faq_enable" class="flex w-full flex-col gap-[8px]">
        <div class="flex items-center gap-[8px]">
          <div>{{ 'straight_out_threshold' | i18nextEager }}</div>
          <nz-icon
            class="cursor-pointer"
            nz-tooltip
            [nzTooltipTitle]="'straight_out_threshold_tip' | i18nextEager"
            nzTooltipPlacement="top"
            nzType="question-circle"
            nzTheme="outline"
          />
        </div>
        <div class="flex items-center justify-between gap-4 pb-2">
          <nz-slider
            class="!my-0 flex-1"
            [(ngModel)]="kbParams.faq_threshold"
            [nzMin]="recall_threshold_min"
            [nzMax]="recall_threshold_max"
            [nzStep]="0.1"
          >
          </nz-slider>
          <nz-input-number
            class="w-[64px] !rounded-md"
            [nzMin]="recall_threshold_min"
            [nzMax]="recall_threshold_max"
            [nzStep]="0.1"
            [(ngModel)]="kbParams.faq_threshold"
            (ngModelChange)="onParamBlur('faq_threshold')"
          ></nz-input-number>
        </div>
      </div>
      <div class="flex w-full flex-col gap-[8px]">
        <div class="flex items-center gap-[8px]">
          <div>
            {{ 'relevance_threshold' | i18nextEager }}
          </div>
          <nz-icon
            class="cursor-pointer"
            nz-tooltip
            [nzTooltipTitle]="'relevance_threshold_tip' | i18nextEager"
            nzTooltipPlacement="top"
            nzType="question-circle"
            nzTheme="outline"
          />
        </div>
        <div class="flex items-center justify-between gap-4 pb-2">
          <nz-slider
            class="!my-0 flex-1"
            [(ngModel)]="kbParams.recall_threshold"
            [nzMin]="recall_threshold_min"
            [nzMax]="recall_threshold_max"
            [nzStep]="0.1"
          >
          </nz-slider>
          <nz-input-number
            class="w-[64px] !rounded-md"
            [nzMin]="recall_threshold_min"
            [nzMax]="recall_threshold_max"
            [nzStep]="0.1"
            [(ngModel)]="kbParams.recall_threshold"
            (ngModelChange)="onParamBlur('recall_threshold')"
          ></nz-input-number>
        </div>
      </div>
      <div class="flex w-full flex-col gap-[8px]">
        <div class="flex items-center gap-[8px]">
          <div>{{ 'topk_recall_quantity' | i18nextEager }}</div>
          <nz-icon
            class="cursor-pointer"
            nz-tooltip
            [nzTooltipTitle]="'topk_recall_quantity_tip' | i18nextEager"
            nzTooltipPlacement="top"
            nzType="question-circle"
            nzTheme="outline"
          />
        </div>
        <div class="flex items-center justify-between gap-4 pb-2">
          <nz-slider
            class="!my-0 flex-1"
            [(ngModel)]="kbParams.top_k"
            [nzMin]="1"
            [nzMax]="50"
            [nzStep]="1"
          >
          </nz-slider>
          <nz-input-number
            class="w-[64px] !rounded-md"
            [nzMin]="1"
            [nzMax]="50"
            [nzStep]="1"
            [(ngModel)]="kbParams.top_k"
            (ngModelChange)="onParamBlur('top_k')"
          ></nz-input-number>
        </div>
      </div>
      <meta-acc-block *ngIf="showCustomConfig"
        class="mt-4 block w-[calc(100%-8px)]"
        [title]="'custom_parameter' | i18nextEager"
        [tip]="'custom_parameter_tip' | i18nextEager"
      >
        <ng-template #content>
          <div class="mt-2 flex items-center gap-1">
            <div class="w-1/3 text-label">
              {{ 'param_name' | i18nextEager }}
            </div>
            <div class="flex w-2/3 items-center gap-2 text-label">
              <div class="flex h-fit w-[calc(100%-16px)] items-center">
                {{ 'param_value' | i18nextEager }}
              </div>
              <div class="h-full w-[16px]"></div>
            </div>
          </div>

          <form #inputForm="ngForm">
            <div
              *ngFor="let param of customParams; index as i"
              class="mt-2 flex gap-1"
            >
              <div class="w-1/3">
                <input
                  style="width: 100%"
                  type="text"
                  [name]="'inputFormName' + i"
                  nz-input
                  maxlength="64"
                  required
                  [title]="param.key"
                  [(ngModel)]="param.key"
                  [placeholder]="'input_placeholder' | i18nextEager"
                  [valueDuplicateValidator]="getInputNames(i)"
                />
              </div>
              <div class="flex w-2/3 gap-[8px]">
                <div class="flex h-fit w-[calc(100%-16px)] items-center gap-2">
                  <div class="flex w-full">
                    <div class="w-[100%]">
                      <input
                        class="combo-select-right"
                        style="width: 100%"
                        [name]="'inputFormValue' + i"
                        nz-input
                        type="text"
                        maxlength="255"
                        [placeholder]="'input_placeholder' | i18nextEager"
                        [(ngModel)]="param.value"
                        [title]="param.value"
                      />
                    </div>
                  </div>
                </div>
                <nz-icon
                  (click)="deleteInputParam($event,i)"
                  style="
                    font-size: 16px;
                    color: #191919;
                    cursor: pointer;
                    padding-top: 8px;
                  "
                  nzType="delete"
                  nzTheme="outline"
                />
              </div>
            </div>
          </form>
        </ng-template>

        <ng-template #right>
          <div class="flex items-center justify-center">
            <button
              (click)="addInputParam()"
              class="w-fit"
              nz-button
              nzType="text"
              nz-tooltip
              [nzTooltipTitle]="'add_params' | i18nextEager"
              [nzBlock]="false"
              [disabled]="isModelReachedParamLimit()"
            >
              <nz-icon
                class="header-button-icon"
                nzType="plus"
                nzTheme="outline"
              />
            </button>
          </div>
        </ng-template>
      </meta-acc-block>
      <div *ngIf="selectedNum > 0" class="flex items-center gap-[8px]" style="margin-top: 6px">
        <div>{{'view_source'|i18nextEager}}</div>
        <nz-switch [(ngModel)]="kbParams.show_source"></nz-switch>
      </div>
      <div *ngIf="kbAbilitiesService.isPermitShowImageInSearch" class="flex items-center gap-[8px]" style="margin-top: 6px">
        <div>{{'view_image'|i18nextEager}}</div>
        <nz-switch [(ngModel)]="kbParams.retrieve_image"></nz-switch>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        padding: 8px;
      }
      .selectable-card {
        background-color: #fafafa;
        border-radius: 8px;
        border: 1px solid transparent;
        transition: border-color 0.3s ease;
        padding: 12px;
        cursor: pointer;
        &:hover {
          border-color: #1476ff;
        }
        &.selected {
          background-color: #f0f7ff;
          border-color: #1476ff;
        }
      }
    `,
  ],
  standalone: true,
  imports: [MODULES, ClickOutsideDirective, ValueDuplicateValidatorDirective, AccBlockComponent, CommonSkeletonComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class KbConfComponent implements OnInit {
  @Input() param: IKbParam;

  @Input() selectedNum = 0;

  @Input() kbId = '';

  @Output() updatedParam = new EventEmitter<IKbParam>();

  public kbParams: IKbParam;

  public searchModeOptions: KBRetrieveMode[] = [];

  public customParams: Array<{ key: string, value: string }> = [];

  public lakesearch_faq_enable = false;

  public isFaqDisabled = false;

  faqDisabledTip = '';

  format: string = 'n3';

  recall_threshold_min = 0;

  recall_threshold_max = 1;

  recall_threshold_average = 0.5;

  public retrieveModeTip = '';

  showCustomConfig = false;

  abilityLoading = true;

  constructor(
    private i18n: I18NextEagerPipe,
    private configServ: AgentConfigService,
    private cdr: ChangeDetectorRef,
    public kbAbilitiesService: KbAbilitiesService,
  ) {}

  ngOnInit(): void {
    this.#initParam();
    this.#getSearchModes();
  }

  #initParam() {
    const {
      top_k = 3,
      recall_threshold = 0.5,
      search_mode = 'doc',
      faq_threshold = 0.9,
      show_source = false,
      need_extras_faq_search = false,
      extra_params = [],
      retrieve_image = false,
    } = this.param || {};
    this.kbParams = {
      top_k,
      recall_threshold,
      search_mode,
      faq_threshold,
      show_source,
      need_extras_faq_search,
      extra_params,
      retrieve_image,
    };
    if (this.kbParams.extra_params.length) {
      this.customParams = this.kbParams.extra_params;
    }
    this.lakesearch_faq_enable = this.configServ.faqEnable();
    this.recall_threshold_min =
      this.configServ.getConfigs().knowledge_recall_threshold_min ?? 0;
    this.recall_threshold_max =
      this.configServ.getConfigs().knowledge_recall_threshold_max ?? 1;
    this.recall_threshold_average =
      (this.recall_threshold_min + this.recall_threshold_max) / 2;
  }

  #getSearchModes() {
    this.abilityLoading = true;
    this.kbAbilitiesService.getKBConnectionId(this.kbId).subscribe(connectionId => {
      this.isFaqDisabled = false;
      this.faqDisabledTip = '';
      this.kbAbilitiesService.getKbRetrieveModes(connectionId).subscribe(res => {
        this.isFaqDisabled = Boolean(!res.find(item => item.value === SearchModeType.FAQ));
        if (this.isFaqDisabled) {
          this.faqDisabledTip = this.i18n.transform('faq_not_supported');
          this.kbParams.need_extras_faq_search = false;
        }
        this.showCustomConfig = this.kbAbilitiesService.showKbCustomConfig;
        this.searchModeOptions = res.filter(item => item.value !== SearchModeType.FAQ);
        this.cdr.detectChanges();
        const currentModeIsExist = Boolean(this.searchModeOptions.find(mode => mode.value === this.kbParams?.search_mode));
        if (!currentModeIsExist) {
          this.kbParams.search_mode = this.searchModeOptions?.[0]?.value ?? 'doc';
        }
        this.retrieveModeTip = this.searchModeOptions.reduce((pre, next) => {
          return `${pre}${pre ? '<br />' : ''}${next.title ?? ''}${Boolean(next.title) ? '：' : ''}${next.desc ?? ''}`
        }, '');
        this.abilityLoading = false;
        this.cdr.detectChanges();
      })
    })
  }

  public filterNaN(event: KeyboardEvent): void {
    if (['e', 'E', '+'].includes(event.key)) {
      event.preventDefault();
    }
  }

  public onParamBlur(key: string) {
    const value = Number(this.kbParams[key]);

    if (key === 'top_k') {
      this.kbParams[key] = Math.round(value);
    } else {
      this.kbParams[key] = value.toFixed(3);
    }
  }

  public close() {
    if (this.selectedNum === 0) {
      this.kbParams.show_source = false;
    }
    if (this.customParams.length) {
      const params = [];
      this.customParams.forEach(item => {
        if (item.key) {
          params.push(item);
        }
      })
      this.kbParams.extra_params = params;
    }

    this.updatedParam.emit(this.kbParams);
  }

  isModelReachedParamLimit() {
    return this.customParams.length === 50;
  }

  addInputParam() {
    this.customParams.push({
      key: '',
      value: '',
    });
  }

  deleteInputParam(event: Event, index: number) {
    event.stopPropagation();
    this.customParams.splice(index, 1);
  }

  getInputNames(index: number): {
    existingValues: string[];
  } {
    const names = this.customParams.map((p) => p.key);
    names.splice(index, 1);
    return { existingValues: [...names] };
  }
}
