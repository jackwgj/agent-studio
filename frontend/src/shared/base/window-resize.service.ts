import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, fromEvent, Subscription, throttleTime } from 'rxjs';
import { CommonService } from '@services/common.service';

@Injectable({ providedIn: 'root' })
export class WindowResizeService implements OnDestroy {
  private resizeSubscription: Subscription;
  private windowWidth$ = new BehaviorSubject<number>(window.innerWidth);
  private safeAreaEnv = null;
  private HWCloudTopHeight: number = 48;

  constructor(private readonly commonService: CommonService) {
    this.resizeSubscription = fromEvent(window, 'resize')
      .pipe(throttleTime(400))
      .subscribe(() => {
        this.windowWidth$.next(window.innerWidth);
      });
    if (commonService.isSite()) {
      this.HWCloudTopHeight = 0;
    }
  }

  getSafeAreaEnv() {
    return this.safeAreaEnv;
  }

  getHWCloudTopHeight() {
    return this.HWCloudTopHeight;
  }

  getWindowWidth() {
    return this.windowWidth$.asObservable();
  }

  // 获取lite环境 license提示的高度
  getLiteAlertTopHeight() {
    if (this.commonService.isSite()) {
      const alertEl = document.querySelector('.license-alert-content');
      return alertEl?.clientHeight ?? 0;
    }
    return 0;
  }

  ngOnDestroy() {
    this.resizeSubscription.unsubscribe();
  }
}
