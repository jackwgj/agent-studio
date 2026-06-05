import { Injectable } from "@angular/core";
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn } from "@angular/forms";
import { swaggerExtProp } from "./custom-connectors-models/connectors-const";
import * as uuid from "uuid";
import i18next from 'i18next';

@Injectable({
  providedIn: "root",
})
export class CreateTriggerActionService {
  constructor() {}

  public basicInfoFormValidator(form: FormGroup, el: HTMLElement): boolean {
    form.markAllAsTouched();
    const errors: ValidationErrors | null = form.valid ? null : this.collectFormErrors(form);
    if (errors) {
      const firstError: any = Object.keys(errors)[0];
      let curEl = el.querySelector(`[formControlName=${firstError}]`) as HTMLInputElement;
      curEl.focus();
    }
    return !!errors;
  }

  private collectFormErrors(form: FormGroup | FormArray): ValidationErrors | null {
    const errors: ValidationErrors = {};
    Object.keys(form.controls).forEach(key => {
      const control = form.get(key);
      if (control && control.invalid) {
        errors[key] = control.errors;
      }
    });
    return Object.keys(errors).length ? errors : null;
  }
  public selectFunValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      let obj = control.value;
      if (!obj) {
        obj = {
          label: "",
          value: "",
        };
      }
      if (!obj.value) {
        return {
          isSelFun: {
            requiredValue: "",
            actualValue: obj,
            tiErrorMessage: i18next.t('codemodalcomponent_287'),
          },
        };
      }
      return null;
    };
  }

  public getAllValuediffValidator(formArray: AbstractControl): ValidatorFn {
    if (!formArray.value) {
      return null;
    }

    let obj = {};
    let objValue = {};
    let isRepeat = false;
    let isRepeatValue = false;
    formArray.value.forEach((item: any, index: number) => {
      if (obj[item.label]) {
        isRepeat = true;
      } else {
        obj[item.label] = true;
        isRepeat = false;
      }
      isRepeat
        ? (formArray.get(index.toString()).get("label") as FormControl).setErrors({
          valueRepeat: { tiErrorMessage: i18next.t('createtriggeractionservice_992') },
        })
        : (formArray.get(index.toString()).get("label") as FormControl).clearValidators();
    });

    formArray.value.forEach((item: any, index: number) => {
      if (
        /[`~!@#$%^&*()_\-+=<>?:"{}|,.\/;'\\[\]·~！@#￥%……&*（）——\-+={}|《》？：“”【】、；‘’，。、]/.test(item.value)
      ) {
        (formArray.get(index.toString()).get("value") as FormControl).setErrors({
          valueRepeat: { tiErrorMessage: i18next.t('createtriggeractionservice_993') },
        });
      }
      if (objValue[item.value]) {
        isRepeatValue = true;
      } else {
        objValue[item.value] = true;
        isRepeatValue = false;
      }
      isRepeatValue
        ? (formArray.get(index.toString()).get("value") as FormControl).setErrors({
          valueRepeat: { tiErrorMessage: i18next.t('createtriggeractionservice_994') },
        })
        : (formArray.get(index.toString()).get("value") as FormControl).clearValidators();
    });

    return null;
  }

  public urlMapPathParamsValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      const value = control.value;
      if (!/^\/([@\+\-\(\[\]\)\?\&%#_.、~&=${}:\w]+\/)*([@\+\-\(\[\]\)\?\&%#_.、~&=${}:\w]*$)/.test(value)) {
        return {
          urlMapPathParamsValidator: {
            actualValue: control.value,
            tiErrorMessage: i18next.t('createtriggeractionservice_995'),
          },
        };
      }
      return null;
    };
  }

  public structWebhookReqData(params, parameters, responses) {
    const { description, triggerName, operationId, consumes, produces } = params;
    const uuidPath = "/" + uuid.v4();
    let obsTrigger = {
      type: "webhook",
      config: {
        properties: {
          url: {
            type: "string",
            format: "webhook_url",
            description: i18next.t('createtriggeractionservice_996'),
            "x-hw-label": i18next.t('createtriggeractionservice_997'),
          },
        },
      },
    };
    return {
      swagger: {
        paths: {
          [uuidPath]: {
            post: {
              operationId,
              description,
              summary: triggerName,
              parameters,
              responses,
              consumes,
              produces,
              [swaggerExtProp["x-is-trigger"]]: obsTrigger,
            },
          },
        },
      },
    };
  }

  public structPullingReqData(params, methodConfig, config, pullingConfig, parameters, responses) {
    const { triggerValue, description, triggerName, operationId, consumes, produces } = params;
    let result = {
      swagger: {
        paths: {
          [methodConfig.url]: {
            [methodConfig.method.value]: {
              operationId,
              description,
              summary: triggerName,
              parameters,
              responses,
              consumes,
              produces,
              [swaggerExtProp["x-is-trigger"]]: {
                type: triggerValue,
                config,
                polling_config: pullingConfig,
              },
            },
          },
        },
      },
    };
    return result;
  }
}
