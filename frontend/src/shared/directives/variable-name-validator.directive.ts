/* eslint-disable eslint-comments/disable-enable-pair */
/* eslint-disable @typescript-eslint/no-unsafe-argument */
/* eslint-disable @typescript-eslint/no-unsafe-call */
/* eslint-disable @typescript-eslint/no-unsafe-member-access */
/* eslint-disable max-classes-per-file */
import { Directive, forwardRef, Input } from '@angular/core';
import {
  AbstractControl,
  NG_VALIDATORS,
  ValidationErrors,
  Validator,
} from '@angular/forms';
import i18next from 'i18next';
import { CommonUtils } from 'src/utils/common.util';
import { isIntegerStr, isNumberStr } from 'src/utils/utils';

interface ContentMap {
  existingValues: any[];
  forbiddenValues?: string[];
  notAllowedValues?: string[];
  isHttp?: any;
}

interface PluginRequestArgs {
  name: string;
  description: string;
  type: string;
  method: string;
  required: boolean;
  default_value: string;
}

interface PluginResponseArgs {
  name: string;
  description: string;
}

@Directive({
  selector: '[variableNameValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => VariableNameValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class VariableNameValidatorDirective implements Validator {
  private isValidate: boolean = true;
  @Input('variableNameValidator')
  set shouldValidate(value: boolean | string) {
    // value === ''：适配直接使用nonEmptyValidator，会进行非空校验
    this.isValidate = value === '' || value === 'true' || value === true;
  }
  validate(control: AbstractControl): ValidationErrors | null {
    if (!this.isValidate) {
      return null;
    }

    return /^[a-zA-Z0-9_]+$/.test(control.value as string)
      ? null
      : { serviceName: { tiErrorMessage: i18next.t('variable_validator') } };
  }
}

@Directive({
  selector: '[mcpAuthValueValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => MCPAuthValueValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class MCPAuthValueValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    if (String(control.value).trim() === '') {
      return null;
    }
    return /^[^\s](.*[^\s])?$/.test(control.value as string)
      ? null
      : {
          serviceName: {
            tiErrorMessage: i18next.t('variable_validator4'),
          },
        };
  }
}

@Directive({
  selector: '[headerParam]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => HeaderParam),
      multi: true,
    },
  ],
  standalone: true,
})
export class HeaderParam implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    return /^[\w-]+$/.test(control.value as string)
      ? null
      : {
          serviceName: {
            tiErrorMessage: i18next.t('variable_validator1'),
          },
        };
  }
}

@Directive({
  selector: '[pluginAuthNameValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => PluginAuthNameValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class PluginAuthNameValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    return /^[a-zA-Z0-9_-]+$/.test(control.value as string)
      ? null
      : {
          serviceName: {
            tiErrorMessage: i18next.t('variable_validator1'),
          },
        };
  }
}

