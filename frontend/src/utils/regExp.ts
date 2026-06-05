import { ValidationErrors, ValidatorFn, AbstractControl } from '@angular/forms';
// 数据源名称
export const regDataSourceName = '^[a-zA-Z0-9_-]{1,32}$';

// 数据库名称
export const regSqlName = '^[a-zA-Z0-9_-]{1,32}$';

// 校验输入端口 只允许数字组成
export const regPort = '^[0-9][0-9]{0,4}$';

// 校验输入IPV4地址
export const regIpv4 =
  '^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$';

// 用户名称
export const regUserName = '^[a-zA-Z0-9_-]{2,32}$';

// 画布英文名称
export const regCanvasNameEn = '^[a-zA-Z][a-zA-Z0-9_-]{1,31}$';

// 画布中文名称
export const regCanvasNameCn = '^[\u4e00-\u9fa5a-zA-Z0-9_-]{0,32}$';

// 组件英文名称
export const regNodeNameEn = '^[a-zA-Z][a-zA-Z0-9_-]{1,31}$';

// 组件中文名称
export const regNodeNameCn = '^[\u4e00-\u9fa5a-zA-Z0-9_-]{0,32}$';

// 参数英文名称
export const regParamNameEn = '^[a-zA-Z][a-zA-Z0-9_.-]{1,31}$';

// 参数中文名称
export const regParamNameCn = '^[\u4e00-\u9fa5a-zA-Z0-9_-]{1,32}$';

// 校验部署服务名称
export const regAImodelDeploy = '^[a-z0-9][a-z0-9-]{0,21}[a-z0-9]$';

export const regEnv = `^[a-zA-Z_][0-9a-zA-Z_]{0,63}$`;

export const regParamasName = '^[a-zA-Z][a-zA-Z0-9_]{0,31}$';

// 模型库
export const modelRegObj = {
  name: '^[a-zA-Z][a-zA-Z0-9_.-]{1,31}$',
  type: '^[\u4e00-\u9fa5a-zA-Z]{1,128}$',
  desc: '^[\u4e00-\u9fa5a-zA-Z0-9_-]{0,256}$',
};
// 模型参数
export const modelParemasRegObj = {
  name: '^[\u4e00-\u9fa5a-zA-Z0-9_-]{1,32}$',
  desc: '^[\u4e00-\u9fa5a-zA-Z0-9_-]{0,256}$',
};
// 组件
export const nodeRegObj = {
  path: '[^\u4e00-\u9fa5]{0,1024}$',
};
// 节点
export const nodeConfigRegObj = {
  cpu: '^([0-9][0-9]{0,1}|100)$',
  memory: '^([0-9][0-9]{0,1}|100)$',
  ram: '^[1-9]\\d*$',
};

export const regDataPath = '^/[a-zA-Z0-9_,/-]{1,250}/$';

export function checkCommon(regExp: any, message: string): ValidatorFn {
  const regEx = new RegExp(regExp);
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (value === '' || (value !== 0 && !value)) {
      return null;
    }

    if (!regEx.test(value)) {
      return {
        folerNameRegx: {
          requiredValue: '',
          actualValue: value,
          tiErrorMessage: message,
        },
      };
    }

    return null;
  };
}
