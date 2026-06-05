export function ThrottlingDecorators(time: number = 1000) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const originalMethod = descriptor.value;
    let _throttleTimeout = null;
    descriptor.value = function (...args: any[]) {
      if (!_throttleTimeout) {
        originalMethod.apply(this, args);
        _throttleTimeout = true;

        setTimeout(() => {
          _throttleTimeout = false;
        }, time);
      }
    };
    return descriptor;
  };
}

export function DebounceDecorators(time: number = 500) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const originalMethod = descriptor.value;
    let timeout = null;
    descriptor.value = function (...args: any[]) {
      if (timeout) {
        clearTimeout(timeout);
      }
      timeout = null;
      timeout = setTimeout(() => {
        originalMethod.apply(this, args);
        clearTimeout(timeout);
        timeout = null;
      }, time);
    };
    return descriptor;
  };
}
