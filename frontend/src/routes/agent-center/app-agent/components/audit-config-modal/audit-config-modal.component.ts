import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  ViewChild,
  Output,
  EventEmitter,
  Input,
  ChangeDetectorRef,
  OnChanges,
  SimpleChanges,
  OnDestroy
} from "@angular/core";
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidatorFn
} from "@angular/forms";
import { NzDrawerRef } from "ng-zorro-antd/drawer";
import { NzFormModule } from 'ng-zorro-antd/form';
import { MODULES } from "@shared/modules";
import { Subscription } from "rxjs";
import { v4 as uuidV4 } from "uuid";
import { I18NEXT_NAMESPACE } from "angular-i18next";
import { I18nNamespace } from "@i18n";
import { I18NextEagerPipe } from "angular-i18next";

const enablePropMap = {
  input: "inputEnable",
  output: "outputEnable"
};

@Component({
  selector: "audit-config-halfmodal",
  templateUrl: "./audit-config-modal.component.html",
  styleUrls: ["./audit-config-modal.component.less"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [MODULES, NzFormModule],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER, I18nNamespace.AGENT]
    }
  ]
})
export class AuditConfigModalComponent implements OnInit, OnChanges, OnDestroy {
  @ViewChild("auditConfigDrawer", { static: true })
  auditConfigDrawer: NzDrawerRef;

  @Input() auditConfig = {
    enabled: true,
    filter: {
      keywords: ""
    },
    replace: [],
    reply: []
  };

  @Output() onSaveAuditConfig = new EventEmitter<any>();

  constructor(
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private i18n: I18NextEagerPipe
  ) {
    this.groupFormControl = fb.group({
      filter: new FormControl("", [this.noDuplicateKeywordsValidator()]),
      replace: this.fb.array([]),
      handling: this.fb.array([])
    });

    this.valueChangesSubscription =
      this.groupFormControl.valueChanges.subscribe(() => {
        if (!this.isUpdating) {
          this.validateAllControls();
        }
        this.updateTabInvalidStatus();
      });
  }

  public tabs = [
    {
      title: this.i18n.transform("filtering"),
      active: true,
      invalid: false
    },
    {
      title: this.i18n.transform("replaces"),
      active: false,
      invalid: false
    },
    {
      title: this.i18n.transform("bottom_reply"),
      active: false,
      invalid: false
    }
  ];

  public groupFormControl: FormGroup;

  private isUpdating = false;

  public tempPropValues = [];

  private valueChangesSubscription: Subscription;

  public open() {
    this.auditConfigDrawer.open();
  }

  public dismiss(): void {
    this.resetFormToAuditConfig();
    this.auditConfigDrawer.close();
  }

  public saveAuditConfig(): void {
    if (this.groupFormControl.invalid) {
      this.groupFormControl.markAllAsTouched();
      this.cdr.markForCheck();
      return;
    }
    const { filter, replace, handling } = this.groupFormControl.controls;
    const params = {
      enabled: true,
      filter: {
        keywords: filter.value
      },
      replace: [
        ...replace.value.map((item) => ({
          keywords: item.keywords,
          replace: item.replaceWords
        }))
      ],
      reply: [
        ...handling.value.map((item, index) => {
          return {
            keywords: item.keywords,
            input_text: item.input ?? this.tempPropValues[index].input,
            output_text: item.output ?? this.tempPropValues[index].output,
            input_enable: item.inputEnable,
            output_enable: item.outputEnable
          };
        })
      ]
    };
    this.onSaveAuditConfig.emit(params);
    this.auditConfigDrawer.close();
  }

  public addReplaceItem() {
    const control = this.groupFormControl.get("replace") as FormArray;
    if (control.length >= 10) {
      return;
    }
    control.push(this.newReplaceItem());
  }

  public removeReplaceItem(index: number) {
    const control = this.groupFormControl.get("replace") as FormArray;
    control.removeAt(index);
  }

