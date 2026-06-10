import { AbstractControl } from '@angular/forms';

class PromptValidators {
  public static PatternValidator(pattern: RegExp, tips: string) {
    return (control: AbstractControl) => {
      const v: string = control.value || '';
      return pattern.test(v) ? null : { invalidName: { tiErrorMessage: tips } };
    };
  }
}

export default PromptValidators;
