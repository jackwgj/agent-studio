import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MODULES } from '@shared/modules';
import * as scenarioGuideModalConstant from '../../scenario-guide-modal.constant';
import { I18NEXT_NAMESPACE } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { Subject, takeUntil } from 'rxjs';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'dependency-item',
  templateUrl: './dependency-item.component.html',
  styleUrls: ['./dependency-item.component.scss'],
  standalone: true,
  imports: [
    MODULES,
    ReactiveFormsModule,
    NzFormModule,
    NzSelectModule,
    NzInputModule,
    NzTagModule,
    NzAlertModule,
    NzIconModule,
    NzToolTipModule,
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.COMMON],
    },
  ],
})
export class DependencyItemComponent implements OnInit, OnDestroy, OnChanges {
  @Input() dependencyDataItem: any;
  @Input() formGroupItem: FormGroup | any;
  @Input() itemIndex: number;
  @Input() dependencyFormConfig: scenarioGuideModalConstant.FormConfig;
  @Input() dependencyOptions: Array<any> = [];
  @Input() highlightedCycleRows = new Set<number>();
  @Output() deleteDependencyEvent = new EventEmitter<number>();
  protected SCENARIO_FIELD_ID = scenarioGuideModalConstant.SCENARIO_FIELD_ID;
  protected FIELD_TYPE = scenarioGuideModalConstant.FIELD_TYPE;
  public options: Array<any> = [];
  public isHigherDependentLower = false;
  private ngUnsubscribe = new Subject<void>();

  ngOnInit(): void {
    this.updateHigherDependentLower(this.formGroupItem.value);
    this.formGroupItem?.controls?.[
      this.SCENARIO_FIELD_ID.DEPEND_ITEM
    ]?.valueChanges
      ?.pipe(takeUntil(this.ngUnsubscribe))
      .subscribe((data) => {
        const aId = data?.id;
        const bId =
          this.formGroupItem?.controls?.[
            this.SCENARIO_FIELD_ID.BE_DEPENDED_ITEM
          ]?.value?.id;
        if (bId && aId === bId) {
          this.formGroupItem.patchValue({
            [this.SCENARIO_FIELD_ID.BE_DEPENDED_ITEM]: undefined,
          });
        }
      });

    this.formGroupItem.valueChanges
      ?.pipe(takeUntil(this.ngUnsubscribe))
      .subscribe((data) => {
        this.updateHigherDependentLower(data);
      });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.dependencyOptions) {
      if (
        JSON.stringify(changes.dependencyOptions.previousValue) ===
        JSON.stringify(changes.dependencyOptions.currentValue)
      ) {
        return;
      }
      const originValue = this.formGroupItem.value;
      const newDependValue = this.dependencyOptions.find(
        (item) =>
          item.id === originValue[this.SCENARIO_FIELD_ID.DEPEND_ITEM]?.id,
      );
      const newBeDependedValue = this.dependencyOptions.find(
        (item) =>
          item.id === originValue[this.SCENARIO_FIELD_ID.BE_DEPENDED_ITEM]?.id,
      );
      this.formGroupItem.patchValue({
        [this.SCENARIO_FIELD_ID.DEPEND_ITEM]: newDependValue,
        [this.SCENARIO_FIELD_ID.BE_DEPENDED_ITEM]: newBeDependedValue,
      });
    }
  }

  public deleteDependency(): void {
    this.deleteDependencyEvent.emit(this.itemIndex);
  }

  public isRowInCycle(index: number): boolean {
    return this.highlightedCycleRows.has(index);
  }

  private updateHigherDependentLower(data): void {
    this.isHigherDependentLower =
      data[this.SCENARIO_FIELD_ID.DEPEND_ITEM]?.priority <
      data[this.SCENARIO_FIELD_ID.BE_DEPENDED_ITEM]?.priority;
  }

  trackByIndex(index: number): number {
    return index;
  }

  compareWith(o1: any, o2: any): boolean {
    return o1?.id === o2?.id;
  }
}