@Directive({
  selector: '[variableLongMemoryNameValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => variableLongMemoryNameValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class variableLongMemoryNameValidatorDirective implements Validator {
  private isValidate: boolean = true;
  @Input('variableLongMemoryNameValidator') contentMap: ContentMap = {
    existingValues: [],
  };
  set shouldValidate(value: boolean | string) {
    // value === ''：适配直接使用nonEmptyValidator，会进行非空校验
    this.isValidate = value === '' || value === 'true' || value === true;
  }
  validate(control: AbstractControl): ValidationErrors | null {
    if (!this.isValidate) {
      return null;
    }

    const isDuplicate =
      this.contentMap.existingValues.filter((value) => value === control.value)
        .length > 0;
    if (isDuplicate) {
      return {
        serviceName: { tiErrorMessage: i18next.t('duplicate_name') },
      };
    }

    return /^[^\^]*$/.test(control.value as string)
      ? null
      : { serviceName: { tiErrorMessage: i18next.t('variable_validator8') } };
  }
}

@Directive({
  selector: '[valueValidityValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => ValueValidityValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class ValueValidityValidatorDirective implements Validator {
  @Input('valueValidityValidator') contentMap: ContentMap = {
    existingValues: [],
    forbiddenValues: [],
    notAllowedValues: [],
    isHttp: false,
  };

  validate(control: AbstractControl): ValidationErrors | null {
    let isLegalCharacter = /^[a-zA-Z_][a-zA-Z0-9_]*$/.test(
      control.value as string,
    );

    if (this.contentMap.isHttp) {
      isLegalCharacter = /^[a-zA-Z_][a-zA-Z0-9_-]*$/.test(
        control.value as string,
      );
    }

    if (!isLegalCharacter) {
      return {
        serviceName: {
          tiErrorMessage: this.contentMap.isHttp
            ? i18next.t('variable_validator3')
            : i18next.t('variable_validator2'),
        },
      };
    }
    const isDuplicate =
      this.contentMap.existingValues.filter((value) => value === control.value)
        .length > 0;
    if (isDuplicate) {
      return {
        serviceName: { tiErrorMessage: i18next.t('noDupParamNameMsg') },
      };
    }

    if (this.contentMap.forbiddenValues?.length) {
      const index = this.contentMap.forbiddenValues?.indexOf(control.value);
      if (index !== -1) {
        return {
          serviceName: {
            tiErrorMessage: `${i18next.t(
              'not_allowed_to_input_system_parameters',
            )}${this.contentMap.forbiddenValues[index]}`,
          },
        };
      }
    }

    if (this.contentMap.notAllowedValues?.length) {
      const index = this.contentMap.notAllowedValues?.indexOf(control.value);
      if (index !== -1) {
        return {
          serviceName: {
            tiErrorMessage: `${i18next.t('no_input_allowed')}${
              this.contentMap.notAllowedValues[index]
            }`,
          },
        };
      }
    }

    return null;
  }
}

@Directive({
  selector: '[valueDuplicateValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => ValueDuplicateValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class ValueDuplicateValidatorDirective implements Validator {
  @Input('valueDuplicateValidator') contentMap: ContentMap = {
    existingValues: [],
    forbiddenValues: [],
    notAllowedValues: [],
    isHttp: false,
  };

  validate(control: AbstractControl): ValidationErrors | null {
    let isLegalCharacter = /^[a-zA-Z_][a-zA-Z0-9_]*$/.test(
      control.value as string,
    );
    const isDuplicate =
      this.contentMap.existingValues.filter((value) => value === control.value)
        .length > 0;
    if (isDuplicate) {
      return {
        serviceName: { tiErrorMessage: i18next.t('noDupParamNameMsg') },
      };
    }

    if (this.contentMap.forbiddenValues?.length) {
      const index = this.contentMap.forbiddenValues?.indexOf(control.value);
      if (index !== -1) {
        return {
          serviceName: {
            tiErrorMessage: `${i18next.t(
              'not_allowed_to_input_system_parameters',
            )}${this.contentMap.forbiddenValues[index]}`,
          },
        };
      }
    }

    return null;
  }
}

@Directive({
  selector: '[flowParamIsUnique]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => FlowParamIsUnique),
      multi: true,
    },
  ],
  standalone: true,
})
export class FlowParamIsUnique implements Validator {
  @Input('flowParamIsUnique') contentMap: ContentMap = {
    existingValues: [],
  };

  validate(control: AbstractControl): ValidationErrors | null {
    const isDuplicate =
      this.contentMap.existingValues.filter((value) => value === control.value)
        .length > 0;
    return isDuplicate
      ? { serviceName: { tiErrorMessage: i18next.t('noDupParamNameMsg') } }
      : null;
  }
}

@Directive({
  selector: '[nonEmptyValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => NonEmptyValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class NonEmptyValidatorDirective implements Validator {
  private isValidate: boolean = true;

  @Input('nonEmptyValidator')
  set shouldValidate(value: boolean | string) {
    // value === ''：适配直接使用nonEmptyValidator，会进行非空校验
    this.isValidate = value === '' || value === 'true' || value === true;
  }

  validate(control: AbstractControl): ValidationErrors | null {
    if (!this.isValidate) {
      return null;
    }

    return !CommonUtils.isNil(control.value) &&
      String(control.value).trim() !== ''
      ? null
      : { serviceName: { tiErrorMessage: i18next.t('input_cannot_be_empty') } };
  }
}

// 通过传值改变，实现表单非空校验
@Directive({
  selector: '[nonEmptyControlValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => NonEmptyControlValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class NonEmptyControlValidatorDirective implements Validator {
  @Input('nonEmptyControlValidator') nonEmptyValidator: boolean = true;

  validate(control: AbstractControl): ValidationErrors | null {
    if (!this.nonEmptyValidator) {
      return null;
    }
    return !CommonUtils.isNil(control.value) &&
      String(control.value).trim() !== ''
      ? null
      : { serviceName: { tiErrorMessage: i18next.t('input_cannot_be_empty') } };
  }
}

@Directive({
  selector: '[pluginRequestArgsDefaultValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => PluginRequestArgsDefaultValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class PluginRequestArgsDefaultValidatorDirective implements Validator {
  @Input('pluginRequestArgsDefaultValidator') requestArgs: PluginRequestArgs = {
    name: '',
    description: '',
    type: 'String',
    method: 'Body',
    required: true,
    default_value: '',
  };

  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value && this.requestArgs.type === 'Integer') {
      const isLegalNumber = /^[-+]?[0-9]+$/.test(control.value);
      if (!isLegalNumber) {
        return {
          serviceName: {
            tiErrorMessage: i18next.t('default_integer'),
          },
        };
      }
    }
    if (control.value && this.requestArgs.type === 'Number') {
      const isLegalNumber =
        /^(?<sign>[-+])?(?<number>[0-9]*\.[0-9]+|[0-9]+)$/.test(control.value);
      if (!isLegalNumber) {
        return {
          serviceName: { tiErrorMessage: i18next.t('default_float') },
        };
      }
    }
    return null;
  }
}


@Directive({
  selector: '[pluginRequestArgsNameValidatorNew]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => PluginRequestArgsNameValidatorDirectiveNew),
      multi: true,
    },
  ],
  standalone: true,
})

