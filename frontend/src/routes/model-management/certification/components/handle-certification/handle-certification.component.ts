import { Component, ElementRef, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MODULES } from '@shared/modules';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { ModelManagementService } from '@services/repositories/model-management-new';
import type { modelType } from '@routes/model-management/utils';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
  ReactiveFormsModule
} from '@angular/forms';
import { CommonValidation } from '@shared/validation/commonValidation';
import { CertificationTipComponent } from '@routes/model-management/certification/components/certification-tip/certification-tip.component';
import { NzModalRef, NzModalService, NzModalModule } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'meta-handle-certification',
  standalone: true,
  imports: [
    CommonModule,
    MODULES,
    ReactiveFormsModule,
    NzModalModule,
    NzButtonModule,
    NzFormModule,
    NzInputModule
  ],
  templateUrl: './handle-certification.component.html',
  styleUrls: ['./handle-certification.component.scss'],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.MODEL_ACCESS],
    },
  ],
})
export class HandleCertificationComponent implements OnInit {
  @Input() type: modelType = 'create';
  @Input() name?: string = '';
  @Input() id?: string = '';
  @Input() close?: () => void;

  myForm: FormGroup;

  constructor(
    private modelManagementService: ModelManagementService,
    private modalRef: NzModalRef,
    private nzModal: NzModalService,
    private i18n: I18NextEagerPipe,
    private fb: FormBuilder,
    public elementRef: ElementRef,
    private message: NzMessageService,
  ) {
    this.myForm = this.fb.group({
      apikey_name: new FormControl('', [
        Validators.required,
        CommonValidation.validAPIKEYVerify(
          this.i18n.transform('validate_name_format'),
        ),
        Validators.minLength(1),
        Validators.maxLength(100)
      ]),
      apikey_description: new FormControl('', [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.minLength(1),
        Validators.maxLength(1024),
      ]),
    });
  }

  ngOnInit() {}

  handelAuth() {
    if (this.myForm.invalid) {
      Object.values(this.myForm.controls).forEach(control => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
      const firstErrorKey = Object.keys(this.myForm.controls).find(key => this.myForm.get(key)?.invalid);
      if (firstErrorKey) {
        const el = this.elementRef.nativeElement.querySelector(`[formcontrolname="${firstErrorKey}"]`);
        if (el) el.focus();
      }
      return;
    }

    const value = this.myForm.getRawValue();

    this.modelManagementService
      .postApiAuth(value)
      .then((res: any) => {
        this.message.success(
          this.i18n.transform('api_key_create_successfully'),
        );
        this.executeClose();
        setTimeout(() => {
          this.nzModal.create({
            nzContent: CertificationTipComponent,
            nzFooter: null,
            nzWidth: 400,
            nzData: {
              apiKey: res.apikey,
            }
          });
        }, 1000);
      })
      .catch((error) => {
        this.message.error(this.i18n.transform('auth_config_failure'));
      });
  }

  executeClose(): void {
    if (this.close) {
      this.close();
    }
    this.modalRef.destroy();
  }

  dismiss(): void {
    this.modalRef.destroy();
  }
}
