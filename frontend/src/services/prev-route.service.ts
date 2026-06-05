import { Injectable } from '@angular/core';
import { Router, NavigationEnd, NavigationStart } from '@angular/router';
import { filter } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class PrevRouteService {
  public previousUrl = '';
  public currentUrl = '';

  constructor(private router: Router) {
    this.currentUrl = router.url;          // 组件初始化时的地址

    router.events
      .pipe(filter((e): e is NavigationStart => e instanceof NavigationStart))
      .subscribe((event: NavigationStart) => {
        this.previousUrl = this.currentUrl;
        this.currentUrl = event.url; // 更精确
      });
  }

  /** 返回上一路由（Angular 路由） */
  public goBack(): void {
    if (this.previousUrl) {
      this.router.navigateByUrl(this.previousUrl);
    } else {
      // 兜底：没有上一路由时回到首页
      this.router.navigate(['/']);
    }
  }
}