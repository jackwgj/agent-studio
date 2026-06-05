import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  TemplateRef,
  ViewChild,
  Inject,
  Optional
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { MODULES } from '@shared/modules';
import { FormControl, FormGroup, Validators, AbstractControl, ValidationErrors, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { cdnAssetUrl } from '../../../../single-spa/assets-url';
import {
  MonacoEditorComponent,
  MonacoEditorConstructionOptions,
  MonacoEditorModule,
} from '@materia-ui/ngx-monaco-editor';
import { EnvironmentVariablesManagementService } from "@routes/platform-management/environment-variables-management/environment-variables-management.service";
import { NzModalModule, NzModalService, NzModalRef } from 'ng-zorro-antd/modal';
import { NzDrawerRef, NZ_DRAWER_DATA } from 'ng-zorro-antd/drawer';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzMessageService } from 'ng-zorro-antd/message';

interface EnvVariable {
  name: string;
  value: string;
  secret: boolean;
}


enum mapKeys {
  required = 'required',
  pattern = 'pattern',
  maxlength = 'maxlength',
  uniqueName = 'uniqueName',
  emptyLine = 'emptyLine',
}

@Component({
  selector: 'config-env-variable',
  templateUrl: './config-env-variable.component.html',
  styleUrls: ['./config-env-variable.component.less'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MODULES,
    MonacoEditorModule,
    NzInputModule,
    NzButtonModule,
    NzCheckboxModule,
    NzAlertModule,
    NzIconModule,
    NzFormModule,
    NzModalModule
  ],
  providers: [
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [
        I18nNamespace.PLATFORM_MANAGEMENT,
        I18nNamespace.COMMON,
        I18nNamespace.PROMPT_PLATFORM,
        I18nNamespace.AGENT_CENTER
      ],
    },
    NzModalService
  ],
})
export class ConfigEnvVariableComponent implements OnInit {
  @Input() id!: string;
  @Input() rowData: any;
  @Output() getList = new EventEmitter<void>();

  @ViewChild('editorCmp') editorCmp!: MonacoEditorComponent;
  @ViewChild('importModal') importModal!: TemplateRef<any>;

  showEdit = true;
  jsonData = '';
  editorOptions: MonacoEditorConstructionOptions = {
    theme: 'vs',
    language: 'json',
    minimap: { enabled: false },
    formatOnPaste: true,
    formatOnType: true,
  };

  envVariables: EnvVariable[] = [];
  envFormGroups: FormGroup[] = [];
  formSubmitted = false;

  private readonly NAME_PATTERN = /^[a-zA-Z_$][a-zA-Z0-9_$]*$/;
  private readonly VALUE_MAX_LENGTH = 2000;
  private readonly NAME_MAX_LENGTH = 64;

  constructor(
    private envVariablesManagement: EnvironmentVariablesManagementService,
    private nzModalService: NzModalService,
    private i18n: I18NextEagerPipe,
    private message: NzMessageService,
    @Optional() private drawerRef: NzDrawerRef,
    @Optional() @Inject(NZ_DRAWER_DATA) public nzData: any
  ) {
    if (this.nzData?.context) {
      this.id = this.nzData.context.id || this.id;
      this.rowData = this.nzData.context.rowData || this.rowData;
      if (this.nzData.context.outputs?.getList) {
        this.getList.subscribe(() => this.nzData.context.outputs.getList());
      }
    }
  }

  ngOnInit(): void {
    this.resetForms();
    if (Array.isArray(this.rowData?.variables) && this.rowData.variables.length > 0) {
      this.rowData.variables.forEach((item: any) => {
        const name = item.name || '';
        const content = item.value?.content || '';
        const secret = item.value?.secret === true;
        this.addFormGroup(name, content, secret);
      });
    }
  }

  private addFormGroup(name: string = '', value: string = '', secret: boolean = false): void {
    const existingIndex = this.envFormGroups.findIndex(
      group => group.get('name')?.value === name
    );

    if (existingIndex !== -1 && name) {
      const formGroup = this.envFormGroups[existingIndex];
      formGroup.get('value')?.setValue(value);
      formGroup.get('secret')?.setValue(secret);
      this.envVariables[existingIndex] = { name, value, secret };
      this.updateUniqueNameValidations();
      return;
    }

    const formGroup = this.createFormGroup(name, value, secret);
    this.envFormGroups.push(formGroup);
    this.envVariables.push({ name, value, secret });

    formGroup.valueChanges.subscribe(({ name: newName, value: newValue, secret: newSecret }) => {
      const index = this.envFormGroups.indexOf(formGroup);
      if (index !== -1) {
        this.envVariables[index] = { name: newName, value: newValue, secret: newSecret };
        if (formGroup.get('name')?.dirty) {
          this.updateUniqueNameValidations();
        }
      }
    });
  }

