import { Directive, forwardRef } from '@angular/core';
import {
  AbstractControl,
  NG_VALIDATORS,
  ValidationErrors,
  Validator,
} from '@angular/forms';
import { I18nNamespace } from '@i18n';
import { I18NEXT_NAMESPACE, I18NextEagerPipe } from 'angular-i18next';

@Directive({
  selector: '[objTypeValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => ObjTypeValidatorDirective),
      multi: true,
    },
    {
      provide: I18NEXT_NAMESPACE,
      useValue: [I18nNamespace.AGENT_CENTER],
    },
  ],
  standalone: true,
})
export class ObjTypeValidatorDirective implements Validator {
  constructor(private i18n: I18NextEagerPipe) {}

  validate(control: AbstractControl): ValidationErrors | null {
    if (control?.value) {
      try {
        const passibleObj = JSON.parse(control.value as string);

        if (typeof passibleObj !== 'object' || Array.isArray(passibleObj)) {
          return {
            arrErr: {
              msg: this.i18n.transform('obj_type_validate_msg1'),
            },
          };
        }

        return null;
      } catch {
        return {
          arrErr: {
            msg: this.i18n.transform('obj_type_validate_msg2'),
          },
        };
      }
    }

    return null;
  }
}
