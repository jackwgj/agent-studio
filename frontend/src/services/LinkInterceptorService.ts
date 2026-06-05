import { Injectable, OnDestroy } from '@angular/core';

enum mapKeys {
  target = 'target',
}

export interface LinkInterceptorOptions {
  allowExternal?: boolean;
  allowSameOrigin?: boolean;
  callback?: (
    type: 'click' | 'window.open',
    url: string,
    isBlank: string,
    element?: HTMLElement,
  ) => void;
}

@Injectable({
  providedIn: 'root', // 全局单例
})
export class LinkInterceptorService implements OnDestroy {
  private options: LinkInterceptorOptions;
  private originalWindowOpen: typeof window.open;
  private isInitialized = false;
  private whitelist = []; // 内部网站白名单

  private allowDomainList = [];

  private canNavigation: boolean = false;

  private currentDomain = window.location.hostname;

  constructor() {
    this.options = {
      allowExternal: false,
      allowSameOrigin: true,
      callback: null,
    };
  }

  /**
   * 初始化拦截器
   */
  init(options?: LinkInterceptorOptions): void {
    this.whitelist.push(this.currentDomain);
    if (this.isInitialized) {
      return;
    }

    if (options) {
      this.options = { ...this.options, ...options };
    }

    // 拦截a标签点击
    document.addEventListener('click', this.handleClick.bind(this), true);

    // 拦截window.open
    this.originalWindowOpen = window.open;
    (window as any).open = this.handleWindowOpen.bind(this);

    this.isInitialized = true;
  }

  /**
   * 更新配置
   */
  updateOptions(data: boolean): void {
    this.canNavigation = data;
  }

  /**
   * 处理a标签点击事件
   */
  private handleClick(e: MouseEvent): void {
    const target = e.target as HTMLElement;
    if (target?.tagName !== 'A') {
      return;
    }
    if (!target) {
      return;
    }

    const href = (target as HTMLAnchorElement).getAttribute('href');
    if (!href) {
      return;
    }
    if (href && !href.startsWith('http')) {
      return;
    }

    // 不拦截文档中心链接
    if (href && this.isAllowUrl(href)) {
      return;
    }

    e.preventDefault();

    const isInter = this.isInterUrl(href); // 是否是内部白名单地址
    let isBlank = target[mapKeys.target] === '_blank'; // 是否是新开个浏览器窗口
    if (isInter) {
      if (target[mapKeys.target] === '_blank') {
        window.open(href, '_blank');
      } else {
        window.location.href = href;
      }
      return;
    }
    this.notify('click', href, isBlank ? '_blank' : '');
  }

  /**
   * 处理window.open调用
   */
  private handleWindowOpen(
    url?: string,
    target?: string,
    features?: string,
  ): Window | null {
    if (this.canNavigation) {
      this.updateOptions(false);
      return this.originalWindowOpen.call(window, url, target, features);
    }
    if (!url) {
      return this.originalWindowOpen.call(window, url, target, features);
    }
    const isInter = this.isInterUrl(url);
    if (isInter) {
      return this.originalWindowOpen.call(window, url, target, features);
    } else {
      this.notify('window.open', url, target);
      return null;
    }
  }

  /**
   * 判断是否为内部URL
   */
  private isInterUrl(url: string): boolean {
    try {
      const _url = new URL(url);
      const currentDomain = _url.hostname.replace('www', '');
      return this.whitelist.some((domain) => currentDomain.endsWith(domain));
    } catch {
      return false;
    }
  }

  /**
   * 判断是否为放通url
   */
  private isAllowUrl(url: string): boolean {
    try {
      const _url = new URL(url);
      const currentDomain = _url.hostname;
      return this.allowDomainList.some((domain) => currentDomain === domain);
    } catch {
      return false;
    }
  }

  /**
   * 通知回调
   */
  private notify(
    type: 'click' | 'window.open',
    url: string,
    isBlank: string,
    element?: HTMLElement,
  ): void {
    if (typeof this.options.callback === 'function') {
      this.options.callback(type, url, isBlank, element);
    }
  }

  /**
   * 销毁拦截器
   */
  destroy(): void {
    if (!this.isInitialized) {
      return null;
    }

    document.removeEventListener('click', this.handleClick, true);
    window.open = this.originalWindowOpen;

    this.isInitialized = false;
    return null;
  }

  /**
   * Angular生命周期 - 组件销毁时自动清理
   */
  ngOnDestroy(): void {
    this.destroy();
  }
}
