import { Injectable } from '@angular/core';
import { FormatAttrsEnum } from './custom-connectors-models/connectors-const';
import i18next from 'i18next';

@Injectable({
  providedIn: 'root',
})
export class SwaggerTreeConvertService {
  private global_treeId = 1;

  // 请分析注释private _visibilityOptions: Array<any> = [
  // 请分析注释  { primary: "无", secondary: "(在流中正常显示)", value: "none" },
  // 请分析注释  { primary: "高级", secondary: "(默认隐藏在高级设置菜单里)", value: "advanced" },
  // 请分析注释  { primary: "隐藏", secondary: "(向用户隐藏)", value: "internal" },
  // 请分析注释  { primary: "重要", secondary: "(优先显示在表单最开始)", value: "important" },
  // 请分析注释];

  private _formatOptions = [
    { label: i18next.t('connectorformtreesharecomponent_960'), value: FormatAttrsEnum.INPUT },
    { label: i18next.t('connectorformtreesharecomponent_961'), value: FormatAttrsEnum.DATE_TIME },
    { label: i18next.t('connectorformtreesharecomponent_962'), value: FormatAttrsEnum.RICH_EDITOR },
    { label: i18next.t('connectorformtreesharecomponent_963'), value: FormatAttrsEnum.DROPDOWN },
  ];

  constructor() {}

  /**
   *@description 转换 body list 到 swagger: { in: "body" }
   */
  public ListToSwaggerTree(
    list: any[],
    type: 'body' | 'response' | 'root' = 'body',
    isPreView?: boolean,
  ) {
    let result;
    let schemaRootKey = 'properties';
    let schemaType = list[0].type.label;
    if (schemaType === 'array') {
      schemaRootKey = 'items';
    }
    if (type === 'body') {
      result = {
        in: 'body',
        name: 'body',
        description: '',
        'x-hw-body-required': true,

        schema: {
          [schemaRootKey]: {},
          required: [],
          type: schemaType,
        },
      };
    } else if (type === 'root') {
      result = {
        in: 'body',
        name: 'body',
        description: '',
        'x-hw-body-required': true,
        schema: {
          [schemaRootKey]: {},
          required: [],
          type: schemaType,
        },
      };
    } else {
      result = {
        schema: {
          [schemaRootKey]: {},
          type: schemaType,
        },
      };
    }
    SwaggerTreeConvertService.processTreeToSwagger(
      list.map((x) => this.convertItemToSwagger(x, isPreView)),
      result.schema,
      schemaRootKey,
      type === 'body',
    );
    return result;
  }

  /**
   *@description 转换 function list 到 swagger: { in: "body" }
   */
  public functionListToSwaggerTree(
    list: any[],
    cdmInfo: any,
    isInput: boolean,
  ) {
    let result;
    let schemaRootKey = 'properties';
    let schemaType = list[0].type.label;
    if (schemaType === 'array') {
      schemaRootKey = 'items';
    }
    const schema = {
      [schemaRootKey]: {},
      required: [],
      type: schemaType,
    };
    if (isInput) {
      result = {
        in: 'body',
        name: 'body',
        required: false,
        description: 'default',
        schema,
      };
    } else {
      result = {
        description: 'default',
        schema,
      };
    }

    if (cdmInfo) {
      result['x-cdm-info'] = cdmInfo;
    }
    SwaggerTreeConvertService.processTreeToSwagger(
      list.map((x) => this.convertItemToSwagger(x, false)),
      result.schema,
      schemaRootKey,
      true,
    );
    return result;
  }