  createFormGroup(name: string, value: string, secret: boolean = false): FormGroup {
    return new FormGroup({
      name: new FormControl(name, [
        Validators.required,
        Validators.pattern(this.NAME_PATTERN),
        Validators.maxLength(this.NAME_MAX_LENGTH),
        (control: AbstractControl): ValidationErrors | null => this.uniqueNameValidator(control)
      ]),
      value: new FormControl(value, [
        this.maxLengthValidator(this.VALUE_MAX_LENGTH)
      ]),
      secret: new FormControl(secret),
    });
  }

  private maxLengthValidator(maxLength: number) {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value || control.value.length <= maxLength) {
        return null;
      }
      return {
        maxlength: {
          requiredLength: maxLength,
          actualLength: control.value.length,
          message: this.i18n.transform('env_value_max_length', { maxLength: maxLength })
        }
      };
    };
  }

  addVariable(): void {
    const hasEmptyLine = this.envFormGroups.some(group => {
      const nameValue = group.get('name')?.value?.trim() || '';
      const valueValue = group.get('value')?.value?.trim() || '';
      return !nameValue && valueValue === '';
    });

    if (hasEmptyLine) {
      this.message.error(this.i18n.transform('fill_existing_line'));
      return;
    }

    this.addFormGroup();
  }

  removeVariable(index: number): void {
    this.envFormGroups.splice(index, 1);
    this.envVariables.splice(index, 1);
    this.updateUniqueNameValidations();
  }

  onSecretChange(index: number, isSecret: boolean): void {
    const formGroup = this.envFormGroups[index];
    const valueControl = formGroup.get('value');
    if (valueControl) {
      valueControl.updateValueAndValidity();
    }
  }

  clearVariables(): void {
    this.resetForms();
    this.formSubmitted = false;
  }

  private resetForms(): void {
    this.envFormGroups = [];
    this.envVariables = [];
  }

  private uniqueNameValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.parent || !control.value) return null;

    if (!this.NAME_PATTERN.test(control.value)) {
      return null;
    }

    const currentGroup = control.parent as FormGroup;
    const currentIndex = this.envFormGroups.indexOf(currentGroup);

    if (currentIndex === -1) return null;

    const isDuplicate = this.envFormGroups.some((group, idx) => {
      if (idx === currentIndex) return false;
      const nameControl = group.get('name');
      return nameControl && nameControl.value === control.value && nameControl.value !== '';
    });

    return isDuplicate ? { uniqueName: true } : null;
  }

  private updateUniqueNameValidations(): void {
    this.envFormGroups.forEach((group) => {
      const nameControl = group.get('name');
      if (nameControl) {
        nameControl.updateValueAndValidity({ onlySelf: false, emitEvent: false });
      }
    });
  }

  getFormControl(formGroup: FormGroup, controlName: string): FormControl {
    return formGroup.get(controlName) as FormControl;
  }

  private markAllAsTouched(): void {
    this.envFormGroups.forEach((group) => {
      Object.values(group.controls).forEach((ctrl) => {
        ctrl.markAsTouched();
        ctrl.updateValueAndValidity();
      });
    });
  }

  private hasPartialEntries(): boolean {
    let hasPartial = false;
    let hasEmptyLine = false;

    this.envFormGroups.forEach((group) => {
      const nameControl = group.get('name');
      const valueControl = group.get('value');

      if (nameControl && valueControl) {
        const nameValue = nameControl.value?.trim() || '';
        const valueValue = valueControl.value || '';

        if (!nameValue) {
          hasEmptyLine = true;
          nameControl.setErrors({ emptyLine: true });
          nameControl.markAsTouched();
        }
        else if (!nameValue && valueValue !== '') {
          hasPartial = true;
          nameControl.setErrors({ required: true });
          nameControl.markAsTouched();
        } else {
          nameControl.setErrors(null);
          valueControl.setErrors(null);
        }
      }
    });

    return hasPartial || hasEmptyLine;
  }

  private hasExceededLength(): boolean {
    return this.envFormGroups.some(group => {
      const valueControl = group.get('value');
      const value = valueControl?.value || '';
      return value.length > this.VALUE_MAX_LENGTH;
    });
  }

  public isFormValid(): boolean {
    if (this.envFormGroups.length === 0) {
      return true;
    }

    if (this.hasExceededLength()) {
      return false;
    }

    if (this.hasPartialEntries()) {
      return false;
    }

    const validFormGroups = this.envFormGroups.filter(group => {
      const nameValue = group.get('name')?.value?.trim() || '';
      return nameValue;
    });

    if (validFormGroups.length === 0 && this.envFormGroups.length > 0) {
      return false;
    }

    return validFormGroups.every(group => {
      const nameControl = group.get('name');
      const valueControl = group.get('value');
      return nameControl?.valid && valueControl?.valid;
    });
  }

  confirm(): void {
    this.formSubmitted = true;
    this.markAllAsTouched();

    if (this.hasExceededLength()) {
      this.message.error(this.i18n.transform('env_value_too_long'));
      return;
    }

    if (!this.isFormValid()) {
      let errorMessage = '';

      const hasEmptyLine = this.envFormGroups.some(group => {
        const nameValue = group.get('name')?.value?.trim() || '';
        return !nameValue;
      });

      const hasPartial = this.envFormGroups.some(group => {
        const nameValue = group.get('name')?.value?.trim() || '';
        const valueValue = group.get('value')?.value || '';
        return !nameValue && valueValue !== '';
      });

      if (hasEmptyLine) {
        errorMessage = this.i18n.transform('fill_existing_line');
      } else if (hasPartial) {
        errorMessage = this.i18n.transform('variable_name_required');
      } else {
        errorMessage = this.i18n.transform('check_form_error');
      }

      this.message.error(errorMessage);
      return;
    }

    const payload = this.transformArray(this.envVariables);

    this.envVariablesManagement
      .createEnvVariables(payload, this.id)
      .then(() => {
        this.message.success(this.i18n.transform('config_success'));
        this.getList.emit();
      });
  }

  // 3. 修复这里的 close/dismiss，安全回退
  close(): void {
    if (this.nzData?.context?.close) {
      this.nzData.context.close();
    } else if (this.drawerRef) {
      this.drawerRef.close();
    }
  }

  dismiss(): void {
    if (this.nzData?.context?.dismiss) {
      this.nzData.context.dismiss();
    } else if (this.drawerRef) {
      this.drawerRef.close();
    }
  }

  closeConfirm() {
    this.nzModalService.confirm({
      nzTitle: this.i18n.transform('environment_edit_confirm_title'),
      nzContent: this.i18n.transform('environment_edit_confirm_content'),
      nzOkText: this.i18n.transform('ok'),
      nzCancelText: this.i18n.transform('cancel'),
      nzOnOk: () => {
        this.close();
      }
    });
  }

  private transformArray(arr: EnvVariable[]): any[] {
    return arr
      .filter(item => {
        const name = item.name?.trim() || '';
        return name;
      })
      .map((item) => ({
        name: item.name.trim(),
        value: {
          type: 'string',
          content: item.value,
          secret: item.secret,
        },
      }));
  }

  getErrorMessage(formGroup: FormGroup, fieldName: string): string {
    const control = formGroup.get(fieldName);
    if (!control || (!control.touched && !this.formSubmitted) || !control.errors) return '';

    const errors = control.errors;

    if (fieldName === 'name') {
      if (errors[mapKeys.required]) return this.i18n.transform('variable_name_required');
      if (errors[mapKeys.pattern]) return this.i18n.transform('env_variable_verify');
      if (errors[mapKeys.maxlength]) {
        const maxLength = errors[mapKeys.maxlength]?.requiredLength || this.NAME_MAX_LENGTH;
        return this.i18n.transform('max_length_exceeded', { maxLength });
      }
      if (errors[mapKeys.uniqueName]) return this.i18n.transform('variable_names_unique');
      if (errors[mapKeys.emptyLine]) return this.i18n.transform('variable_name_required');
    }

    if (fieldName === 'value') {
      if (errors[mapKeys.maxlength]) {
        const maxLength = errors[mapKeys.maxlength].requiredLength || this.VALUE_MAX_LENGTH;
        return this.i18n.transform('env_value_max_length', { maxLength: maxLength });
      }
    }

    return '';
  }

  openImportModal(): void {
    this.showEdit = true;
    this.nzModalService.create({
      nzTitle: this.i18n.transform('import_and_parse'),
      nzContent: this.importModal,
      nzFooter: null,
      nzWidth: 900
    });
  }

  confirmImport(modalRef: NzModalRef): void {
    try {
      const parsed = JSON.parse(this.jsonData.trim());
      if (
        typeof parsed !== 'object' ||
        parsed === null ||
        Array.isArray(parsed)
      ) {
        throw new Error('Invalid JSON Object');
      }

      for (const [key, value] of Object.entries(parsed)) {
        if (typeof key === 'string') {
          if (!this.NAME_PATTERN.test(key)) {
            continue;
          }

          let stringValue: string;
          if (value == null) {
            stringValue = '';
          } else if (typeof value === 'object') {
            try {
              stringValue = JSON.stringify(value);
            } catch (jsonError) {
              stringValue = String(value);
            }
          } else {
            stringValue = String(value);
          }

          if (stringValue.length > this.VALUE_MAX_LENGTH) {
            const truncatedValue = stringValue.substring(0, this.VALUE_MAX_LENGTH);
            this.updateOrAddVariable(key, truncatedValue);
          } else {
            this.updateOrAddVariable(key, stringValue);
          }
        }
      }

      this.updateUniqueNameValidations();
      modalRef.destroy();
      this.jsonData = '';
    } catch (error) {
      this.message.error(this.i18n.transform('enter_correct_json_content'));
    }
  }

  private updateOrAddVariable(key: string, value: string): void {
    const existingIndex = this.envFormGroups.findIndex(
      (group) => group.get('name')?.value === key
    );

    if (existingIndex !== -1) {
      const formGroup = this.envFormGroups[existingIndex];
      formGroup.get('value')?.setValue(value);
      this.envVariables[existingIndex] = {
        ...this.envVariables[existingIndex],
        value: value
      };
    } else {
      this.addFormGroup(key, value, false);
    }
  }

  protected readonly changeUrl = cdnAssetUrl;
}