export class PluginRequestArgsNameValidatorDirectiveNew implements Validator {
  @Input('pluginRequestArgsNameValidatorNew') existingArgs: string[];

  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value) {
      if (this.existingArgs?.includes(control.value) && !control.pristine) {
        return {
          serviceName: { tiErrorMessage: i18next.t('name_exists') },
        };
      }

      if (control.value) {
        return /^[\w-.]+$/.test(control.value as string)
          ? null
          : {
            serviceName: {
              tiErrorMessage: i18next.t('variable_validator1'),
            },
          };
      }
    }

    return null;
  }
}


@Directive({
  selector: '[pluginRequestArgsNameValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => PluginRequestArgsNameValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})


export class PluginRequestArgsNameValidatorDirective implements Validator {
  @Input('pluginRequestArgsNameValidator') existingArgs: string[];

  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value) {
      if (this.existingArgs?.includes(control.value) && !control.pristine) {
        return {
          serviceName: { tiErrorMessage: i18next.t('name_exists') },
        };
      }

      if (control.value) {
        return /^[\w-]+$/.test(control.value as string)
          ? null
          : {
              serviceName: {
                tiErrorMessage: i18next.t('variable_validator1'),
              },
            };
      }
    }

    return null;
  }
}

@Directive({
  selector: '[pluginResponseArgsValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => PluginResponseArgsValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class PluginResponseArgsValidatorDirective implements Validator {
  @Input('pluginResponseArgsValidator') inputArgs: [
    PluginResponseArgs[],
    PluginResponseArgs,
    string,
  ];

  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value) {
      // name校验
      if (this.inputArgs[2] === 'name') {
        const argTempNames = this.inputArgs[0].map(
          (item) => item.name,
        ) as string[];

        if (argTempNames.includes(control.value) && !control.pristine) {
          return {
            serviceName: {
              tiErrorMessage: i18next.t('param_already_exists'),
            },
          };
        }

        if (!/^[a-zA-Z0-9_-]+$/.test(control.value as string)) {
          return {
            serviceName: {
              tiErrorMessage: i18next.t('variable_validator1'),
            },
          };
        }

        return null;
      }

      return !control.value.trim() && this.inputArgs[1].name
        ? {
            serviceName: { tiErrorMessage: i18next.t('input_cannot_be_empty') },
          }
        : null;
    }

    return this.inputArgs[2] === 'desc' && this.inputArgs[1].name
      ? { serviceName: { tiErrorMessage: i18next.t('input_cannot_be_empty') } }
      : null;
  }
}

