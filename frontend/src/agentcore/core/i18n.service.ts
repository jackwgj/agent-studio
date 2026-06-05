import { Inject, Injectable } from '@angular/core';
import {
  I18NEXT_NAMESPACE,
  I18NextEagerPipe,
  PipeOptions,
} from 'angular-i18next';
import { AGENT_CORE, I18N_NAMESPACE } from '../constants/common';

@Injectable()
export class I18nService {
  constructor(
    // I18NextEagerPipe 和 I18NextPipe 的区别： eager实现了数据缓存
    private eagerPipe: I18NextEagerPipe,
    @Inject(I18NEXT_NAMESPACE) private ns: string[],
  ) {}

  transform(key: string | string[], options: PipeOptions = {}) {
    this.setOptionsNs(options, key);
    return this.eagerPipe.transform(key, options);
  }

  /**
   * 仅适配迁移过来的代码，新代码建议直接使用transform
   * @deprecated
   * @param id
   * @param params
   * @returns
   */
  get(id: string, params?: any) {
    return this.transform(id, params);
  }

  private setOptionsNs(options: PipeOptions = {}, key: string | string[]) {
    if (options.ns) {
      return;
    }

    // 如果注入的ns存在且ns中存在agentcore开头的空间，就认为组件自己提供了词条的空间
    if (this.ns && this.ns.some((v) => v.startsWith(AGENT_CORE))) {
      options.ns = this.ns;
      return;
    }

    let defaultNs: string[] = [];
    let keys: string[];
    if (!Array.isArray(key)) {
      keys = [key];
    } else {
      keys = key;
    }

    keys.forEach((k) => {
      const firstWord = k.split('.')[0];
      if (firstWord !== key) {
        defaultNs.push(`${AGENT_CORE}.${firstWord}`);
      }
    });

    defaultNs.push(`${AGENT_CORE}.common`);

    options.ns = defaultNs;
  }
}

/**
 * 获取i18n provider value
 * value为字符串数组，表示要取用的词条
 * useCommon 默认为true，默认加载common词条
 * @returns
 * @param value
 * @param option
 */
export function i18nProviderValue(
  value: string[],
  option?: {
    useCommon?: boolean;
    fallbacks?: string[];
  },
) {
  const { useCommon = true, fallbacks = [] } = option ?? {};
  if (useCommon) {
    value.push(I18N_NAMESPACE.Common);
  }
  return [
    ...value.map((v) => (v.startsWith(AGENT_CORE) ? v : `${AGENT_CORE}.${v}`)),
    ...fallbacks,
  ];
}
