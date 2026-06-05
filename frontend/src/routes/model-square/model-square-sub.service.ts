import { Injectable } from '@angular/core';
import { ContextService } from '@services/context.service';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { CommonService } from '@services/common.service';

@Injectable({
  providedIn: 'root',
})
export class ModelSquareSubService {

  private readonly clearFilter$ = new BehaviorSubject<boolean>(false);

  constructor(
    private ctxServ: ContextService,
    private commonService: CommonService,
  ) {}

  //创建任务
  public clearFilter(value: boolean): void {
    this.clearFilter$.next(value);
  }
  public clearUpdate$() {
    return this.clearFilter$.asObservable();
  }



}
