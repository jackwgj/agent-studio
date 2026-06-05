import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  effect,
  EventEmitter,
  forwardRef,
  inject,
  input,
  Output,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { NzSelectModule, NzSelectComponent } from 'ng-zorro-antd/select';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { I18nNamespace } from '@i18n';
import { TagCreationComponent } from '@routes/knowledge-center/components/tag-creation/tag-creation.component';
import { MAX_KB_TAG_LIMIT } from '@routes/knowledge-center/knowledge.const';
import { KbTagItem } from '@routes/knowledge-center/knowledge.types';
import { KbTagService } from '@services/knowledge-center/kb-tag.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { v4 as uuidV4 } from 'uuid';

@Component({
  selector: 'tag-select',
  templateUrl: './tag-select.component.html',
  styleUrls: ['./tag-select.component.less'],
  standalone: true,
  imports: [MODULES, TagCreationComponent, NzSelectModule, NzButtonModule, NzIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.KNOWLEDGE],
    },
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TagSelectComponent),
      multi: true,
    },
  ],
})
export class TagSelectComponent implements ControlValueAccessor {
  @Output() goToTagManage = new EventEmitter();
  kbId = input<string>();

  isSupCreate = input(false);

  isSupRefresh = input(false);

  isLimitSelection = input(false);

  isCacheInInit = input(false);

  tagCreationModal = viewChild<TagCreationComponent>('tagCreationModal');

  tagSelectComp = viewChild<NzSelectComponent>('tagSelectComp');

  _value = signal(null);

  tagOptions = signal<KbTagItem[]>([]);

  isLoading = signal(false);

  compId = `tag-select-${uuidV4()}`;

  protected readonly MAX_KB_TAG_LIMIT = MAX_KB_TAG_LIMIT;
  protected readonly uuidV4 = uuidV4;
  private readonly kbTagService = inject(KbTagService);
  private readonly cdr = inject(ChangeDetectorRef);

  constructor() {
    effect(() => {
      const kbId = this.kbId();

      untracked(() => {
        if (kbId) {
          this.getTags(this.isCacheInInit());
        }
      });
    });
  }

  goToTag() {
    this.tagCreationModal()?.close();
    this.goToTagManage.emit();
  }

  onOpenChange(isOpen: boolean) {
    if (isOpen) {
      this.refreshTags();
    }
  }

  refreshTags() {
    this.isLoading.set(true);
    this.kbTagService.clearCache();
    this.getTags();
  }

  getTags(isCache = false) {
    const kbId = this.kbId();
    this.kbTagService.getKbTags(kbId, {
      offset: 0,
      limit: MAX_KB_TAG_LIMIT,
    }, isCache).subscribe({
      next: res => {
        this.tagOptions.set(res?.tag_list ?? []);
        setTimeout(() => {
          this.cdr.detectChanges();
        });
        this.isLoading.set(false);
      },
      error: () => {
        this.tagOptions.set([]);
        this.isLoading.set(false);
      },
    });
  }

  createTag() {
    this.tagCreationModal()?.showCreationModal();
  }

  footerClick(event: MouseEvent) {
    event.preventDefault();
  }

  writeValue(value?: string[]): void {
    this._value.set(value ?? []);
    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  modeChange(value) {
    this._value.set(value);
    this.onChange(value);
    this.onTouched();
  }

  closeCreation() {
    this.tagCreationModal()?.close();
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  private onChange = (value: any) => {
  };

  private onTouched = () => {
  };
}
