/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/no-unsafe-argument */
/* eslint-disable max-classes-per-file */
import { Directive, forwardRef, Input } from '@angular/core';
import {
  AbstractControl,
  NG_VALIDATORS,
  ValidationErrors,
  Validator,
} from '@angular/forms';
import { EValidationMode } from '@routes/agent-center/types/common.types';
import i18next from 'i18next';
import { isIntegerStr, isNumberStr } from 'src/utils/utils';
import { I18nNamespace } from '@i18n';

@Directive({
  selector: '[noDupParamName]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => NoDupParamNameValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class NoDupParamNameValidatorDirective implements Validator {
  @Input('noDupParamName') existingValues: string[] = [];

  validate(control: AbstractControl): ValidationErrors | null {
    const isLegalCharacter = /^[a-zA-Z0-9_]+$/.test(control.value as string);
    if (!isLegalCharacter) {
      return {
        serviceName: { tiErrorMessage: i18next.t('variable_validator') },
      };
    }

    const isDup =
      this.existingValues.filter((value) => value === control.value).length > 0;
    return isDup
      ? { serviceName: { tiErrorMessage: i18next.t('noDupParamNameMsg') } }
      : null;
  }
}

@Directive({
  selector: '[httpHeadersOrQueryKey]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => HttpHeadersOrQueryKeyDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class HttpHeadersOrQueryKeyDirective implements Validator {
  @Input('httpHeadersOrQueryKey') existingValues: string[] = [];

  validate(control: AbstractControl): ValidationErrors | null {
    const isLegalCharacter = /^[a-zA-Z0-9_-]+$/.test(control.value as string);
    if (!isLegalCharacter) {
      return {
        serviceName: { tiErrorMessage: i18next.t('variable_validator1') },
      };
    }
    const isDuplicate =
      this.existingValues.filter((value) => value === control.value).length > 0;
    return isDuplicate
      ? { serviceName: { tiErrorMessage: i18next.t('noDupParamNameMsg') } }
      : null;
  }
}

@Directive({
  selector: '[noCn]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => NoCnDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class NoCnDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    return /[\u4e00-\u9fa5]/.test(control.value as string)
      ? {
          serviceName: {
            tiErrorMessage: i18next.t('noCn'),
          },
        }
      : null;
  }
}

@Directive({
  selector: '[numberStr]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => NumberStrValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class NumberStrValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value) {
      const isNumeric = isNumberStr(control.value);

      return isNumeric
        ? null
        : { serviceName: { tiErrorMessage: i18next.t('validator_number') } };
    }

    return null;
  }
}

@Directive({
  selector: '[integerStr]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => IntegerStrValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class IntegerStrValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value) {
      const isInteger = isIntegerStr(control.value);

      return isInteger
        ? null
        : { serviceName: { tiErrorMessage: i18next.t('validator_integer') } };
    }

    return null;
  }
}

