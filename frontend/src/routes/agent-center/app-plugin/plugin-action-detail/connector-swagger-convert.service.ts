import { Injectable } from "@angular/core";
import i18next from 'i18next';
@Injectable({
  providedIn: "root",
})
export class ConnectorSwaggerConvertService {
  constructor() { }
  private readonly httpCodeOptions: Array<any> = [
    {
      label: "2xx",
      children: [
        { label: i18next.t('connectorswaggerconvertservice_974'), code: "200" },
        { label: i18next.t('connectorswaggerconvertservice_975'), code: "201" },
      ],
    },
    {
      label: "3xx",
      children: [
        { label: i18next.t('connectorswaggerconvertservice_976'), code: "300" },
        { label: i18next.t('connectorswaggerconvertservice_977'), code: "301" },
        { label: i18next.t('connectorswaggerconvertservice_978'), code: "305" },
        { label: i18next.t('connectorswaggerconvertservice_979'), code: "307" },
      ],
    },
    {
      label: "4xx",
      children: [
        { label: i18next.t('connectorswaggerconvertservice_980'), code: "400" },
        { label: i18next.t('connectorswaggerconvertservice_981'), code: "401" },
        { label: i18next.t('connectorswaggerconvertservice_982'), code: "402" },
        { label: i18next.t('connectorswaggerconvertservice_983'), code: "403" },
        { label: i18next.t('connectorswaggerconvertservice_984'), code: "404" },
        { label: i18next.t('connectorswaggerconvertservice_985'), code: "405" },
        { label: i18next.t('connectorswaggerconvertservice_986'), code: "406" },
        { label: i18next.t('connectorswaggerconvertservice_987'), code: "408" },
      ],
    },
    {
      label: "5xx",
      children: [{ label: i18next.t('connectorswaggerconvertservice_988'), code: "500" }],
    },
  ];
  private convertConfig: any = {
    "swagger_3.0": {
      params: {
        from: "openapi_3",
        to: "swagger_2",
        source: {},
      },
      tips: {
        404: i18next.t('connectorswaggerconvertservice_989'),
        500: i18next.t('connectorswaggerconvertservice_990'),
      },
    },
  };

  getHttpCodeOptions() {
    return this.httpCodeOptions;
  }

  getIndexBodyParameters(data) {
    let bodyIndex = -1;
    if (data && data.parameters && data.parameters.length) {
      bodyIndex = data.parameters.findIndex(item => item.in === 'body');
    }
    return bodyIndex;
  }

  getSwaggerParameters(result) {
    let data;
    let url;
    let method;
    let swagger = (result && result.swagger) || {};
    if (Object.keys(swagger).length) {
      url = Object.keys(swagger)[0];
      if (swagger[url] && Object.keys(swagger[url]).length) {
        method = Object.keys(swagger[url])[0];
        data = swagger[url][method];
      }
    }
    return { url, method, data };
  }

  private initSchema() {
    let schema = {
      description: "",
      in: "body",
      name: "body",
      schema: {
        properties: {},
        required: [],
        type: "object",
      },
      "x-hw-body-required": true,
    };
    return [schema];
  }

  private normalizeSchema(schema) {
    let columns = ["properties", "required", "type"];
    let baseData = {
      properties: {},
      required: [],
      type: "object",
    };
    columns.forEach(column => {
      if (!Object.prototype.hasOwnProperty.call(schema, column)) {
        schema[column] = baseData[column];
      }
    });
  }

  /**
   *  只针对body 进行合并
   */
  mergeSwaggerData(source, target) {
    let souOpt = this.getSwaggerParameters(source);
    let tarOpt = this.getSwaggerParameters(target);
    let bodySouIndex = this.getIndexBodyParameters(souOpt.data);
    let bodyTarIndex = this.getIndexBodyParameters(tarOpt.data);
    // 异常数据使用连接器执行动作最新数据
    if (bodyTarIndex === -1) {
      tarOpt.data.parameters = this.initSchema();
      bodyTarIndex = 0;
    } else {
      this.normalizeSchema(tarOpt.data.parameters[bodyTarIndex].schema);
    }

    if (bodySouIndex === -1) {
      bodySouIndex = 0;
      souOpt.data.parameters = this.initSchema();
    } else {
      this.normalizeSchema(souOpt.data.parameters[bodySouIndex].schema);
    }
    let checkType = this.getBodyCheckType(souOpt.data.parameters[bodySouIndex].schema);
    this.setDataExtendConfig(souOpt.data.parameters[bodySouIndex].schema, {
      checkType: checkType,
    });
    if (bodyTarIndex === -1) {
      tarOpt.data.parameters = [
        {
          description: "",
          in: "body",
          name: "body",
          schema: { properties: {}, type: "object" },
          "x-hw-body-required": true,
        },
      ];
      bodyTarIndex = 0;
    }
    this.mergeProperty(
      souOpt.data.parameters[bodySouIndex].schema,
      tarOpt.data.parameters[bodyTarIndex].schema,
      checkType
    );
    let resCheckType = this.getBodyCheckType(souOpt.data.responses["200"].schema);
    this.setDataExtendConfig(souOpt.data.responses["200"].schema, {
      checkType: resCheckType,
    });
    this.mergeProperty(souOpt.data.responses["200"].schema, tarOpt.data.responses["200"].schema, resCheckType);
    return { option: tarOpt };
  }