  public addHandlingItem() {
    const control = this.groupFormControl.get("handling") as FormArray;
    if (control.length >= 10) {
      return;
    }
    const item = this.newHandlingItem();
    control.push(item);
    this.tempPropValues.push({ input: "", output: "" });
    this.onCheckboxChange(item, "input");
    this.onCheckboxChange(item, "output");
  }

  public removeHandlingItem(index: number) {
    const control = this.groupFormControl.get("handling") as FormArray;
    control.removeAt(index);
    this.tempPropValues.splice(index, 1);
  }

  public getFormArray(paramName: string): FormArray {
    const control = this.groupFormControl.get(paramName);
    if (control instanceof FormArray) {
      return control;
    }
    throw new Error(`${paramName} control is not a FormArray`);
  }

  public getFormControl(item: AbstractControl, key: string): FormControl {
    return item.get(key) as FormControl;
  }

  private newReplaceItem(): FormGroup {
    return this.fb.group({
      uuid: uuidV4(),
      keywords: [
        "",
        [this.requiredValidator, this.noDuplicateKeywordsValidator()]
      ],
      replaceWords: ["", [this.requiredValidator]]
    });
  }

  private newHandlingItem(): FormGroup {
    return this.fb.group({
      uuid: uuidV4(),
      keywords: [
        "",
        [this.requiredValidator, this.noDuplicateKeywordsValidator()]
      ],
      input: ["", [this.handlingContentValidator("input")]],
      inputEnable: [false],
      output: ["", [this.handlingContentValidator("output")]],
      outputEnable: [false]
    });
  }

  private requiredValidator: ValidatorFn = (control: AbstractControl) => {
    if (!control.value || control.value.trim() === "") {
      return { required: true };
    }
    return null;
  };

