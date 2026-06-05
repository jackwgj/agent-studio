import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import {
  ClickedOptionParams,
  DisplayFilterOption,
  DisplayGroupInfo,
  FilterGroupInfo,
  FilterOption,
  MultiSearchParam
} from "@routes/model-square/common.interface";
import {I18NEXT_NAMESPACE, I18NextEagerPipe} from "angular-i18next";
import {COMMON_MODULES, MODULES} from "@shared/modules";
import {I18nNamespace} from "@i18n";
import {ModelSquareSubService} from "@routes/model-square/model-square-sub.service";
import {Subject, takeUntil} from "rxjs";

export interface VerticalFilterProp {
  title: string;
  filterGroupInfo: Array<FilterGroupInfo>;
}

@Component({
  selector: 'vertical-filter',
  templateUrl: './vertical-filter.component.html',
  styleUrl: './vertical-filter.component.less',
  standalone: true,
  imports: [COMMON_MODULES, MODULES],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.JIUWEN_MODEL],
    },
  ],
})

export class VerticalFilterComponent {
  private destroy$ = new Subject<void>();
  @Input() optionList: VerticalFilterProp;
  @Input() isMultiSelect: boolean;

  @Output() onOptionClick: EventEmitter<ClickedOptionParams | any> = new EventEmitter();

  public displayData: Array<DisplayGroupInfo> = [];
  public filterTitle = '';
  public showClear = false;

  constructor(
    public router: Router,
    public i18n: I18NextEagerPipe,
    public modelSquareSubService:ModelSquareSubService,
  ) {
    this.modelSquareSubService
      .clearUpdate$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value: boolean) => {
        if (value) {
          this.clear()
        }
      });
  }


  getCurSelect(): MultiSearchParam[] {
    const searchParams: MultiSearchParam[] = [];
    this.displayData.forEach(data => {
      const values = data.options.filter(option => option.isSelect).map(option => option.value);
      searchParams.push({ groupType: data.groupType, value: values });
    });
    return searchParams;
  }

  clear(): void {
    this.displayData.forEach(data => {
      data.options.forEach(option => (option.isSelect = false));
    });
    this.onOptionClick.emit([]);
    this.showClear = false;
  }

  public onGroupOptionClick(selectOption: DisplayFilterOption): void {
    const { value, groupType, isSelect } = selectOption;
    if (this.isMultiSelect) {
      selectOption.isSelect = !isSelect;
      this.onOptionClick.emit(this.getCurSelect());
      this.setClearState();
      return;
    }
    this.updateActiveState(value, groupType);
    this.onOptionClick.emit({
      value,
      groupType,
    });
  }

  ngOnInit(): void {
    this.initData();
  }

  private updateActiveState(selectValue: string, selectedGroupType: string): void {
    const selectedGroup = this.displayData.find(filterGroup => filterGroup.groupType === selectedGroupType);
    selectedGroup.options.forEach(option => {
      option.isSelect = option.value === selectValue;
    });
  }

  private generateDisplayGroupOption(defaultActiveValue: string, groupType: string, options: Array<FilterOption>): Array<DisplayFilterOption> {
    const displayOptions: Array<DisplayFilterOption> = [];
    const defaultActiveValueList = defaultActiveValue.split('|').map(value => value.trim());
    options.forEach(option => {
      const displayOption: DisplayFilterOption = {
        label: option.label,
        value: option.value,
        groupType,
        isSelect: this.isMultiSelect ? defaultActiveValueList.includes(option.value.split('|')[0].trim()) : option.value === defaultActiveValue,
      };
      displayOptions.push(displayOption);
    });

    return displayOptions;
  }

  private setClearState(): void {
    if (!this.isMultiSelect) {
      this.showClear = false;
      return;
    }

    const curSelectData = this.displayData.find(data => {
      const selectOption = data.options.find(option => option.isSelect);
      return !!selectOption;
    });
    this.showClear = !!curSelectData;
  }

  private initData(): void {
    this.filterTitle = this.optionList.title;
    this.updateDisplayGroups();
    this.setClearState();
  }

  private updateDisplayGroups(): void {
    this.displayData = [];
    this.optionList.filterGroupInfo.forEach(groupInfo => {
      const defaultActiveValue = groupInfo.defaultActiveValue ?? groupInfo.options[0].value;
      const { subtitle, groupType, options } = groupInfo;
      const displayOptions = this.generateDisplayGroupOption(defaultActiveValue, groupType, options);
      const displayGroup: DisplayGroupInfo = {
        groupTitle: subtitle || '',
        groupType,
        options: displayOptions,
      };
      this.displayData.push(displayGroup);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    // 初始化时的onChange不执行更新
    if (changes.optionList) {
      this.updateDisplayGroups();
    }

  }
  get selectedNum() {
    let num = 0;
    this.displayData.forEach(data => {
      num += data.options.filter(option => option.isSelect).length;
    });
    return num;
  }
}
