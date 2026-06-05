import { Directive, forwardRef, Input } from '@angular/core';
import {
  AbstractControl,
  NG_VALIDATORS,
  ValidationErrors,
  Validator,
} from '@angular/forms';
import i18next from 'i18next';
import { type IWorkflowField } from '@routes/agent-center/app-flow/node.type';
import { capitalize } from 'lodash';

@Directive({
  selector: '[arrTypeValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => ArrTypeValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class ArrTypeValidatorDirective implements Validator {
  @Input('param') param: IWorkflowField;

  validate(control: AbstractControl): ValidationErrors | null {
    if (control?.value) {
      try {
        const passibleArr = JSON.parse(control.value as string);

        if (Array.isArray(passibleArr)) {
          if (passibleArr.length === 0) {
            return null;
          }

          const { type } = this.param.schema as IWorkflowField;
          if(type === 'any') {
            return null;
          }
          const isValid = passibleArr.every((element) => {
            if (type === 'integer' && typeof element === 'number') {
              return true;
            }

            if (typeof element === type) {
              return true;
            }

            return false;
          });

          if (isValid) {
            return null;
          }

          return {
            arrErr: {
              msg: i18next.t('validator_array_type', {
                type: capitalize(type),
              }),
            },
          };
        } else {
          return {
            arrErr: {
              msg: i18next.t('validator_array'),
            },
          };
        }
      } catch {
        return {
          arrErr: {
            msg: i18next.t('validator_array'),
          },
        };
      }
    }

    return null;
  }
}
