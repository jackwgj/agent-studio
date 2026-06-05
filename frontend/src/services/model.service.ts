import { Injectable } from '@angular/core';
import { MODEL_PARAMS_INFO_MAP } from '@constants/exp-tmpl-config.const';
import {
  IModel,
  IModelParam,
  IModelParamInfo,
  IModelServInfo,
} from '@interfaces/prompt/model.interface';
import { IModelParamExtend } from '@interfaces/prompt/prompt-writing-repo.interface';
import { cloneDeep, isNil } from 'lodash';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ModelService {
  private readonly models$ = new BehaviorSubject<IModelServInfo[]>([]);

  private readonly model$ = new BehaviorSubject<IModel>(this.getInitParam());

  private readonly disabledParam$ = new BehaviorSubject<string>('top_p');

  private readonly hasModelData$ = new BehaviorSubject<boolean>(true);

  public getModels(): IModelServInfo[] {
    return this.models$.getValue();
  }

  public getModel(): IModel {
    return this.model$.getValue();
  }

  public setModels(models: IModelServInfo[]): void {
    this.models$.next(models);
  }

  public modelsUpdate$(): Observable<IModelServInfo[]> {
    return this.models$.asObservable();
  }

  public setModel(model: IModel): void {
    // model 的值可能存在top p与temperature二者只有一个的情况
    const disabledParam = this.findDisabledParam(model.params);

    if (disabledParam) {
      this.setDisabledParam(disabledParam);
      // eslint-disable-next-line no-param-reassign
      model.params[disabledParam] =
        MODEL_PARAMS_INFO_MAP.get(disabledParam).default;
    }

    this.model$.next(model);
  }

  public getDisabledParam(): string {
    return this.disabledParam$.getValue();
  }

  public setDisabledParam(disabledParam: string): void {
    this.disabledParam$.next(disabledParam);
  }

  public modelUpdate$(): Observable<IModel> {
    return this.model$.asObservable();
  }

  public getParamInfo(name: string): IModelParamInfo {
    return MODEL_PARAMS_INFO_MAP.get(name);
  }

  public getHasModelData(): boolean {
    return this.hasModelData$.getValue();
  }

  public setHasModelData(state: boolean): void {
    this.hasModelData$.next(state);
  }

  public hasModelDataUpdate$(): Observable<boolean> {
    return this.hasModelData$.asObservable();
  }

  public initParam(): void {
    this.setDisabledParam('top_p');
    this.setModel(this.getInitParam());
  }

  public getInitParam(): IModel {
    return {
      name: '',
      params: {
        temperature: MODEL_PARAMS_INFO_MAP.get('temperature').default,
        top_p: MODEL_PARAMS_INFO_MAP.get('top_p').default,
        max_tokens: MODEL_PARAMS_INFO_MAP.get('max_tokens').default,
        presence_penalty: MODEL_PARAMS_INFO_MAP.get('presence_penalty').default,
      },
    };
  }

  public getValidParams(): IModelParamExtend {
    const model = cloneDeep(this.getModel());

    const { params } = model;

    const disabledParam = this.getDisabledParam();

    if (disabledParam) {
      delete params[disabledParam];
    }

    return {
      ...params,
      ...model.extendInfo,
    };
  }

  private findDisabledParam(param: Partial<IModelParam>): string {
    const paramKeys = [
      'temperature',
      'top_p',
      'max_tokens',
      'presence_penalty',
    ];

    // eslint-disable-next-line no-restricted-syntax
    for (const key of paramKeys) {
      if (isNil(param[key])) {
        return key;
      }
    }

    return '';
  }
}