  mergeProperty(source, target, checkType) {
    if (!source || this.isSameReferType(source, target)) {
      let _checkType = "unchecked";
      if (checkType === "checked") {
        _checkType = "checked";
      }
      this.addChildCheckData(target, _checkType);
      return _checkType;
    }
    let dataExtendConfig = this.getDataExtendConfig(source);
    this.setDataExtendConfig(target, dataExtendConfig);
    // 进行merge操作
    if (!this.isObject(target.type)) {
      // 值类型,直接替换，保留选中状态
      return dataExtendConfig.checkType;
    }
    if (target.type === "object") {
      return this.mergeObjectProperty(source, target, checkType, dataExtendConfig);
    } else {
      let dataExtendConfig = this.getDataExtendConfig(source.items);
      let res = this.mergeProperty(source.items, target.items, dataExtendConfig.checkType);
      target["x-hw-config-data-extend"].checkType = res;
      return res;
    }
  }

  private mergeObjectProperty(source, target, checkType, dataExtendConfig) {
    let sourceMap = this.getSpcLevelData(source);
    let hasChild = true;
    if (Object.prototype.toString.call(target.properties) === "[object Object]") {
      hasChild = Boolean(Object.keys(target.properties).length);
    } else if (Array.isArray(target.properties)) {
      hasChild = Boolean(target.properties.length);
    } else {
      hasChild = Boolean(target.properties);
    }
    if (!Object.keys(sourceMap).length && !hasChild) {
      return checkType;
    }
    let keys = Object.keys(target.properties || {});
    let statics = {
      count: 0,
      checked: 0,
      halfChecked: 0,
      unchecked: 0,
    };
    let names = [];
    for (let index = 0; index < keys.length; index++) {
      statics.count++;
      let key = keys[index];
      let childCheckType = { checkType: checkType };
      if (sourceMap[key]) {
        childCheckType = this.getDataExtendConfig(sourceMap[key]);
      }
      names.push(key);
      let res = this.mergeProperty(sourceMap[key], target.properties[key], childCheckType.checkType);
      statics[res] = statics[res] + 1;
    }
    let sourceKeys = Object.keys(source.properties || {});
    let oldSourceRequires = source.required || [];
    let sourceRequires = [];
    for (let index = 0; index < sourceKeys.length; index++) {
      let souKey = sourceKeys[index];
      if (!names.includes(souKey) && this.isExtendData(source, souKey)) {
        if (oldSourceRequires.includes(souKey)) {
          sourceRequires.push(souKey);
        }
        statics.count++;
        if (!target.properties && source.properties) {
          if (source.properties instanceof Array) {
            target.properties = [];
          } else {
            target.properties = {};
          }
        }
        target.properties[souKey] = source.properties[souKey];
        let checkType = source.properties[souKey]["x-hw-config-data-extend"].checkType || "unchecked";
        statics[checkType] = statics[checkType] + 1;
      }
    }
    this.updateTargetRequired(target, sourceRequires);
    if (statics.count) {
      let _checkType = this.setParentDataExtendConfig(statics);
      target["x-hw-config-data-extend"].checkType = _checkType;
      return _checkType;
    } else {
      return dataExtendConfig.checkType;
    }
  }

  private isExtendData(obj, key) {
    return (
      obj.properties[key] &&
      obj.properties[key]["x-hw-config-data-extend"] &&
      obj.properties[key]["x-hw-config-data-extend"].isExtend
    );
  }

  private updateTargetRequired(target, sourceRequires) {
    if (!sourceRequires || !sourceRequires.length) {
      return false;
    }
    let oldTarRequires = target.required || [];
    let tarRequires = new Set();
    oldTarRequires.forEach(item => {
      tarRequires.add(item);
    });
    sourceRequires.forEach(item => {
      tarRequires.add(item);
    });
    target.required = Array.from(tarRequires.keys());
    return null;
  }