  private noDuplicateKeywordsValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      if (!control.value) {
        return null;
      }
      const keywords = control.value.split(",").map((kw) => kw.trim());
      const allKeywords = this.getAllKeywords(control);
      const isDuplicate = keywords.some((keyword) => {
        return (
          keyword &&
          (keywords.filter((kw) => kw === keyword).length > 1 ||
            (allKeywords.includes(keyword) &&
              !keywords.includes(keyword, keywords.indexOf(keyword) + 1)))
        );
      });
      if (isDuplicate) {
        control.markAsTouched();
      }
      return isDuplicate
        ? {
          serviceName: {
            tiErrorMessage: this.i18n.transform("keyword_repetition")
          }
        }
        : null;
    };
  }

  private handlingContentValidator(prop: string) {
    return (control: AbstractControl): { [key: string]: any } | null => {
      if (control?.value || !control?.parent?.value[enablePropMap[prop]]) {
        return null;
      } else {
        return { empty: true };
      }
    };
  }

  private getAllKeywords(control: AbstractControl): string[] {
    const controlName = Object.keys(this.groupFormControl?.controls ?? []).find(
      (name) => this.groupFormControl.get(name) === control
    );
    const allKeywords: string[] = [];

    if (
      this.groupFormControl?.get("filter").value &&
      controlName !== "filter"
    ) {
      allKeywords.push(
        ...this.groupFormControl
          .get("filter")
          .value.split(",")
          .map((kw) => kw.trim())
      );
    }
    this.groupFormControl?.get("replace").value.forEach((item) => {
      if (item.uuid !== control?.parent?.value?.uuid) {
        allKeywords.push(...item.keywords.split(",").map((kw) => kw.trim()));
      }
    });

    this.groupFormControl?.get("handling").value.forEach((item) => {
      if (item.uuid !== control?.parent?.value?.uuid) {
        allKeywords.push(...item.keywords.split(",").map((kw) => kw.trim()));
      }
    });
    return allKeywords;
  }

  private updateTabInvalidStatus() {
    this.tabs[0].invalid = this.groupFormControl.get("filter").invalid;
    this.tabs[1].invalid = this.groupFormControl.get("replace").invalid;
    this.tabs[2].invalid = this.groupFormControl.get("handling").invalid;
  }

  private validateAllControls() {
    this.isUpdating = true;
    const controls = this.groupFormControl.controls;
    Object.keys(controls).forEach((name) => {
      const control = controls[name] as AbstractControl;
      if (control instanceof FormGroup) {
        this.validateFormGroup(control);
      } else if (control instanceof FormArray) {
        this.validateFormArray(control);
      } else {
        control.updateValueAndValidity();
      }
    });
    this.updateTabInvalidStatus();
    this.isUpdating = false;
  }

  private validateFormGroup(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach((name) => {
      const control = formGroup.get(name) as AbstractControl;
      if (control instanceof FormGroup) {
        this.validateFormGroup(control);
      } else if (control instanceof FormArray) {
        this.validateFormArray(control);
      } else {
        control.updateValueAndValidity();
      }
    });
  }

  private validateFormArray(formArray: FormArray) {
    formArray.controls.forEach((control) => {
      if (control instanceof FormGroup) {
        this.validateFormGroup(control);
      } else if (control instanceof FormArray) {
        this.validateFormArray(control);
      } else {
        control.updateValueAndValidity();
      }
    });
  }

  public onCheckboxChange(item: AbstractControl, prop: string, index?: number) {
    const checkProp = enablePropMap[prop];
    const inputDisabledControl = this.getFormControl(item, checkProp);
    const inputControl = this.getFormControl(item, prop);
    const currentValue = inputControl.value;
    if (index !== undefined) {
      this.tempPropValues[index] = this.tempPropValues[index] || {};
      this.tempPropValues[index][prop] = currentValue;
    }
    if (inputDisabledControl.value) {
      inputControl.enable();
    } else {
      inputControl.disable();
    }
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes?.auditConfig) {
      this.resetFormToAuditConfig();
    }
  }

  private resetFormToAuditConfig() {
    if (!this.auditConfig) {
      return;
    }
    const { filter, replace, reply } = this.auditConfig;
    this.groupFormControl.patchValue({
      filter: filter?.keywords || ""
    });

    if (replace && Array.isArray(replace)) {
      const replaceArray = this.groupFormControl.get(
        "replace"
      ) as FormArray;
      replaceArray.clear();
      replace.forEach((item) => {
        replaceArray.push(
          this.fb.group({
            uuid: item.uuid || uuidV4(),
            keywords: [
              item.keywords || "",
              [this.requiredValidator, this.noDuplicateKeywordsValidator()]
            ],
            replaceWords: [item.replace || "", [this.requiredValidator]]
          })
        );
      });
    }

    if (reply && Array.isArray(reply)) {
      const handlingArray = this.groupFormControl.get(
        "handling"
      ) as FormArray;
      handlingArray.clear();
      this.tempPropValues = [];
      reply.forEach((item, index) => {
        const newItem = this.fb.group({
          uuid: item.uuid || uuidV4(),
          keywords: [
            item.keywords || "",
            [this.requiredValidator, this.noDuplicateKeywordsValidator()]
          ],
          input: [
            item.input_text || "",
            [this.handlingContentValidator("input")]
          ],
          inputEnable: item.input_enable || false,
          output: [
            item.output_text || "",
            [this.handlingContentValidator("output")]
          ],
          outputEnable: item.output_enable || false
        });
        handlingArray.push(newItem);
        this.tempPropValues.push({
          input: "",
          output: ""
        });
        this.onCheckboxChange(newItem, "input", index);
        this.onCheckboxChange(newItem, "output", index);
      });
    }
    this.cdr.detectChanges();
  }

  get isReplaceArrayLimit() {
    return (this.groupFormControl.get("replace") as FormArray).length >= 10;
  }

  get isHandlingArrayLimit() {
    return (this.groupFormControl.get("handling") as FormArray).length >= 10;
  }

  ngOnDestroy(): void {
    if (this.valueChangesSubscription) {
      this.valueChangesSubscription.unsubscribe();
    }
  }
}