@Directive({
  selector: '[enumStr]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => EnumStrValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class EnumStrValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value) {
      const isEnumFormat = (control.value as string)
        .split(/[,，]/)
        .every(Boolean);

      return isEnumFormat
        ? null
        : {
            serviceName: {
              tiErrorMessage: i18next.t('validator_enum'),
            },
          };
    }

    return null;
  }
}

@Directive({
  selector: '[integerRange]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => IntegerRangeValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class IntegerRangeValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value) {
      const [min, max] = control.value.split(',');
      if (!isIntegerStr(min) || !isIntegerStr(max)) {
        return {
          serviceName: {
            tiErrorMessage: i18next.t('integer_range'),
          },
        };
      }

      const minNum = parseInt(min, 10);
      const maxNum = parseInt(max, 10);

      if (minNum > maxNum) {
        return {
          serviceName: {
            tiErrorMessage: i18next.t('numberRangeMsg'),
          },
        };
      }

      return null;
    }

    return null;
  }
}

@Directive({
  selector: '[numberRange]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => NumberRangeValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class NumberRangeValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    if (control.value) {
      const [min, max] = control.value.split(',');
      if (!isNumberStr(min) || !isNumberStr(max)) {
        return {
          serviceName: {
            tiErrorMessage: i18next.t('range_of_numbers'),
          },
        };
      }

      const minNum = parseFloat(min);
      const maxNum = parseFloat(max);

      if (minNum > maxNum) {
        return {
          serviceName: {
            tiErrorMessage: i18next.t('numberRangeMsg'),
          },
        };
      }

      return null;
    }

    return null;
  }
}

@Directive({
  selector: '[cnNameValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => CnNameValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class CnNameValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    const reg = /^[\u4e00-\u9fa5]+$/;
    return control.value && reg.test(control.value)
      ? null
      : { serviceName: { tiErrorMessage: i18next.t('cnNameValidator') } };
  }
}

@Directive({
  selector: '[bodyParamsValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => BodyParamsValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class BodyParamsValidatorDirective implements Validator {
  @Input('bodyParamsValidator') contentMap = {
    existingValues: [],
  };

  validate(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return {
        serviceName: { tiErrorMessage: i18next.t('bodyParamsValidatorEmpty') },
      };
    }

    const isDuplicate =
      this.contentMap.existingValues.filter((value) => value === control.value)
        .length > 0;
    return isDuplicate
      ? {
          serviceName: {
            tiErrorMessage: i18next.t('bodyParamsValidatorExist'),
          },
        }
      : null;
  }
}

@Directive({
  selector: '[valueExitValidityValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => ValueExitValidityValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class ValueExitValidityValidatorDirective implements Validator {
  @Input('valueExitValidityValidator') contentMap: ContentMap = {
    existingValues: [],
  };

  validate(control: AbstractControl): ValidationErrors | null {
    const isDuplicate =
      this.contentMap.existingValues.filter((value) => value === control.value)
        .length > 0;
    if (isDuplicate) {
      return {
        serviceName: { tiErrorMessage: i18next.t('noDupParamNameMsg') },
      };
    }

    return null;
  }
}

@Directive({
  selector: '[lengthValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => LengthValidatorDirective),
      multi: true,
    },
  ],
  standalone: true,
})
export class LengthValidatorDirective implements Validator {
  @Input('lengthValidator') contentMap = [];

  validate(control: AbstractControl): ValidationErrors | null {
    if (this.contentMap[0] >= 1 && control.value && this.contentMap[0] > control.value.length) {
      return {
        serviceName: { tiErrorMessage: `长度不能小于${this.contentMap[0]}` },
      };
    }
    if (this.contentMap[1] >= 1 && control.value && this.contentMap[1] < control.value.length) {
      return {
        serviceName: { tiErrorMessage: `长度不能大于${this.contentMap[1]}` },
      };
    }
    return null;
  }
}