@Directive({
  selector: '[positiveIntStr]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => PositiveIntValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class PositiveIntValidatorDirective implements Validator {
  private maxValue: number = Number.MAX_SAFE_INTEGER; // 默认值为最大整数
  private validationMode: EValidationMode = EValidationMode.DEFAULT; // 默认执行校验

  @Input('positiveIntStr')
  set max(value: string | number | null | EValidationMode) {
    this.validationMode = EValidationMode.DEFAULT;

    if (value === null || value === '') {
      this.maxValue = Number.MAX_SAFE_INTEGER; // 直接使用positiveIntStr，不会校验最大值
    } else if (value === EValidationMode.SKIP) {
      this.validationMode = EValidationMode.SKIP;
    } else if (typeof value === 'string') {
      this.maxValue = Number(value);
    } else {
      this.maxValue = value;
    }
  }

  validate(control: AbstractControl): ValidationErrors | null {
    // 如果是跳过校验模式，直接返回 null
    if (this.validationMode === EValidationMode.SKIP) {
      return null;
    }

    if (control.value) {
      const isInteger = isIntegerStr(control.value);

      if (isInteger) {
        const value = Number(control.value);
        if (value < 0) {
          return {
            serviceName: { tiErrorMessage: i18next.t('validator_integer0') },
          };
        }

        if (this.maxValue !== undefined && value > this.maxValue) {
          return {
            serviceName: {
              tiErrorMessage: i18next.t('cannot_exceed_max_question_count'),
            },
          };
        }

        return null;
      }
      return {
        serviceName: { tiErrorMessage: i18next.t('validator_integer') },
      };
    }

    return null;
  }
}

@Directive({
  selector: '[positiveIntIndexStr]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => PositiveIntIndexValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class PositiveIntIndexValidatorDirective implements Validator {
  private maxValue: number = Number.MAX_SAFE_INTEGER; // 默认值为最大整数
  private validationMode: EValidationMode = EValidationMode.DEFAULT; // 默认执行校验

  @Input('positiveIntIndexStr')
  set max(value: string | number | null | EValidationMode) {
    this.validationMode = EValidationMode.DEFAULT;

    if (value === null || value === '') {
      this.maxValue = Number.MAX_SAFE_INTEGER; // 直接使用positiveIntStr，不会校验最大值
    } else if (value === EValidationMode.SKIP) {
      this.validationMode = EValidationMode.SKIP;
    } else if (typeof value === 'string') {
      this.maxValue = Number(value);
    } else {
      this.maxValue = value;
    }
  }

  validate(control: AbstractControl): ValidationErrors | null {
    // 如果是跳过校验模式，直接返回 null
    if (this.validationMode === EValidationMode.SKIP) {
      return null;
    }

    if (control.value) {
      const isInteger = isIntegerStr(control.value);

      if (isInteger) {
        const value = Number(control.value);
        if (value < 0) {
          return {
            serviceName: { tiErrorMessage: i18next.t('validator_integer0') },
          };
        }

        if (this.maxValue !== undefined && value > (this.maxValue - 1) && value !== 0) {
          return {
            serviceName: {
              tiErrorMessage: i18next.t('cannot_exceed_max_non_empty_question_index'),
            },
          };
        }

        return null;
      }
      return {
        serviceName: { tiErrorMessage: i18next.t('validator_integer') },
      };
    }
    return null;
  }
}

@Directive({
  selector: '[urlValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => UrlValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class UrlValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    return /^(?<protocol>https?:\/\/)(?<number>[0-9a-z.]+)/i.test(control.value)
      ? null
      : { serviceName: { tiErrorMessage: i18next.t('validator_url') } };
  }
}

@Directive({
  selector: '[isEmptyObj]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => IsEmptyObjDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class IsEmptyObjDirective implements Validator {
  @Input('isEmptyObj') param: { type: string; children?: any[] };

  validate(control: AbstractControl): ValidationErrors | null {
    if (!control?.value) {
      return null;
    }
    let flag=false;
    if (this.param?.children?.length) {
      flag=true;
      for (let child of this.param.children) {
        if (child.name.trim()!==''){
          flag=false;
          break
        }
      }
    }
    if (flag ||
      (['object', 'array<object>'].includes(this.param.type) &&
      !this.param?.children?.length)
    ) {
      return {
        serviceName: {
          tiErrorMessage: ` ${
            this.param.type === 'object'
              ? i18next.t('object_properties')
              : i18next.t('array_object_properties')
          }${i18next.t('cannot_be_empty', {
            ns: [I18nNamespace.COMMON, I18nNamespace.AGENT_CENTER],
          })}`,
        },
      };
    }

    return null;
  }
}

@Directive({
  selector: '[isEmptyObjReq]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => IsEmptyObjReqDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class IsEmptyObjReqDirective implements Validator {
  @Input('isEmptyObjReq') param: { type: string; children?: any[] };

  validate(control: AbstractControl): ValidationErrors | null {
    if (!control?.value) {
      return null;
    }
    let flag=false;
    if (this.param?.children?.length) {
      flag=true;
      for (let child of this.param.children) {
        if (child.name.trim()!==''){
          flag=false;
          break
        }
      }
    }

    return null;
  }
}

@Directive({
  selector: '[refSelectedRequire]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => RefSelectedRequireDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class RefSelectedRequireDirective implements Validator {
  private isValidate: boolean = true;

  @Input('refSelectedRequire')
  set shouldValidate(value: boolean | string) {
    // value === ''：适配直接使用refSelectedRequire，会进行非空校验
    this.isValidate = value === '' || value === 'true' || value === true;
  }

  validate(control: AbstractControl): ValidationErrors | null {
    if (!this.isValidate) {
      return null;
    }

    if (!control?.value || !Array.isArray(control.value)) {
      return {
        serviceName: { tiErrorMessage: i18next.t('select_placeholder') },
      };
    }

    return null;
  }
}

@Directive({
  selector: '[positiveIntStrLoop]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => PositiveIntLoopValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class PositiveIntLoopValidatorDirective implements Validator {
  private maxValue: number = 1000; // 默认值为最大整数
  private minValue: number = 1; // 默认值为最大整数
  private validationMode: EValidationMode = EValidationMode.DEFAULT; // 默认执行校验

  validate(control: AbstractControl): ValidationErrors | null {
    // 如果是跳过校验模式，直接返回 null
    if (this.validationMode === EValidationMode.SKIP) {
      return null;
    }

    if (control.value) {
      const isInteger = isIntegerStr(control.value);

      if (isInteger) {
        const value = Number(control.value);
        if (value < 0) {
          return {
            serviceName: { tiErrorMessage: i18next.t('validator_integer0') },
          };
        }

        if (
          (this.maxValue !== undefined && value > this.maxValue) ||
          (this.minValue !== undefined && value < this.minValue)
        ) {
          return {
            serviceName: {
              tiErrorMessage: i18next.t('loop_count_range_1_to_1000'),
            },
          };
        }

        return null;
      }
      return {
        serviceName: { tiErrorMessage: i18next.t('validator_integer') },
      };
    }

    return null;
  }
}
