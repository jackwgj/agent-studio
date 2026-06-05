import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import i18next from 'i18next';
import { MODULES } from '@shared/modules';
import { FlowUtils } from '../../utils/flow-utils';

@Component({
  selector: 'node-description',
  templateUrl: './node-description.component.html',
  styleUrls: ['./node-description.component.less'],
  standalone: true,
  imports: [MODULES],
})
export class NodeDescriptionComponent implements OnInit {
  @Input() nodeInfo!: any;
  @Input() originDescription?: string;
  @Output() descriptionChange = new EventEmitter<any>();

  public descriptionForm = this.fb.group({
    description: new FormControl('', [this.notEmptyValid(), Validators.maxLength(256)]),
  });

  public isEdit = false;

  public description = '';

  constructor(private fb: FormBuilder) {}

  ngOnInit() {}

  initDescription() {
    const defaultDescription = FlowUtils.nodeDesci18nMap(this.nodeInfo?.type);
    const description = this.nodeInfo?.configs?.descriptionText || this.originDescription || defaultDescription;
    this.description = description;
    this.descriptionForm.controls.description.setValue(description);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if(changes) {
      this.initDescription();
    }
  }

  private notEmptyValid(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const description = control?.value || '';
      if (!description || description.trim().length === 0) {
        return {
          interfaceValidate: {
            value: control.value,
            tiErrorMessage: i18next.t('input_cannot_be_empty'),
          },
        };
      }
      return null;
    };
  }

  public changeEdit() {
    this.isEdit = true;
  }

  public onBlur(): void {
    this.descriptionForm.markAllAsTouched();
    if (this.descriptionForm.invalid) {
      return;
    }

    const defaultDescription = FlowUtils.nodeDesci18nMap(this.nodeInfo?.type);
    let description = this.descriptionForm.controls.description.value?.trim();
    this.description = description;
    if(description === defaultDescription || description === this.originDescription) {
      description = '';
    }
    this.nodeInfo.configs = {
      ...this.nodeInfo.configs,
      descriptionText: description
    }

    this.isEdit = false;
    this.descriptionChange.emit();
  }
}
