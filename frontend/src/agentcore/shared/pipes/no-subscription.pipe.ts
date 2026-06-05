import { Pipe, PipeTransform } from '@angular/core';
import { CommonService } from '@services/common.service';
import { HttpService } from '@services/http.service';
import { map } from 'rxjs';

@Pipe({
  name: 'noSubscription',
  standalone: true,
})
export class NoSubscriptionPipe implements PipeTransform {
  constructor(
    public commonService: CommonService,
    private readonly http: HttpService,
  ) {}
  transform(flag: boolean, args?: any): any {
    return this.http.getCurrentUserResourceStatus().pipe(
      map(() => {
        return flag;
      }),
    );
  }
}
