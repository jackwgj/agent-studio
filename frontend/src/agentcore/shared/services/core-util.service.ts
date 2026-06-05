import { effect, Injectable, signal } from '@angular/core';
import { CommonService } from '@services/common.service';
import { HttpService } from '@services/http.service';

@Injectable({
  providedIn: 'root',
})
export class CoreUtilService {
  subscribeBtnStatus = signal(false);

  constructor(
    private readonly commonService: CommonService,
    private readonly http: HttpService,
  ) {
    this.subscription();
  }

  subscriptionCallback(callBackFn: (flag: boolean) => void) {
    effect(() => {
      const subscribeBtnStatus = this.subscribeBtnStatus();
      callBackFn(subscribeBtnStatus);
    });
  }

  subscription() {
    this.http.getCurrentUserResourceStatus().subscribe((res) => {
      this.subscribeBtnStatus.set(this.commonService.getSubscribeStatus());
    });
  }
}
