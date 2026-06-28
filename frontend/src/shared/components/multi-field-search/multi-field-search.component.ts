import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { I18NextModule, I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';

export interface ISearchFieldOption {
  label: string;
  id: string;
}

export interface ISearchField {
  label: string;
  field: string;
  options?: ISearchFieldOption[];
}

export interface ISearchTag {
  field: string;
  value: string;
  id?: string;
  label: string;
}

@Component({
  selector: 'multi-field-search',
  templateUrl: './multi-field-search.component.html',
  styleUrls: ['./multi-field-search.component.less'],
  standalone: true,
  imports: [CommonModule, FormsModule, NzSelectModule, NzTagModule, NzIconModule, NzToolTipModule, I18NextModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER]
    }
  ]
})
export class MultiFieldSearchComponent {
  @Input() searchItems: ISearchField[] = [];
  @Input() searchTags: ISearchTag[] = [];
  @Input() searchField: string = 'name';
  @Input() width: string = '100%';
  @Output() searchTagsChange = new EventEmitter<ISearchTag[]>();
  @Output() searchFieldChange = new EventEmitter<string>();
  @Output() searchChange = new EventEmitter<ISearchTag[]>();

  public searchInputValue: string = '';

  public get currentFieldHasOptions(): boolean {
    const item = this.searchItems.find(i => i.field === this.searchField);
    return !!item?.options?.length;
  }

  public get currentFieldOptions(): ISearchFieldOption[] {
    const item = this.searchItems.find(i => i.field === this.searchField);
    return item?.options || [];
  }

  public get selectedOptionLabel(): string {
    if (!this.searchInputValue) return '';
    const opt = this.currentFieldOptions.find(o => o.id === this.searchInputValue);
    return opt?.label || '';
  }

  public addSearchTag() {
    const item = this.searchItems.find(i => i.field === this.searchField);
    if (!item || !this.searchInputValue?.trim()) return;
    const existing = this.searchTags.find(t => t.field === this.searchField);
    if (existing) {
      existing.value = this.searchInputValue.trim();
    } else {
      this.searchTags = [...this.searchTags, { field: this.searchField, value: this.searchInputValue.trim(), label: item.label }];
    }
    this.searchInputValue = '';
    this.emitChange();
  }

  public addSearchTagFromOption(optionId: string) {
    const item = this.searchItems.find(i => i.field === this.searchField);
    if (!item || !optionId) return;
    const opt = item.options?.find(o => o.id === optionId);
    if (!opt) return;
    const existing = this.searchTags.find(t => t.field === this.searchField);
    if (existing) {
      existing.value = opt.label;
      existing.id = opt.id;
    } else {
      this.searchTags = [...this.searchTags, { field: this.searchField, value: opt.label, id: opt.id, label: item.label }];
    }
    this.searchInputValue = '';
    this.emitChange();
  }

  public removeSearchTag(tag: ISearchTag) {
    this.searchTags = this.searchTags.filter(t => t !== tag);
    this.emitChange();
  }

  public clearSearch() {
    this.searchTags = [];
    this.searchInputValue = '';
    this.searchField = this.searchItems[0]?.field || '';
    this.searchFieldChange.emit(this.searchField);
    this.emitChange();
  }

  public onSearchFieldChange(field: string) {
    this.searchField = field;
    this.searchInputValue = '';
    this.searchFieldChange.emit(field);
  }

  private emitChange() {
    this.searchTagsChange.emit(this.searchTags);
    this.searchChange.emit(this.searchTags);
  }
}
