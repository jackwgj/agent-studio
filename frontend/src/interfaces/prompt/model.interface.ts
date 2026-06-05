export interface IModelParam {
  /** 最小值: 0, 最大值: 1, 步长: 0.1 */
  temperature: number;

  /** 最小值: 0, 最大值: 1, 步长: 0.1 */
  top_p: number;

  /** 最小值: 1, 最大值: 2048, 默认值: 16, 步长: 1 */
  max_tokens: number;

  /** 最小值: -2, 最大值: 2, 步长: 0.1 */
  presence_penalty: number;
}

export interface IModelServInfo {
  deployment_id: string;
  endpoint: string;
  metadata: {
    max_tokens: number;
  };
  model_id?: string;
  name: string;
  project_id: string;
  model_type: string;
}

export interface IModel {
  name: string;
  params: IModelParam;
  extendInfo?: IModelServInfo;
}

export interface IModelBaseParamInfo {
  min: number;
  max: number;
  step: number;
  mid: number;
}

/** 模型参数基本属性 */
export interface IModelParamInfo extends IModelBaseParamInfo {
  name: string;
  default: number;
}