  private static processTreeToSwagger(
    list,
    schema,
    schemaRootKey,
    isBody = true,
  ) {
    const listMap = {};
    for (const item of list) {
      const { pId, name, _treeId, required, ...itemReset } = item;
      if (item.type === 'object') {
        listMap[item._treeId] = { ...itemReset, properties: {} };
      } else if (item.type === 'array') {
        listMap[item._treeId] = { ...itemReset, items: { type: 'array' } };
      } else {
        listMap[item._treeId] = { ...itemReset };
      }
    }

    for (const item of list) {
      if (item.pId === 0) {
        if (schemaRootKey === 'items') {
          schema[schemaRootKey] = listMap[item._treeId];
        } else if (schemaRootKey === 'properties') {
          schema[schemaRootKey][item.name] = listMap[item._treeId];
          if (item.required && isBody) {
            schema.required.push(item.name);
          }
        }
      } else if (Object.prototype.hasOwnProperty.call(listMap, item.pId)) {
        if (listMap[item.pId].type === 'object') {
          listMap[item.pId].properties[item.name] = listMap[item._treeId];
          if (!listMap[item.pId].required && isBody) {
            listMap[item.pId].required = [];
          }
          if (item.required && isBody) {
            listMap[item.pId].required.push(item.name);
          }
        } else if (listMap[item.pId].type === 'array') {
          listMap[item.pId].items = listMap[item._treeId];
        }
      }
    }
  }
  /* eslint-disable */
  private convertItemToSwagger(item, isPreView: boolean) {
    const result: { [key: string]: any } = {};
    result.description = item.description || '';
    result.default = item.default ? item.default : item['x-hw-default'] || '';
    result['x-hw-default'] = result.default
      ? result.default
      : result['x-hw-default'] || '';
    result.type = item.type.label || 'string';

    result['x-hw-label'] = item['x-hw-label'] || item.label || '';
    if (item['x-cdm-info']) {
      result['x-cdm-info'] = item['x-cdm-info'];
    }
    if (item['x-json-info']) {
      result['x-json-info'] = item['x-json-info'];
    }
    result['x-hw-visibility'] = item.visibility;
    result.isMutiSelect = item.isMutiSelect;
    result.splitSign = item.splitSign;
    result.required = item.isNecessary || false;
    if (!isPreView) {
      result.format = item.format
        ? item.format.value
        : this._formatOptions[0].value;
      result['x-hw-format'] = result.format;
    }
    result['x-hw-select-options'] = item['x-hw-select-options'] || [];
    if (item['x-hw-polling-value']) {
      result['x-hw-polling-value'] = item['x-hw-polling-value'];
    }

    result.pId = item.pId;
    result.name = item.name;
    result._treeId = item._treeId;
    if (result.isMutiSelect) {
      result['x-hw-format'] = result.format = FormatAttrsEnum.MUTIDROPDOWN;
    }
    if (item.splitSign && item.isMutiSelect) {
      result['x-hw-select-options'] = result['x-hw-select-options'].map(
        (items) => {
          return { ...items, splitSign: result.splitSign };
        },
      );
    }
    if (item.checkType) {
      result.checkType = item.checkType;
    }
    if (item.isExtend) {
      result.isExtend = item.isExtend;
    }
    SwaggerTreeConvertService.getXHwExtraField(item).forEach((key) => {
      result[key] = item[key];
    });
    return result;
  }

  public getSaveListItemData(
    item,
    inputParamType?: string,
    isNeedExtraAttrs = false,
  ) {
    const result: { [key: string]: any } = {};
    result['x-hw-label'] = item['x-hw-label'] || item.label || '';
    result.type = item.type.label || 'string';
    result['x-hw-visibility'] = item.visibility;
    result.format = item.format
      ? item.format.value
      : this._formatOptions[0].value;
    result['x-hw-format'] = result.format;
    result.description = item.description || '';
    result.isMutiSelect = item.isMutiSelect || false;
    result.splitSign = item.splitSign || '';
    if (item['x-hw-polling-value']) {
      result['x-hw-polling-value'] = item['x-hw-polling-value'];
    }
    if (result.isMutiSelect) {
      result['x-hw-format'] = result.format = FormatAttrsEnum.MUTIDROPDOWN;
    }
    if (item.default) {
      result.default = item.default || '';
      result['x-hw-default'] = result.default;
    }
    if (item['x-hw-select-options']) {
      result['x-hw-select-options'] = item['x-hw-select-options'];
    }
    this.handleAdvanceListData(result, item, inputParamType, isNeedExtraAttrs);

    SwaggerTreeConvertService.getXHwExtraField(item).forEach((key) => {
      result[key] = item[key];
    });
    return result;
  }

  handleAdvanceListData(
    result: any,
    item,
    inputParamType?: string,
    isNeedExtraAttrs = false,
  ): any {
    if (isNeedExtraAttrs) {
      result.required = item.isNecessary || false;
      result.name = item.name;
      result.in = inputParamType || '';
      result['x-hw-url-encode'] = item.defaultCodeUrl || false;
      result.isMutiSelect = item.isMutiSelect || false;
      result.splitSign = item.splitSign || '';
    }
    if (item.splitSign && item.isMutiSelect) {
      result['x-hw-select-options'] = result['x-hw-select-options'].map(
        (items) => {
          return { ...items, splitSign: result.splitSign };
        },
      );
    }
    return result;
  }

  /**
   * 转换 swagger body 数据到 页面 list
   */
  public swaggerTreeToList(
    tree,
    level: number,
    _treeId: number,
    isProperties,
    required: string[],
    isBody = true,
  ) {
    this.global_treeId = 1;
    const result = [];
    this.processTreeToList(
      tree,
      result,
      level,
      _treeId,
      isProperties,
      required,
      isBody,
    );
    return result;
  }

