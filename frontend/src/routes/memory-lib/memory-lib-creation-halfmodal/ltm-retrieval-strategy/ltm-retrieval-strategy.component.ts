import {
  AfterViewInit,
  ChangeDetectionStrategy, ChangeDetectorRef,
  Component,
  computed,
  EventEmitter,
  forwardRef,
  inject,
  Injector,
  Input,
  OnDestroy,
  OnInit,
  Output,
  signal,
} from '@angular/core';
import { ControlValueAccessor, FormArray, FormBuilder, FormGroup, NG_VALUE_ACCESSOR, NgControl, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Clipboard } from '@angular/cdk/clipboard'; // 引入 CDK Clipboard

import { I18NEXT_NAMESPACE, I18NextEagerPipe, I18NextModule } from 'angular-i18next';
import { Subscription } from 'rxjs';

// 替换 NG-ZORRO 模块
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzFormModule } from 'ng-zorro-antd/form';

import { I18nNamespace } from '@i18n';
import { AgentConfigService } from '@routes/agent-center/agent-config.service';
import { MemoryStrategyType } from '@routes/memory-lib/memory-lib-enums';
import { IMemoryStrategy, IMemoryStrategyItem } from '@routes/memory-lib/memory-lib-interfaces';

@Component({
  selector: 'ltm-retrieval-strategy',
  standalone: true,
  // 更新 imports
  imports: [
    ReactiveFormsModule,
    I18NextModule,
    NzCheckboxModule,
    NzInputModule,
    NzIconModule,
    NzToolTipModule,
    NzFormModule
  ],
  templateUrl: './ltm-retrieval-strategy.component.html',
  styleUrl: './ltm-retrieval-strategy.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => LtmRetrievalStrategyComponent),
      multi: true,
    },
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MEMORY_LIB],
    },
  ],
})
export class LtmRetrievalStrategyComponent implements ControlValueAccessor, OnInit, AfterViewInit, OnDestroy {
  @Input()
  set value(strategies: IMemoryStrategy[]) {
    this.writeValue(strategies);
  }

  @Output() readonly valueChange = new EventEmitter<IMemoryStrategy[]>();

  readonly injector = inject(Injector);
  readonly memoryStrategyType = MemoryStrategyType;
  readonly fb = inject(FormBuilder);
  readonly i18n = inject(I18NextEagerPipe);
  readonly agentConfigService = inject(AgentConfigService);
  readonly cdr = inject(ChangeDetectorRef);
  readonly clipboard = inject(Clipboard); // 注入 Clipboard 服务

  strategyList = signal<IMemoryStrategyItem[]>([
    {
      type: this.memoryStrategyType.SEMANTIC_MEMORY,
      name: this.i18n.transform('memory.create.title.semanticMemory'),
      description: this.i18n.transform('memory.create.longMemoryStrategy.semanticTip'),
      defaultPrompt: '',
    },
    {
      type: this.memoryStrategyType.USER_PROFILE,
      name: this.i18n.transform('memory.create.title.userProfile'),
      description: this.i18n.transform('memory.create.longMemoryStrategy.customTip'),
      defaultPrompt: '',
    },
  ]);

  strategyForm: FormGroup = this.#resolveStrategyForm();
  ngControl = signal<NgControl | null>(null);
  controlErrors = signal<ValidationErrors | null>(null);

  hasError = computed(() => {
    const _ = this.controlErrors();
    const ctrl = this.ngControl()?.control;
    return ctrl ? ctrl.invalid && (ctrl.touched || ctrl.dirty) : false;
  });

  formStatusSubscription: Subscription | null = null;
  valueChangeSubscription: Subscription | null = null;

  private onTouched: () => void = () => {};
  private onChange: (value: IMemoryStrategy[]) => void = () => {};

  get strategyListFormArray() {
    return this.strategyForm.get('strategyList') as FormArray;
  }

  // 新增复制方法
  copyText(text: string): void {
    this.clipboard.copy(text);
  }

