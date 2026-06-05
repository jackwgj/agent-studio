import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, catchError, from, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { AppAgentRepoService } from '@services/agent-center/app-agent-repo.service';

@Injectable({
  providedIn: 'root',
})
export class MasOperatorService {
  private masOperatorSubject = new BehaviorSubject<any>({
    roles: [],
    masOperator: [],
  });
  public masOperator$ = this.masOperatorSubject.asObservable();

  constructor(private appService: AppAgentRepoService) {}

  // 获取用户权限信息
  fetchMasOperators(): Observable<any> {
    return from(
      this.appService.requireMasOperator().then(masOperator => {
        return masOperator; // 或进行必要的数据转换
      })
    ).pipe(
      tap(masOperator => {
        this.masOperatorSubject.next(masOperator);
      })
    );
  }
  // 获取当前权限（同步方法）
  getCurrentMasOperators(): any {
    return this.masOperatorSubject.value;
  }
}
