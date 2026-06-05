import { I18NextModule, defaultInterpolationFormat } from 'angular-i18next';
import { InitOptions } from 'i18next';

import { Lang, Network } from '@model';
import { AGENT_CORE } from 'src/agentcore/constants/common';

import * as Model from './i18n.model';

/** 支持语言 */
let supportLanguages = [Lang.Language['zh-CN'], Lang.Language['en-US']];

/**
 * I18Next 配置
 *
 * @see {@link https://www.i18next.com/overview/configuration-options | I18next - Configuration Options}
 */
export const I18NEXT_CONFIG: InitOptions = {
  supportedLngs: supportLanguages,
  fallbackLng: {
    [Lang.Language.zh]: [Lang.Language['zh-CN']],
    [Lang.Language.en]: [Lang.Language['en-US']],
    default: [Lang.Language['zh-CN']],
  },
  returnEmptyString: false,
  ns: [
    ...Object.values(Model.I18nNamespace),
    // ...agentCoreWordNamespaces,
  ],
  defaultNS: Model.I18nNamespace.COMMON,
  interpolation: {
    format: I18NextModule.interpolationFormat(defaultInterpolationFormat),
  },
  backend: {
    loadPath: (langs: string[], namespaces: string[]) => {
      const pathList = [];
      langs.forEach(lang => {
        namespaces.forEach((namespace: string) => {
          // agentcore 额外处理下
          if (namespace.startsWith(AGENT_CORE)) {
            const i18nDir = `${Network.ASSETS_PATH}agentcore/i18n`;
            pathList.push(`${i18nDir}/${lang}/${namespace.replace(`${AGENT_CORE}.`, '')}.json`);
            return;
          }
          pathList.push(`${Network.ASSETS_PATH}i18n/${lang}/${namespace}.json`);
        });
      });
      return pathList;
    },
  },
};

export default I18NEXT_CONFIG;
