import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Router} from '@angular/router';
import {VerticalFilterComponent} from '@routes/model-square/components/vertical-filter/vertical-filter.component';
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from 'angular-i18next';
import {FilterGroupInfo} from '@routes/model-square/common.interface';
import {I18nNamespace} from '@i18n';
export interface VerticalFilterProp {
  title: string;
  filterGroupInfo: Array<FilterGroupInfo>;
}
@Component({
  selector: 'model-filter',
  templateUrl: './model-filter.component.html',
  styleUrl: './model-filter.component.less',
  standalone: true,
  imports: [VerticalFilterComponent],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.JIUWEN_MODEL],
    },
  ],
})


export class ModelFilterComponent {
  @Input() searchItems: Array<any>;
  @Input() searchTags: Array<any>; // 初始选中值
  @Output() onFilter = new EventEmitter<object>();
  public srcData: VerticalFilterProp;

  constructor(public router: Router, public i18n: I18NextEagerPipe) {
  }

  ngOnChanges(changes){
    if(changes.searchItems?.currentValue){
      this.searchItems = changes.searchItems.currentValue
      this.ngOnInit()
    }
  }
  public onOptionClick(datas: any[]): void {
    const activeValue = {};
    datas.forEach(data => {
      activeValue[data.groupType] = data.value?.join('|');
    });
    this.onFilter.emit(activeValue);
  }

  ngOnInit(): void {
    const filterGroupInfo = this.searchItems.map(searchItem => {
      const curSearchTag = this.searchTags.find(searchTag => searchTag.field === searchItem.field);
      return {
        groupType: searchItem.field,
        defaultActiveValue: curSearchTag?.value || '',
        subtitle: searchItem.label,
        options: searchItem.options.map(option => ({
          label: option.label,
          value: option.id,
        })),
      };
    });
    this.srcData = {
      title: this.i18n.transform('square_filter'),
      filterGroupInfo,
    };
  }
}