  /*
   * dsadsa
   * */
  private processTreeToList(
    tree,
    result: any[],
    level: number,
    _treeId: number,
    isProperties,
    required: string[],
    isBody: boolean,
  ) {
    level++;
    if (isProperties) {
      for (const key in tree) {
        if (Object.hasOwnProperty.call(tree, key)) {
          const element = tree[key];
          const addItem = this.convertItemToList(
            element,
            level,
            _treeId,
            key,
            isBody ? (required || []).includes(key) : false,
          );
          result.push(addItem);
          if (
            element.type === 'object' &&
            Object.prototype.hasOwnProperty.call(element, 'properties')
          ) {
            this.processTreeToList(
              element.properties,
              result,
              level,
              addItem._treeId,
              true,
              element.required,
              isBody,
            );
          } else if (element.type === 'array') {
            // 请分析注释 && Object.prototype.hasOwnProperty.call(element, "items")
            if (!element.items) {
              element.items = { type: 'string' };
            }
            if (element['x-cdm-info']) {
              element.items['x-cdm-info'] = element['x-cdm-info'];
            }
            this.processTreeToList(
              element.items,
              result,
              level,
              addItem._treeId,
              false,
              [],
              isBody,
            );
          }
        }
      }
    } else {
      const addItem = this.convertItemToList(
        tree,
        level,
        _treeId,
        'items',
        true,
        true,
      );
      result.push(addItem);
      if (
        tree.type === 'object' &&
        Object.prototype.hasOwnProperty.call(tree, 'properties')
      ) {
        this.processTreeToList(
          tree.properties,
          result,
          level,
          addItem._treeId,
          true,
          tree.required,
          isBody,
        );
      } else if (tree.type === 'array') {
        // 请分析注释&& Object.prototype.hasOwnProperty.call(tree, "items")
        if (!tree.items) {
          tree.items = { type: 'string' };
        }
        if (tree['x-cdm-info']) {
          tree.items['x-cdm-info'] = tree['x-cdm-info'];
        }
        this.processTreeToList(
          tree.items,
          result,
          level,
          addItem._treeId,
          false,
          [],
          isBody,
        );
      }
    }
  }

  private static getXHwExtraField(item: object): string[] {
    if (item) {
      return Object.keys(item).filter((key) => {
        return (
          (key.startsWith('x-hw') &&
            ![
              'x-hw-label',
              'x-hw-select-options',
              'x-hw-visibility',
              'x-hw-default',
              'x-hw-format',
              'x-hw-url-encode',
              'x-hw-polling-value',
            ].includes(key)) ||
          key.startsWith('x-cdm-info')
        );
      });
    } else {
      return [];
    }
  }

  private convertItemToList(
    item,
    level: number,
    _treeId: number,
    key,
    isRequired: boolean,
    isArrayChildNode?: boolean,
  ) {
    if (item.format === FormatAttrsEnum.MUTIDROPDOWN) {
      item.format = FormatAttrsEnum.DROPDOWN;
      item.isMutiSelect = true;
      if (
        item['x-hw-select-options'] &&
        item['x-hw-select-options'].length > 0
      ) {
        item.splitSign = item['x-hw-select-options'][0]?.splitSign || '';
      }
    }

    let returnItem = {
      _treeId: this.global_treeId++,
      pId: _treeId,
      level,
      name: key,
      'x-hw-label': item['x-hw-label'],
      cdmName: item['x-cdm-info']?.['cdm-name'] || '',
      'x-cdm-info': item['x-cdm-info'] || undefined,
      label: item['x-hw-label'] || '',
      type: { label: item.type || 'string' },
      visibility: item['x-hw-visibility'] || 'none',
      'x-hw-select-options': item['x-hw-select-options'] || [],
      format: this._formatOptions.find(
        (x) => x.value === (item.format || FormatAttrsEnum.INPUT),
      ),
      'x-hw-format': this._formatOptions.find(
        (x) => x.value === (item.format || FormatAttrsEnum.INPUT),
      ),
      description: item.description,
      default: item.default,
      isMutiSelect: item.isMutiSelect,
      splitSign: item.splitSign,
      'x-hw-default': item['x-hw-default'],
      'x-hw-polling-value': item['x-hw-polling-value'] || '',
      isNecessary: item['x-hw-visibility'] === 'internal' ? false : isRequired,
      expand: true,
      isShow: true,
      isArrayChildNode: isArrayChildNode || false,
      checkType: item.checkType || null,
      isExtend: item.isExtend || false,
    };
    if (item.checkType) {
      returnItem.checkType = item.checkType;
    }

    if (item.isExtend) {
      returnItem.isExtend = item.isExtend;
    }

    SwaggerTreeConvertService.getXHwExtraField(item).forEach((key) => {
      returnItem[key] = item[key];
    });
    return returnItem;
  }

