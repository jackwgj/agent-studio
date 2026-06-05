import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ConsoleFrameworkService {

  dataService: any;

  private static __links: any = {};
  public static get links() {
    return this.__links;
  }
  constructor() {
    this.#init();
  }

  /**
   * 初始化
   */
  #init() {
    this.dataService = {
      getLocale: () => {
        return window.localStorage.getItem('jiuwenlang') || 'zh-cn';
      },
    };
  }
}
