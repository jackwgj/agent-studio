import { ChangeDetectionStrategy, Component, computed, inject, Input, OnDestroy, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, FormsModule, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { I18NEXT_NAMESPACE, I18NextEagerPipe, I18NextModule } from 'angular-i18next';

// NG-ZORRO 导入
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzGridModule } from 'ng-zorro-antd/grid';

import { I18nNamespace } from '@i18n';
import { UploadImageComponent } from '@routes/knowledge-center/components/upload-image/upload-image.component';
import { KbUtils } from '@routes/knowledge-center/kb.utils';
import { MemoryLibApiService } from '@routes/memory-lib/memory-lib-api.service';
import { MEMORY_LIB_DEFAULT_ICON } from '@routes/memory-lib/memory-lib-constants';
import { LtmRetrievalStrategyComponent } from '@routes/memory-lib/memory-lib-creation-halfmodal/ltm-retrieval-strategy/ltm-retrieval-strategy.component';
import { IMemoryStrategy } from '@routes/memory-lib/memory-lib-interfaces';
import { CommonService } from '@services/common.service';
import { HttpService } from '@services/http.service';
import { SetSidebarVisibilityService } from '@shared/services/set-sidebar-visibility.service';

@Component({
  selector: 'memory-lib-creation-halfmodal',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    I18NextModule,
    NzFormModule,
    NzInputModule,
    NzButtonModule,
    NzSpinModule,
    NzIconModule,
    NzToolTipModule,
    NzGridModule,
    LtmRetrievalStrategyComponent,
    UploadImageComponent,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MEMORY_LIB],
    },
  ],
  templateUrl: './memory-lib-creation-halfmodal.component.html',
  styleUrl: './memory-lib-creation-halfmodal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MemoryLibCreationHalfmodalComponent implements OnInit, OnDestroy {
  @Input() libId = '';

  readonly visibilityService = inject(SetSidebarVisibilityService);
  readonly i18n = inject(I18NextEagerPipe);
  readonly http = inject(HttpService);
  readonly commonService = inject(CommonService);
  readonly memoryLibApiService = inject(MemoryLibApiService);
  readonly fb = inject(FormBuilder);

  modalTitle = computed(() => (this.libId ? this.i18n.transform('memory.edit.title') : this.i18n.transform('memory.create.title')));
  loading = signal(false);
  defaultIcon = MEMORY_LIB_DEFAULT_ICON;

  helpConfig = {
    iconTip: this.i18n.transform('memory.create.strategy.triggerTip'),
  };

  basicInfoFormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(50), this.nameVerify()]],
    description: ['', [Validators.maxLength(1000)]],
    icon: [this.defaultIcon],
  });

  ltmRetrievalStrategyFormGroup = this.fb.group({
    ltmRetrievalStrategy: [[] as IMemoryStrategy[], [this.strategyRequiredValidator()]],
  });

  private destroy$ = new Subject<void>();

  public setLoading = (isLoading: boolean) => {
    this.loading.set(isLoading);
  };

  get currentNameLength() {
    return this.basicInfoFormGroup.get('name')?.value?.length ?? 0;
  }

  constructor() {
    this.http.getCurrentUserResourceStatus().pipe(takeUntil(this.destroy$)).subscribe();
  }

  #queryDetail() {
    if (this.libId) {
      this.loading.set(true);
      this.memoryLibApiService
        .queryMemoryLibDetail(this.libId)
        .then(res => {
          this.basicInfoFormGroup.patchValue({
            name: res.name,
            description: res.description,
            icon: res.icon,
          });
          this.ltmRetrievalStrategyFormGroup.patchValue({
            ltmRetrievalStrategy: res.long_term_memory_strategies ?? [],
          });
        })
        .finally(() => {
          this.loading.set(false);
        });
    }
  }

  ngOnInit() {
    this.#queryDetail();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  nameVerify(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      return /^(?!\s)[\u4e00-\u9fa5a-zA-Z0-9_\- ]{1,50}(?<!\s)$/.test(control.value)
        ? null
        : { name: { tiErrorMessage: this.i18n.transform('memory.create.name.regx') } };
    };
  }

  onImageChanged(file: any) {
    if (!file) {
      return;
    }
    KbUtils.readFileAsBase64(file).then(res => {
      this.basicInfoFormGroup.patchValue({ icon: res });
    });
  }

  strategyRequiredValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      return !value || value.length < 1
        ? {
          isStrategyRequired: {
            value: control.value,
            errorMsg: this.i18n.transform('memory.create.errorTip.strategyNeed'),
          },
        }
        : null;
    };
  }

  dismiss(): void {}
  close(): void {}
}
