const customPropDecoratorKey = Symbol('customPropDecoratorKey');
export const customPropListKey = Symbol('propList');

export type ClassConstructor<T> = new (...args: any[]) => T;
export interface CustomPropDecorator<T> {
  /**
   * 将特定属性名替换
   */
  nameTransfer?: string;

  transform?: Function;

  from?: string;

  name?: string;

  autoNameChange?: boolean;

  keepNotUsed?: boolean;

  instance?: ClassConstructor<T>;
  instances?: ClassConstructor<T>;
  record?: ClassConstructor<T>;
}

class PropTransferOption {
  @prop()
  keepNotUsed = false;

  @prop()
  keepAllProp = false;

  @prop()
  getOrigin = false;

  @prop()
  injector: any[] = [];

  @prop()
  from = true;

  @prop()
  objectDefault = true;
}

export function autoProp<T>(param?: CustomPropDecorator<T>) {
  return prop({ ...param, autoNameChange: true });
}

export function prop<T>(param?: CustomPropDecorator<T>) {
  function customProp(target: any, key: string | symbol) {
    Reflect.defineMetadata(customPropListKey, (Reflect.getMetadata(customPropListKey, target) || []).concat(key), target);

    Reflect.defineMetadata(customPropDecoratorKey, param || {}, target, key);
  }
  return customProp;
}

function changeWithInstance(customPropDecorator, cloneData, prop, option) {
  if (!cloneData[prop] && !option.objectDefault) {
    return;
  }
  // 接口返回的数据可能为null 导致对象数据没有初始化
  const notNullCheck = option.from ? true : cloneData[prop] !== null;
  if (customPropDecorator?.instances && notNullCheck) {
    cloneData[prop] = (cloneData[prop] || []).reduce((arrayS: any, arrayC: any) => {
      const subProp = arrayC instanceof Object ? customPropTransfer(arrayC, customPropDecorator.instances as any, option) : arrayC;
      return Object.keys(subProp).length ? arrayS.concat(subProp) : arrayS;
    }, []);
  } else if (customPropDecorator?.instance && notNullCheck) {
    cloneData[prop] = customPropTransfer(cloneData[prop], customPropDecorator.instance, option);
  } else if (customPropDecorator?.record && notNullCheck) {
    cloneData[prop] = Object.keys(cloneData[prop] || {}).reduce((recordS: any, recordKey: any) => {
      recordS[recordKey] = customPropTransfer(cloneData[prop][recordKey], customPropDecorator.record as any, option);
      return recordS;
    }, {});
  }
}

const camelToSnake = (str: string) => str.replace(/[A-Z]/g, char => `_${char.toLowerCase()}`);

// eslint-disable-next-line max-lines-per-function
export function customPropTransfer<T>(data: any, Type: ClassConstructor<T>, defaultOption?: Partial<PropTransferOption>): T & { __origin?: T } {
  const option: PropTransferOption = {
    ...new PropTransferOption(),
    ...defaultOption,
  };
  const classObj: any = new Type(...option.injector);
  const cloneData = {
    ...classObj,
    ...data,
  };
  const propKeyList = Reflect.getMetadata(customPropListKey, classObj) || [];
  let result = propKeyList.reduce((s: any, c: string) => {
    const customPropDecorator = Reflect.getMetadata(customPropDecoratorKey, classObj as object, c) as CustomPropDecorator<T>;
    let { name, nameTransfer, from } = customPropDecorator;
    if (customPropDecorator?.autoNameChange) {
      name = camelToSnake(c);
    }
    if (name) {
      nameTransfer = name;
      from = name;
    }

    if (from && option.from) {
      cloneData[c] = cloneData[from] ?? cloneData[c];
    }
    if (customPropDecorator?.transform) {
      cloneData[c] = customPropDecorator.transform(cloneData, option);
    }

    changeWithInstance(customPropDecorator, cloneData, c, { ...option, keepNotUsed: customPropDecorator.keepNotUsed });

    // 当对象参数的值为null 时设置为undefined 当对象的参数的值为undefined时返回一个空对象 兼容旧的接口传参逻辑
    if (cloneData[c] === null) {
      cloneData[c] = undefined;
    }
    // 当model 同时作为返回和发送的数据时 发送数据时 设置from = false 转换数据
    if (nameTransfer && (!from || option.from === false)) {
      s[nameTransfer] = cloneData[c];
    } else {
      s[c] = cloneData[c];
    }
    return s;
  }, {} as any);
  if (option.keepAllProp) {
    result = {
      ...cloneData,
      ...result,
    };
  }
  if (option.keepNotUsed) {
    const propMap = propKeyList.reduce((s, c) => {
      s[camelToSnake(c)] = true;
      s[c] = true;
      return s;
    }, {});
    const notUsedData = Object.keys(data || {}).reduce((s, c) => {
      if (!propMap[c] && !result[c]) {
        s[c] = data[c];
      }
      return s;
    }, {});
    result = {
      ...notUsedData,
      ...result,
    };
  }

  if (option.getOrigin) {
    result.__origin = {
      ...cloneData,
    };
  }
  return result;
}
