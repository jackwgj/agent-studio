import { Injectable } from '@angular/core';
import { I18NextEagerPipe, PipeOptions } from 'angular-i18next';
import { CommonService } from '@services/common.service';
import { AGENT_CORE } from '../constants/common';
import { RequestService } from './request.service';

@Injectable({
  providedIn: 'root',
})
export class BaseApi {
  // 用来标识该类及其子类是 API 请求处理的类，用以方便其他处理逻辑识别
  isApi = true;
  // 服务ID，用于请求转发后CF框架识别目标地址
  serviceId: string;
  // GET请求的默认查询参数，用于适配多个GET请求都需要使用固定查询参数的场景，简化代码书写
  baseQuery: Record<string, any>;

  // POST 请求的默认查询参数，用于适配多个POST请求都需要使用固定查询参数的场景，简化代码书写
  basePayload: Record<string, any>;

  request = new Proxy(this.httpRequest, {
    get: (target, propKey: string) => {
      if (['get', 'put', 'delete', 'post', 'patch'].includes(propKey)) {
        return (params, ...extraParams) => {
          params.params = {
            ...params.params,
            ...(propKey === 'get' ? this.baseQuery : {}),
            ...(['post', 'put', 'patch'].includes(propKey) ? this.basePayload : {}),
          };
          return this.httpRequest[propKey](
            {
              serviceId: this.serviceId,
              ...params,
            },
            ...extraParams,
          );
        };
      }
      return target[propKey];
    },
  });

  i18n: {
    transform: (key: string | string[], options?: PipeOptions) => string;
    get: (id: string, params?: any) => string;
  };

  constructor(
    private eagerPipe: I18NextEagerPipe,
    private _commonService: CommonService,
    private httpRequest: RequestService,
  ) {
    const transform = (key: string | string[], options?: PipeOptions) => {
      const firstKey = Array.isArray(key) ? key[0] : key;
      const ns = `${AGENT_CORE}.${firstKey.split('.')[0]}`;
      return this.eagerPipe.transform(key, {
        ns,
        ...options,
      });
    };
    this.i18n = {
      transform,
      get: (id: string, params?: any) => transform(id, params),
    };
  }

  isHc() {
    return false;
  }
}
