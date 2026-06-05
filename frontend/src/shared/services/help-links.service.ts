/**当前文件为统一处理帮助面板文件 */
import { Injectable } from '@angular/core';
import { ConsoleFrameworkService } from '@core/services';
import { I18nNamespace } from '@i18n';
import { I18NextEagerPipe } from 'angular-i18next';
import { getLinkForNoUrl } from 'src/utils/commonFn';

@Injectable({
  providedIn: 'root',
})
export class HelpLinksService {
  constructor(private i18n: I18NextEagerPipe) {}

  /**
   * 获取本地保底链接
   * @param topicId 帮助文档的 topicId，
   * @returns 替换后的 URL 字符串
   */
  getDefaultLink(topicId) {
    const helpDomain =
      ConsoleFrameworkService.links?.links?.common_home_support ||
      (<any>window).helpCenterDomain;
    const urlPath = this.i18n.transform(topicId, {
      ns: I18nNamespace.LINK,
    });
    return urlPath ? `${helpDomain}${urlPath}` : '';
  }
  /**
   * 同步获取资料解耦后的链接
   * @param {string} topicId - 帮助文档的 topicId，
   * @returns {string} 替换后的 URL 字符串
   */
  getHelpCenterLink(topicId: string): string {
    return '';
  }
  /**
   * 获取资料解耦后的url，并处理帮助链接提示文本
   * @param i18n - I18NextEagerPipe 实例（由组件注入）
   * @param i18nKey - i18n 翻译键
   * @param helpDocKey - 帮助文档键（对应 getHelpCenterLink 的 topicId 参数）
   * @returns 处理后的提示文本（URL为空时自动移除 a 标签）
   */
  processHelpLinkTipWithI18n(
    i18n: I18NextEagerPipe,
    i18nKey: string,
    helpDocKey: string,
  ): string {
    const url = this.getHelpCenterLink(helpDocKey);
    const urlTip = i18n.transform(i18nKey, { url });
    return getLinkForNoUrl(url, urlTip);
  }
}