  addChildCheckData(target: any, checkType: string) {
    let config = {
      checkType: checkType,
    };
    this.setDataExtendConfig(target, config);
    if (!this.isObject(target.type)) {
      return config.checkType;
    }
    if (target.type === "object") {
      let keys = Object.keys(target.properties);
      for (let index = 0; index < keys.length; index++) {
        let key = keys[index];
        this.addChildCheckData(target.properties[key], checkType);
      }
    } else {
      this.setDataExtendConfig(target.items, config);
      // array
      this.addChildCheckData(target.items, checkType);
    }
    return null;
  }

  private getSpcLevelData(source) {
    let result = {};
    if (source.properties) {
      let keys = Object.keys(source.properties);
      keys.forEach(key => {
        result[key] = source.properties[key];
      });
    }
    return result;
  }

  private isSameReferType(source, target) {
    return target.type !== source.type && (this.isObject(target.type) || this.isObject(source.type));
  }

  isObject(type) {
    return ["array", "object"].includes(type);
  }

  deepClone(data) {
    return JSON.parse(JSON.stringify(data));
  }

  private getDataExtendConfig(data) {
    if (data && data["x-hw-config-data-extend"]) {
      return this.deepClone(data["x-hw-config-data-extend"]);
    }
    return { checkType: "unchecked" };
  }

  setDataExtendConfig(data: any, config?: any) {
    if (config) {
      data["x-hw-config-data-extend"] = config;
      return;
    } else {
      data["x-hw-config-data-extend"] = { checkType: "unchecked" };
    }
  }

  private setParentDataExtendConfig(statics) {
    if (statics.halfChecked) {
      return "halfChecked";
    }
    if (statics.checked === statics.count) {
      return "checked";
    } else if (statics.unchecked === statics.count) {
      return "unchecked";
    } else {
      return "halfChecked";
    }
  }

  getBodyCheckType(data) {
    if (data.type === "array") {
      return (data.items["x-hw-config-data-extend"] && data.items["x-hw-config-data-extend"].checkType) || "unchecked";
    } else {
      if (!data.properties) {
        return "checked";
      }
      let keys = Object.keys(data.properties);
      if (!keys.length) {
        return "checked";
      }
      let checkedCount = 0;
      for (let index = 0; index < keys.length; index++) {
        let checkType =
          (data.properties[keys[index]]["x-hw-config-data-extend"] &&
            data.properties[keys[index]]["x-hw-config-data-extend"].checkType) ||
          "unchecked";
        if (checkType === "halfChecked") {
          return checkType;
        } else if (checkType === "checked") {
          checkedCount++;
        }
      }
      if (checkedCount === keys.length) {
        return "checked";
      } else if (checkedCount) {
        return "halfChecked";
      } else {
        return "unchecked";
      }
    }
  }

  /**
   * 支持swagger_3 及其他版本的swagger 转换成swagger_2
   * @param data: 文件url 或者数据对象
   * @param reslove: 成功时回调
   * @param reject: 失败时回调
   * @param args: 回调方法参数
   */
  convert(data: any, resolve: Function, reject: Function, args: any) {
    let config = this.getConvertConfig(data);
    if (!config) {
      // swagger 2.0 直接跳过转换
      resolve && resolve(data, ...args);
      return;
    }

    if (!(<any>window).APISpecConverter) {
      reject && reject(data, config.tips["404"], ...args);
      return;
    }

    (<any>window).APISpecConverter.convert(config.params, (err, converted) => {
      if (!converted) {
        reject && reject(data, config.tips["500"], ...args);
        return;
      }
      try {
        let result = JSON.parse(converted.stringify());
        resolve && resolve(result, ...args);
      } catch (error) {
        reject && reject(data, config.tips["500"], ...args);
      }
    });
  }

  /**
   * @description 获取具体版本下的配置（参数及提示等信息）
   * @param data: swagger 对象
   */
  private getConvertConfig(data: any) {
    let type = this.getSwaggerType(data);
    let config = this.getConvertConfigData(type, data);
    return config;
  }

  /**
   * @description 获取 swagger 版本
   * @param data: swagger 对象
   */
  private getSwaggerType(data: any) {
    if (data.openapi) {
      return "swagger_3.0";
    }
    return "swagger_2.0";
  }

  /**
   * @description 组装配置数据
   * @param type: 类型
   * @param data: swagger 对象
   */
  private getConvertConfigData(type: string, data: any) {
    let config = this.convertConfig[type];
    if (!config) {
      return null;
    }
    let cloneDate = JSON.parse(JSON.stringify(config));
    cloneDate.params.source = data;
    return cloneDate;
  }
}
