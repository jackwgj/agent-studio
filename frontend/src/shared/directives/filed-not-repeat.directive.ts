import { Directive, forwardRef, Input } from "@angular/core";
import { AbstractControl, NG_VALIDATORS, ValidationErrors, Validator } from "@angular/forms";
import { TreeFormItem } from '@routes/agent-center/app-plugin/plugin-action-detail/custom-connectors-models/connectors-model';
import i18next from 'i18next';

@Directive({
  selector: "[filedNotRepeat][ngModel],[filedNotRepeat][formControl],[filedNotRepeat][formControlName]",
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => FiledNotRepeatDirective),
      multi: true,
    },
  ],
})
export class FiledNotRepeatDirective implements Validator {
  @Input() filedNotRepeat: TreeFormItem[];
  @Input() currentNode: TreeFormItem;
  @Input() isFunctionArg: boolean;
  @Input() validatorKey: string = "name";
  @Input() errorMessage: string = i18next.t('name_cannot_be_duplicate');

  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value) {
      if (!/^(?![-()])\S{1,128}$/.test(control.value)) {
        return {
          repeat: {
            tiErrorMessage: i18next.t('name_length_limit_and_invalid_start_chars'),
            requiredValue: null,
            actualValue: control.value,
          },
        };
      } else if (control.value.indexOf("[]") > -1) {
        return {
          repeat: {
            tiErrorMessage: i18next.t('cannot_contain_consecutive_brackets'),
            requiredValue: null,
            actualValue: control.value,
          },
        };
      } else if (control.value === "mssiAuthData") {
        return {
          repeat: {
            tiErrorMessage: i18next.t('cannot_be_mssi_auth_data'),
            requiredValue: null,
            actualValue: control.value,
          },
        };
      } else {
        if (this.getIsRepeat(this.filedNotRepeat, control.value, this.currentNode)) {
          return {
            repeat: {
              tiErrorMessage: this.errorMessage,
              requiredValue: this.filedNotRepeat,
              actualValue: control.value,
            },
          };
        } else {
          return null;
        }
      }
    } else {
      return null;
    }
  }

  getIsRepeat(treeData: TreeFormItem[], controlValue: string, currentNode: TreeFormItem) {
    const list = treeData.filter(item => {
      return item.pId === currentNode.pId && item._treeId !== currentNode._treeId && item.name === controlValue;
    });
    return list.length !== 0;
  }
}
