import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { CommonValidation } from '@shared/validation/commonValidation';

@Component({
  selector: 'edit-agent-name',
  imports: [CommonModule, MODULES],
  templateUrl: './edit-agent-name.component.html',
  styleUrls: ['./edit-agent-name.component.less'],
  standalone: true,
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MODEL_ACCESS, I18nNamespace.AGENT_CENTER],
    },
  ],
})
export class EditAgentNameComponent {
  @Output() editName = new EventEmitter<string>();
  @Input() agentName = '';

  constructor(
    private fb: FormBuilder,
    private i18n: I18NextEagerPipe,
    private elementRef: ElementRef,
  ) {
    this.form = this.fb.group({
      name: [
        '',
        [
          Validators.required,
          CommonValidation.wfNameVerify(
            this.i18n.transform('variable_validator5'),
          ),
        ],
      ],
    });
  }

  public form: FormGroup;
  loading = false;

  dismiss(): void {}

  close(): void {}

  ngOnInit(): void {
    this.form.setValue({
      name: this.agentName,
    });
  }

  editAgentName(modalRef) {
    if (!this.form.valid) {
      Object.keys(this.form.controls).forEach(key => {
        const control = this.form.get(key);
        if (control?.invalid) {
          this.elementRef.nativeElement
            .querySelector(`[formControlName=${key}]`)
            ?.focus();
          return;
        }
      });
      this.form.markAllAsTouched();
      return;
    }
    this.editName.emit(this.form.get('name')?.value);
    modalRef.destroy();
  }
}
