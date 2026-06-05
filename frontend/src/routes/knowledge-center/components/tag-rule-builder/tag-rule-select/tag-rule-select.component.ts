import { Component, effect, forwardRef, inject, input, output, signal, untracked } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { KbItem } from '@routes/knowledge-center/knowledge-base.interface';
import { MAX_KB_TAG_LIMIT } from '@routes/knowledge-center/knowledge.const';
import { KbTagService } from '@services/knowledge-center/kb-tag.service';
import { MODULES } from '@shared/modules';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzIconModule } from 'ng-zorro-antd/icon';

@Component({
  selector: 'tag-rule-select',
  templateUrl: './tag-rule-select.component.html',
  styleUrls: ['./tag-rule-select.component.less'],
  standalone: true,
  imports: [MODULES, NzSelectModule, NzIconModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [],
    },
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TagRuleSelectComponent),
      multi: true,
    },
  ],
})
export class TagRuleSelectComponent implements ControlValueAccessor {

  kbs = input.required<KbItem[]>();

  isRuleMode = input(false);

  deleteHandler = output();

  selectChange = output();

  _value = signal({
    operator: '',
    tag: '',
  });

  operatorOptions = [
    {
      label: '=',
      value: 'EQUAL',
    },
    {
      label: '≠',
      value: 'NOT_EQUAL',
    },
  ];

  tagOptions = [];
  isTagLoading = false;

  private readonly kbTagService = inject(KbTagService);

  constructor() {
    effect(() => {
      const kbs = this.kbs();

      untracked(() => {
        if (kbs?.length) {
          this.getTags(true);
        }
      });
    });

    effect(() => {
      const value = this._value();

      untracked(() => {
        this.onChange(value);
      });
    });
  }

  onTagSelectOpen(isOpen: boolean) {
    if (isOpen) {
      this.getTags();
    }
  }

  getTags(isCache = false) {
    const kbs = this.kbs();
    if (kbs.length) {
      this.isTagLoading = true;
      this.kbTagService.batchGetTags(kbs, {
        offset: 0,
        limit: MAX_KB_TAG_LIMIT,
      }, isCache).subscribe({
        next: res => {
          this.tagOptions = res.filter(tagGroup => Boolean(tagGroup.children.length));
          this.validateValueValid();
          this.isTagLoading = false;
        },
        error: () => {
          this.isTagLoading = false;
        }
      })
    }
  }

  validateValueValid() {
    if (this._value().tag) {
      let isValid = false;
      this.tagOptions.forEach(tagGroup => {
        const target = tagGroup.children.find(tag => tag.name === this._value().tag);
        if (!isValid) {
          isValid = Boolean(target);
        }
      });
      if (!isValid) {
        this._value().tag = '';
        this.selectChange.emit();
      }
    }
  }

  writeValue(value: any): void {
    this._value.set(value);
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