  public getEditListItemData(item: any, treeId: number, key?: string): any {
    if (item.format === FormatAttrsEnum.MUTIDROPDOWN) {
      item.format = FormatAttrsEnum.DROPDOWN;
      item.isMutiSelect = true;
      if (
        item['x-hw-select-options'] &&
        item['x-hw-select-options'].length > 0
      ) {
        item.splitSign = item['x-hw-select-options'][0]?.splitSign || '';
      }
    }
    let treeItem = {} as any;
    treeItem.pId = 0;
    treeItem.level = 1;
    treeItem._treeId = treeId;
    treeItem.name = key || item.name;
    treeItem.isShow = true;
    treeItem.expand = true;
    treeItem.isNecessary =
      item['x-hw-visibility'] === 'internal' ? false : item.required;
    treeItem.format = this._formatOptions.find(
      (x) => x.value === (item.format || FormatAttrsEnum.INPUT),
    );
    treeItem['x-hw-format'] = treeItem.format;
    treeItem.description = item.description || '';
    treeItem.type = { label: item.type || 'string' };
    if (treeItem.type.label === 'number' && item.default !== undefined) {
      treeItem.default = String(item.default);
    } else {
      treeItem.default = item.default || '';
    }
    if (item['x-hw-polling-value']) {
      treeItem['x-hw-polling-value'] = item['x-hw-polling-value'] || '';
    }
    treeItem['x-hw-default'] = treeItem.default;
    treeItem.visibility = item['x-hw-visibility'] || 'none';
    treeItem.defaultCodeUrl = item['x-hw-url-encode'] || null;
    treeItem.isMutiSelect = item.isMutiSelect || false;
    treeItem.splitSign = item.splitSign || false;
    treeItem.label = item['x-hw-label'] || '';
    treeItem['x-hw-select-options'] = item['x-hw-select-options'] || [];
    SwaggerTreeConvertService.getXHwExtraField(item).forEach((key) => {
      treeItem[key] = item[key];
    });
    return treeItem;
  }

  public gaddInputswagger(input, refs) {
    const res = [];
    input.forEach((item) => {
      item.refs = refs;
      res.push(item);
    });
    return res;
  }

  public getInputswagger(input, refs) {
    const res = [];
    if (input.schema) {
      Object.keys(input.schema.properties).forEach((keyName) => {
        const item: any = {
          description: input.schema.properties[keyName].description,
          name: keyName,
          required: input.schema.required.includes(keyName),
          source: 'user',
          type: input.schema.properties[keyName].type,
          reflection: false,
          value: {
            content: '',
            hint: '',
            type: 'literal',
          },
          refs: refs,
        };
        res.push(item);
      });
    }
    return res;
  }

  public getOutputswagger(output: any) {
    const res = [];
    if (output.schema) {
      Object.keys(output.schema.properties).forEach((keyName) => {
        const item: any = {
          description: output.schema.properties[keyName].description,
          name: keyName,
          required: output.schema.required.includes(keyName),
          source: 'user',
          type: output.schema.properties[keyName].type,
          reflection: false,
          value: {
            content: '',
            hint: '',
            type: 'literal',
          },
        };

        if (output.schema.properties[keyName].type === 'array') {
          item.schema = this.setSchemaItem(
            output.schema.properties[keyName].items,
            output.schema.required.includes(keyName) >= 0,
            output.schema.required,
          );
        } else if (output.schema.properties[keyName].type === 'object') {
          item.schema = this.setSchemaObject(
            output.schema.properties[keyName],
            output.schema.required.includes(keyName) >= 0,
            output.schema.required,
          );
        } else {
        }

        res.push(item);
      });
    }
    return res;
  }

  private setSchemaItem(schema: any, reqL, reqO) {
    let res: any = {
      description: schema.description,
      name: 'item',
      required: reqL,
      source: 'user',
      type: schema.type,
      reflection: false,
      value: {
        content: '',
        hint: '',
        type: 'literal',
      },
    };

    if (schema.type === 'array') {
      res.schema = this.setSchemaItem(schema.items, reqL, reqO);
    } else if (schema.type === 'object') {
      res.schema = this.setSchemaObject(schema, reqL, reqO);
    } else {
      res = {
        name: '',
        type: schema.type,
      };
    }
    return res;
  }

  private setSchemaObject(schema: any, reqL, reqO) {
    const res = [];
    Object.keys(schema.properties).forEach((keyName) => {
      const item: any = {
        description: schema.properties[keyName].description,
        name: keyName,
        required: schema.required.includes(keyName),
        source: 'user',
        type: schema.properties[keyName].type,
        reflection: false,
        value: {
          content: '',
          hint: '',
          type: 'literal',
        },
      };
      if (schema.properties[keyName].type === 'array') {
        item.schema = this.setSchemaItem(
          schema.properties[keyName].items,
          reqL,
          reqO,
        );
      }

      if (schema.properties[keyName].type === 'object') {
        item.schema = this.setSchemaObject(
          schema.properties[keyName],
          reqL,
          reqO,
        );
      }
      res.push(item);
    });
    return res;
  }
}