  #getDefaultPrompt() {
    const config = this.agentConfigService.getConfigs();
    const { user_profile_prompt = '', semantic_memory_prompt = '' } = config;
    const updatedStrategyList = this.strategyList().map(strategy => {
      const newStrategy = { ...strategy };
      if (strategy.type === MemoryStrategyType.USER_PROFILE) {
        newStrategy.defaultPrompt = user_profile_prompt;
      }
      if (strategy.type === MemoryStrategyType.SEMANTIC_MEMORY) {
        newStrategy.defaultPrompt = semantic_memory_prompt;
      }
      return newStrategy;
    });
    this.strategyList.set(updatedStrategyList);

    this.strategyListFormArray.controls.forEach(group => {
      const strategyType = group.get('type')?.value;
      const defaultItem = updatedStrategyList.find(item => item.type === strategyType);
      if (defaultItem) {
        group.patchValue({ prompt: defaultItem.defaultPrompt });
      }
    });
  }

  #resolveStrategyForm(): FormGroup {
    return this.fb.group({
      strategyList: this.fb.array(
        this.strategyList().map(item =>
          this.fb.group({
            type: item.type,
            checked: false,
            prompt: [item.defaultPrompt, [Validators.maxLength(1000)]], // 统一为 Angular Validators
          })
        )
      ),
    });
  }

  #emitValue(): void {
    const strategies: IMemoryStrategy[] = [];
    this.strategyListFormArray.controls.forEach(group => {
      const groupValue = group.value;
      if (groupValue.checked) {
        strategies.push({ type: groupValue.type, prompt: groupValue.prompt });
      }
    });
    this.onChange(strategies);
    this.valueChange.emit(strategies);
  }

  ngOnInit(): void {
    this.#getDefaultPrompt();
  }

  ngAfterViewInit(): void {
    try {
      this.ngControl.set(this.injector.get(NgControl, { self: true, optional: true }));
      this.formStatusSubscription =
        this.ngControl()?.control?.statusChanges?.subscribe(() => {
          this.controlErrors.set(this.ngControl()?.control?.errors ?? null);
        }) ?? null;
    } catch (_) {
      this.ngControl.set(null);
    }
  }

  ngOnDestroy(): void {
    this.formStatusSubscription?.unsubscribe?.();
    this.valueChangeSubscription?.unsubscribe();
  }

  getErrorMessage(): string {
    const errs = this.controlErrors();
    if (!errs) {
      return '';
    }
    if (errs.isStrategyRequired) {
      return errs.isStrategyRequired.errorMsg;
    }
    return this.i18n.transform('memory.create.validation.inputDefaultError');
  }

  writeValue(value: IMemoryStrategy[]): void {
    if (value) {
      this.strategyListFormArray.controls.forEach(group => {
        const strategyType = group.get('type')?.value;
        const existingStrategy = value.find(s => s.type === strategyType);
        if (existingStrategy) {
          group.patchValue({ checked: true, prompt: existingStrategy.prompt });
        } else {
          const defaultItem = this.strategyList().find(item => item.type === strategyType);
          group.patchValue({ checked: false, prompt: defaultItem ? defaultItem.defaultPrompt : '' });
        }
      });
    } else {
      this.strategyForm.reset();
    }
    this.cdr.detectChanges();
  }

  registerOnChange(fn: (value: IMemoryStrategy[]) => void): void {
    this.onChange = fn;
    this.valueChangeSubscription?.unsubscribe();
    this.valueChangeSubscription = this.strategyForm.valueChanges.subscribe(() => {
      this.#emitValue();
    });
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    isDisabled ? this.strategyForm.disable() : this.strategyForm.enable();
  }

  onTouch(): void {
    this.onTouched();
  }

  resetToDefaults(): void {
    this.strategyListFormArray.controls.forEach(group => {
      const strategyType = group.get('type')?.value;
      const defaultItem = this.strategyList().find(item => item.type === strategyType);
      if (defaultItem) {
        group.patchValue({ prompt: defaultItem.defaultPrompt });
      }
    });
    this.#emitValue();
  }

  getStrategyPrompt(strategyType: MemoryStrategyType): string {
    const strategyControl = this.strategyListFormArray.controls.find(group => group.get('type')?.value === strategyType);
    return strategyControl?.get?.('prompt')?.value ?? '';
  }

  getStrategyChecked(strategyType: MemoryStrategyType): boolean {
    const strategyControl = this.strategyListFormArray.controls.find(group => group.get('type')?.value === strategyType);
    return strategyControl?.get?.('checked')?.value ?? false;
  }
}
