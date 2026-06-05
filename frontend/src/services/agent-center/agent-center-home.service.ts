import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';

// 每个有tab的页面独立分配一个BehaviorSubject，防止互相影响
export type PageKey = 'application' | 'component' | 'single' | 'workflow' | 'multi';

export enum TabIndex {
  SINGLE_AGENT = 0,
  WORKFLOW = 1,
  MULTI_AGENT = 2,
  HIGH = 3,
  MCP_SERVICE = 0,
  PLUGIN = 1,
  INTENT_PACKAGE = 0,
  DATASOURCE = 1,
}

@Injectable({
  providedIn: 'root',
})
export class AgentCenterHomeService {
  private tabMap = new Map<string, BehaviorSubject<number>>();

  private routes: Map<PageKey, string> = new Map([
    ['single', '/home/agent-center/single'],
    ['workflow', '/home/agent-center/workflow'],
    ['multi', '/home/agent-center/multi'],
    ['application', '/home/agent-center/management'],
    ['component', '/home/agent-center/component-library'],
  ]);

  /** 表示导入工作流 或 插件 或 知识型Agent的结果 */
  private readonly importToolsResult$ = new BehaviorSubject<any>({
    succeed_len: 0,
    failed_len: 0,
    inner_plugins_msg: [],
    isShow: '',
    importType: '',
  });

  constructor(private router: Router) {}

  public getTabId$(key: PageKey): BehaviorSubject<number> {
    if (!this.tabMap.has(key)) {
      this.tabMap.set(key, new BehaviorSubject<number>(0));
    }
    return this.tabMap.get(key);
  }

  public getTabObservable$(key: PageKey): Observable<number> {
    return this.getTabId$(key).asObservable();
  }

  public setTabId(key: PageKey, index: TabIndex): void {
    this.getTabId$(key).next(index);
  }

  // 返回页面时指定激活的tab索引，并清空导入提示信息
  public goHome(key: PageKey, index?: TabIndex) {
    const routeUrl = this.routes.get(key);
    this.setTabId(key, index || 0);
    this.setImportRes();
    this.router.navigate([routeUrl]);
  }

  /** 设置导入工作流 或 插件 或 知识型Agent的结果 */
  public setImportRes(result?: any): void {
    this.importToolsResult$.next(result || {});
  }

  public importResUpdate$(): Observable<any> {
    return this.importToolsResult$.asObservable();
  }
}
