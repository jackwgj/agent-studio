import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { HttpService } from '@services/http.service';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  // 更新空间选项
  private dataSubject = new BehaviorSubject<any>(null);
  data$ = this.dataSubject.asObservable();
  // 更新空间
  private space = new BehaviorSubject<any>(null);
  space$ = this.space.asObservable();

  private spaceOptions = new BehaviorSubject<any>([]);
  spaceOptions$ = this.spaceOptions.asObservable();

  get prefix() {
    return `${this.ctxServ.baseUrl}/mgmtconsole`;
  }

  constructor(private http: HttpService, private ctxServ: ContextService) {}


  public getProduct(): Promise<any> {
    return this.http.getAsync({
      url: '/v1/mgmtconsole/webapi/product/sub-product',
      query:{
        workspace_id:'default'
      }
    });
  }


  public deleteSapceFn(params): Promise<any> {
    return this.http.deleteAsync({
      url: `${this.prefix}/workspace`,
      params,
    });
  }
}
